package com.etour.entity;

import com.etour.enums.RuleMatchField;
import com.etour.enums.RuleOperator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A rule that automatically places tours onto additional navigation paths -
 * the "multipath" requirement, without hand-linking every tour.
 *
 * Read as a sentence:
 *   "When TOUR_CODE EQUALS 'INT', add the tour to the 'International' category
 *    (and optionally publish it under the 'Europe' sub-sector)."
 *
 * A rule creates RELATIONSHIPS, never copies of a tour. Running the generator
 * twice produces the same result as running it once, so it is safe to re-run
 * after adding tours.
 */
@Entity
@Table(name = "tour_tag_rule")
public class TourTagRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @NotBlank(message = "Rule name is required")
    @Size(max = 120, message = "Rule name cannot exceed 120 characters")
    @Column(nullable = false, length = 120)
    private String name;

    @NotNull(message = "Match field is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "match_field", nullable = false, length = 30)
    private RuleMatchField matchField;

    @NotNull(message = "Operator is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "match_operator", nullable = false, length = 20)
    private RuleOperator matchOperator;

    /**
     * Compared as text or number depending on matchField. Kept as a String so
     * one column serves "INT", "50000" and "Paris" without three nullable
     * typed columns.
     */
    @Column(name = "match_value", length = 120)
    private String matchValue;

    /** Upper bound, used only by BETWEEN. */
    @Column(name = "match_value_to", length = 120)
    private String matchValueTo;

    /** The category every matching tour is added to. */
    @NotNull(message = "Target category is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_category_id", nullable = false)
    @JsonIgnoreProperties({ "tours", "parentCategory", "hibernateLazyInitializer", "handler" })
    private Category targetCategory;

    /**
     * Optional. When set, the generator also creates a TourProduct under this
     * sub-sector linked back to the tour, so the Sector -> Sub-Sector ->
     * Product path resolves too.
     *
     * Left null by default on purpose: a generated product is a marketing
     * record with a name, image and price that an admin would otherwise have
     * to go back and fill in. Opt in per rule rather than producing empty
     * catalogue entries for every tour.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_sub_sector_id")
    @JsonIgnoreProperties({ "products", "sector", "hibernateLazyInitializer", "handler" })
    private SubSector targetSubSector;

    /** Lower numbers run first. Only affects the order of the preview listing. */
    @Column(name = "priority")
    private Integer priority = 0;

    @Column(nullable = false)
    private Boolean active = true;

    public TourTagRule() {
    }

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public RuleMatchField getMatchField() { return matchField; }
    public void setMatchField(RuleMatchField matchField) { this.matchField = matchField; }
    public RuleOperator getMatchOperator() { return matchOperator; }
    public void setMatchOperator(RuleOperator matchOperator) { this.matchOperator = matchOperator; }
    public String getMatchValue() { return matchValue; }
    public void setMatchValue(String matchValue) { this.matchValue = matchValue; }
    public String getMatchValueTo() { return matchValueTo; }
    public void setMatchValueTo(String matchValueTo) { this.matchValueTo = matchValueTo; }
    public Category getTargetCategory() { return targetCategory; }
    public void setTargetCategory(Category targetCategory) { this.targetCategory = targetCategory; }
    public SubSector getTargetSubSector() { return targetSubSector; }
    public void setTargetSubSector(SubSector targetSubSector) { this.targetSubSector = targetSubSector; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
