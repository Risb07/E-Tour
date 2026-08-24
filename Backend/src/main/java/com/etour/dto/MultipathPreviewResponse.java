package com.etour.dto;

import java.util.List;

/**
 * Dry-run result: exactly what the generator WOULD change, before anything is
 * written. Shown to the admin so a rule with an unintended match is caught
 * before it tags fifty tours.
 */
public class MultipathPreviewResponse {

    private int rulesEvaluated;
    private int toursEvaluated;
    /** Category links that would be created (existing links are not repeated). */
    private int newCategoryLinks;
    /** Products that would be created for the Sector path. */
    private int newProducts;
    private List<PlannedChange> changes;
    /** Rules that matched nothing - usually a sign the value is wrong. */
    private List<String> rulesWithNoMatches;

    public MultipathPreviewResponse() {
    }

    public int getRulesEvaluated() { return rulesEvaluated; }
    public void setRulesEvaluated(int rulesEvaluated) { this.rulesEvaluated = rulesEvaluated; }
    public int getToursEvaluated() { return toursEvaluated; }
    public void setToursEvaluated(int toursEvaluated) { this.toursEvaluated = toursEvaluated; }
    public int getNewCategoryLinks() { return newCategoryLinks; }
    public void setNewCategoryLinks(int newCategoryLinks) { this.newCategoryLinks = newCategoryLinks; }
    public int getNewProducts() { return newProducts; }
    public void setNewProducts(int newProducts) { this.newProducts = newProducts; }
    public List<PlannedChange> getChanges() { return changes; }
    public void setChanges(List<PlannedChange> changes) { this.changes = changes; }
    public List<String> getRulesWithNoMatches() { return rulesWithNoMatches; }
    public void setRulesWithNoMatches(List<String> rulesWithNoMatches) { this.rulesWithNoMatches = rulesWithNoMatches; }

    /** One tour gaining one path. */
    public static class PlannedChange {
        private Long tourId;
        private String tourTitle;
        private String ruleName;
        /** CATEGORY or PRODUCT. */
        private String linkType;
        private String targetName;
        /** True when the link already exists - shown but not re-created. */
        private boolean alreadyLinked;

        public PlannedChange() {
        }

        public PlannedChange(Long tourId, String tourTitle, String ruleName, String linkType,
                String targetName, boolean alreadyLinked) {
            this.tourId = tourId;
            this.tourTitle = tourTitle;
            this.ruleName = ruleName;
            this.linkType = linkType;
            this.targetName = targetName;
            this.alreadyLinked = alreadyLinked;
        }

        public Long getTourId() { return tourId; }
        public void setTourId(Long tourId) { this.tourId = tourId; }
        public String getTourTitle() { return tourTitle; }
        public void setTourTitle(String tourTitle) { this.tourTitle = tourTitle; }
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public String getLinkType() { return linkType; }
        public void setLinkType(String linkType) { this.linkType = linkType; }
        public String getTargetName() { return targetName; }
        public void setTargetName(String targetName) { this.targetName = targetName; }
        public boolean isAlreadyLinked() { return alreadyLinked; }
        public void setAlreadyLinked(boolean alreadyLinked) { this.alreadyLinked = alreadyLinked; }
    }
}
