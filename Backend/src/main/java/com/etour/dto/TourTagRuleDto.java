package com.etour.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Create/update payload and read model for a multipath rule. */
public class TourTagRuleDto {

    private Long ruleId;

    @NotBlank(message = "Rule name is required")
    @Size(max = 120, message = "Rule name cannot exceed 120 characters")
    private String name;

    @NotNull(message = "Match field is required")
    @Pattern(regexp = "TOUR_CODE|TITLE|BASE_PRICE|DURATION_DAYS|ALL",
            message = "matchField must be TOUR_CODE, TITLE, BASE_PRICE, DURATION_DAYS or ALL")
    private String matchField;

    @NotNull(message = "Operator is required")
    @Pattern(regexp = "EQUALS|CONTAINS|GREATER_THAN|LESS_THAN|BETWEEN|ANY",
            message = "matchOperator must be EQUALS, CONTAINS, GREATER_THAN, LESS_THAN, BETWEEN or ANY")
    private String matchOperator;

    @Size(max = 120)
    private String matchValue;

    @Size(max = 120)
    private String matchValueTo;

    @NotNull(message = "Target category is required")
    private Long targetCategoryId;

    private String targetCategoryName;

    /** Optional - also create a product under this sub-sector. */
    private Long targetSubSectorId;

    private String targetSubSectorName;

    private Integer priority = 0;
    private Boolean active = true;

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMatchField() { return matchField; }
    public void setMatchField(String matchField) { this.matchField = matchField; }
    public String getMatchOperator() { return matchOperator; }
    public void setMatchOperator(String matchOperator) { this.matchOperator = matchOperator; }
    public String getMatchValue() { return matchValue; }
    public void setMatchValue(String matchValue) { this.matchValue = matchValue; }
    public String getMatchValueTo() { return matchValueTo; }
    public void setMatchValueTo(String matchValueTo) { this.matchValueTo = matchValueTo; }
    public Long getTargetCategoryId() { return targetCategoryId; }
    public void setTargetCategoryId(Long targetCategoryId) { this.targetCategoryId = targetCategoryId; }
    public String getTargetCategoryName() { return targetCategoryName; }
    public void setTargetCategoryName(String targetCategoryName) { this.targetCategoryName = targetCategoryName; }
    public Long getTargetSubSectorId() { return targetSubSectorId; }
    public void setTargetSubSectorId(Long targetSubSectorId) { this.targetSubSectorId = targetSubSectorId; }
    public String getTargetSubSectorName() { return targetSubSectorName; }
    public void setTargetSubSectorName(String targetSubSectorName) { this.targetSubSectorName = targetSubSectorName; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
