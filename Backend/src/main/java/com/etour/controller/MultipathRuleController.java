package com.etour.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.etour.dto.MultipathPreviewResponse;
import com.etour.dto.TourTagRuleDto;
import com.etour.entity.Category;
import com.etour.entity.SubSector;
import com.etour.entity.TourTagRule;
import com.etour.enums.RuleMatchField;
import com.etour.enums.RuleOperator;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.CategoryRepository;
import com.etour.repository.SubSectorRepository;
import com.etour.repository.TourTagRuleRepository;
import com.etour.service.MultipathGeneratorService;

import jakarta.validation.Valid;

/**
 * Multipath rules: automatically place tours onto extra navigation paths.
 *
 * Entirely ADMIN-only - these rules change what appears where on the public
 * site, so they belong with the other catalogue-management endpoints.
 */
@RestController
@RequestMapping("/api/multipath-rules")
@PreAuthorize("hasRole('ADMIN')")
public class MultipathRuleController {

    private final TourTagRuleRepository ruleRepository;
    private final CategoryRepository categoryRepository;
    private final SubSectorRepository subSectorRepository;
    private final MultipathGeneratorService generatorService;

    public MultipathRuleController(TourTagRuleRepository ruleRepository,
            CategoryRepository categoryRepository,
            SubSectorRepository subSectorRepository,
            MultipathGeneratorService generatorService) {
        this.ruleRepository = ruleRepository;
        this.categoryRepository = categoryRepository;
        this.subSectorRepository = subSectorRepository;
        this.generatorService = generatorService;
    }

    @GetMapping
    public ResponseEntity<List<TourTagRuleDto>> list() {
        return ResponseEntity.ok(
                ruleRepository.findAllByOrderByPriorityAscRuleIdAsc().stream().map(this::toDto).toList());
    }

    @PostMapping
    public ResponseEntity<TourTagRuleDto> create(@Valid @RequestBody TourTagRuleDto dto) {
        TourTagRule rule = new TourTagRule();
        apply(dto, rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(ruleRepository.save(rule)));
    }

    @PutMapping("/{ruleId}")
    public ResponseEntity<TourTagRuleDto> update(@PathVariable Long ruleId,
            @Valid @RequestBody TourTagRuleDto dto) {
        TourTagRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found with id : " + ruleId));
        apply(dto, rule);
        return ResponseEntity.ok(toDto(ruleRepository.save(rule)));
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> delete(@PathVariable Long ruleId) {
        TourTagRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found with id : " + ruleId));
        // Deleting a rule does NOT remove links it already created - those are
        // now ordinary category assignments an admin may have come to rely on.
        ruleRepository.delete(rule);
        return ResponseEntity.noContent().build();
    }

    /**
     * Dry run. Returns exactly what apply() would change, writing nothing -
     * so a mistyped rule is caught before it tags fifty tours.
     */
    @PostMapping("/preview")
    public ResponseEntity<MultipathPreviewResponse> preview() {
        return ResponseEntity.ok(generatorService.preview());
    }

    /** Applies every active rule. Safe to re-run - existing links are skipped. */
    @PostMapping("/apply")
    public ResponseEntity<MultipathPreviewResponse> apply() {
        return ResponseEntity.ok(generatorService.apply());
    }

    private void apply(TourTagRuleDto dto, TourTagRule rule) {
        Category category = categoryRepository.findById(dto.getTargetCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id : " + dto.getTargetCategoryId()));

        SubSector subSector = null;
        if (dto.getTargetSubSectorId() != null) {
            subSector = subSectorRepository.findById(dto.getTargetSubSectorId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Sub-sector not found with id : " + dto.getTargetSubSectorId()));
        }

        rule.setName(dto.getName());
        rule.setMatchField(RuleMatchField.valueOf(dto.getMatchField()));
        rule.setMatchOperator(RuleOperator.valueOf(dto.getMatchOperator()));
        rule.setMatchValue(dto.getMatchValue());
        rule.setMatchValueTo(dto.getMatchValueTo());
        rule.setTargetCategory(category);
        rule.setTargetSubSector(subSector);
        rule.setPriority(dto.getPriority() == null ? 0 : dto.getPriority());
        rule.setActive(dto.getActive() == null || dto.getActive());
    }

    private TourTagRuleDto toDto(TourTagRule rule) {
        TourTagRuleDto dto = new TourTagRuleDto();
        dto.setRuleId(rule.getRuleId());
        dto.setName(rule.getName());
        dto.setMatchField(rule.getMatchField().name());
        dto.setMatchOperator(rule.getMatchOperator().name());
        dto.setMatchValue(rule.getMatchValue());
        dto.setMatchValueTo(rule.getMatchValueTo());
        dto.setTargetCategoryId(rule.getTargetCategory().getCategoryId());
        dto.setTargetCategoryName(rule.getTargetCategory().getCategoryName());
        if (rule.getTargetSubSector() != null) {
            dto.setTargetSubSectorId(rule.getTargetSubSector().getSubSectorId());
            dto.setTargetSubSectorName(rule.getTargetSubSector().getName());
        }
        dto.setPriority(rule.getPriority());
        dto.setActive(rule.getActive());
        return dto;
    }
}
