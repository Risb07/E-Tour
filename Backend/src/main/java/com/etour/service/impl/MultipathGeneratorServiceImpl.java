package com.etour.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.dto.MultipathPreviewResponse;
import com.etour.dto.MultipathPreviewResponse.PlannedChange;
import com.etour.entity.Category;
import com.etour.entity.Tour;
import com.etour.entity.TourProduct;
import com.etour.entity.TourTagRule;
import com.etour.enums.RuleMatchField;
import com.etour.enums.RuleOperator;
import com.etour.enums.TourStatus;
import com.etour.repository.TourProductRepository;
import com.etour.repository.TourRepository;
import com.etour.repository.TourTagRuleRepository;
import com.etour.service.MultipathGeneratorService;

/**
 * Puts tours onto multiple navigation paths automatically, driven by
 * TourTagRule rows.
 *
 * The whole point of "multipath" is that ONE tour row is reachable by several
 * routes. This service therefore only ever creates RELATIONSHIPS -
 * tour_category links and (optionally) a tour_product row pointing back at the
 * tour. It never copies a tour, because duplicated tours drift apart in price
 * and availability the moment one is edited.
 *
 * preview() and apply() share one implementation so the dry run can never
 * disagree with what actually happens.
 */
@Service
public class MultipathGeneratorServiceImpl implements MultipathGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(MultipathGeneratorServiceImpl.class);

    private final TourTagRuleRepository ruleRepository;
    private final TourRepository tourRepository;
    private final TourProductRepository tourProductRepository;

    public MultipathGeneratorServiceImpl(TourTagRuleRepository ruleRepository,
            TourRepository tourRepository,
            TourProductRepository tourProductRepository) {
        this.ruleRepository = ruleRepository;
        this.tourRepository = tourRepository;
        this.tourProductRepository = tourProductRepository;
    }

    @Override
    public MultipathPreviewResponse preview() {
        return run(false);
    }

    @Override
    @Transactional
    public MultipathPreviewResponse apply() {
        return run(true);
    }

    /**
     * @param commit false = dry run (nothing written), true = persist changes
     */
    private MultipathPreviewResponse run(boolean commit) {
        List<TourTagRule> rules = ruleRepository.findByActiveTrueOrderByPriorityAsc();

        // Only ACTIVE tours are placed on public paths - tagging a draft would
        // put it in a category page it still can't appear on.
        List<Tour> tours = tourRepository.findAll().stream()
                .filter(t -> t.getStatus() == TourStatus.ACTIVE)
                .toList();

        List<PlannedChange> changes = new ArrayList<>();
        List<String> rulesWithNoMatches = new ArrayList<>();
        int newCategoryLinks = 0;
        int newProducts = 0;

        for (TourTagRule rule : rules) {
            int matched = 0;

            for (Tour tour : tours) {
                if (!matches(rule, tour)) continue;
                matched++;

                // ---- Path 1: category link ----
                Category target = rule.getTargetCategory();
                boolean alreadyInCategory = tour.getCategories().stream()
                        .anyMatch(c -> c.getCategoryId().equals(target.getCategoryId()));

                changes.add(new PlannedChange(tour.getTourId(), tour.getTitle(), rule.getName(),
                        "CATEGORY", target.getCategoryName(), alreadyInCategory));

                if (!alreadyInCategory) {
                    newCategoryLinks++;
                    if (commit) {
                        // Copy the set before mutating - the managed collection
                        // is shared and modifying it in place is a common
                        // source of ConcurrentModificationException.
                        Set<Category> updated = new HashSet<>(tour.getCategories());
                        updated.add(target);
                        tour.setCategories(updated);
                        tourRepository.save(tour);
                    }
                }

                // ---- Path 2: sector/product entry (opt-in per rule) ----
                if (rule.getTargetSubSector() != null) {
                    boolean productExists = tourProductRepository
                            .existsBySubSector_SubSectorIdAndTour_TourId(
                                    rule.getTargetSubSector().getSubSectorId(), tour.getTourId());

                    changes.add(new PlannedChange(tour.getTourId(), tour.getTitle(), rule.getName(),
                            "PRODUCT", rule.getTargetSubSector().getName(), productExists));

                    if (!productExists) {
                        newProducts++;
                        if (commit) {
                            tourProductRepository.save(buildProduct(tour, rule));
                        }
                    }
                }
            }

            if (matched == 0) {
                rulesWithNoMatches.add(rule.getName());
            }
        }

        if (commit) {
            log.info("Multipath generator applied: {} new category links, {} new products, {} rules",
                    newCategoryLinks, newProducts, rules.size());
        }

        MultipathPreviewResponse response = new MultipathPreviewResponse();
        response.setRulesEvaluated(rules.size());
        response.setToursEvaluated(tours.size());
        response.setNewCategoryLinks(newCategoryLinks);
        response.setNewProducts(newProducts);
        response.setChanges(changes);
        response.setRulesWithNoMatches(rulesWithNoMatches);
        return response;
    }

    /** Mirrors the tour's own details so the generated product isn't a blank record. */
    private TourProduct buildProduct(Tour tour, TourTagRule rule) {
        TourProduct product = new TourProduct();
        product.setSubSector(rule.getTargetSubSector());
        product.setTour(tour);
        product.setName(tour.getTitle());
        product.setDescription(tour.getDescription());
        product.setBaseCost(tour.getBasePrice());
        product.setDurationDays(tour.getDurationDays());
        // Nights is conventionally one less than days for a return trip.
        product.setDurationNights(Math.max(0, (tour.getDurationDays() == null ? 1 : tour.getDurationDays()) - 1));
        product.setTourCode(tour.getTourCode() == null ? null : tour.getTourCode().name());
        product.setActive(true);
        product.setSortOrder(0);
        return product;
    }

    /** Evaluates one rule against one tour. */
    private boolean matches(TourTagRule rule, Tour tour) {
        RuleMatchField field = rule.getMatchField();
        RuleOperator op = rule.getMatchOperator();

        if (field == RuleMatchField.ALL || op == RuleOperator.ANY) {
            return true;
        }

        switch (field) {
            case TOUR_CODE: {
                String code = tour.getTourCode() == null ? "" : tour.getTourCode().name();
                return compareText(code, op, rule.getMatchValue());
            }
            case TITLE: {
                String title = tour.getTitle() == null ? "" : tour.getTitle();
                return compareText(title, op, rule.getMatchValue());
            }
            case BASE_PRICE:
                return compareNumber(tour.getBasePrice(), op,
                        parse(rule.getMatchValue()), parse(rule.getMatchValueTo()));
            case DURATION_DAYS:
                return compareNumber(
                        tour.getDurationDays() == null ? null : BigDecimal.valueOf(tour.getDurationDays()),
                        op, parse(rule.getMatchValue()), parse(rule.getMatchValueTo()));
            default:
                return false;
        }
    }

    private boolean compareText(String actual, RuleOperator op, String expected) {
        if (expected == null) return false;
        return switch (op) {
            case EQUALS -> actual.equalsIgnoreCase(expected.trim());
            case CONTAINS -> actual.toLowerCase().contains(expected.trim().toLowerCase());
            // Numeric operators are meaningless on text - treat as no match
            // rather than throwing, so one bad rule can't break the whole run.
            default -> false;
        };
    }

    private boolean compareNumber(BigDecimal actual, RuleOperator op, BigDecimal from, BigDecimal to) {
        if (actual == null || from == null) return false;
        return switch (op) {
            case EQUALS -> actual.compareTo(from) == 0;
            case GREATER_THAN -> actual.compareTo(from) > 0;
            case LESS_THAN -> actual.compareTo(from) < 0;
            case BETWEEN -> to != null && actual.compareTo(from) >= 0 && actual.compareTo(to) <= 0;
            default -> false;
        };
    }

    /** Null-safe, exception-safe numeric parse - a bad value simply never matches. */
    private BigDecimal parse(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
