package com.etour.dto;

import java.util.List;

/**
 * Nav menu item as the site header consumes it.
 *
 * Replaces serialising the NavMenuItem entity directly, which had two
 * problems that made dropdowns unreliable:
 *   1. `children` is a LAZY @OneToMany - it only serialised because
 *      open-in-view happened to be on, and silently produced an empty list
 *      whenever it wasn't.
 *   2. The collection was unfiltered and unsorted, so retired (active = false)
 *      children still appeared and sort_order was ignored.
 *
 * Children here are explicitly loaded, filtered to active, and sorted.
 */
public class NavMenuItemDto {

    private Long navMenuItemId;
    private String label;
    private String link;
    private Integer sortOrder;
    private List<NavMenuItemDto> children;

    public NavMenuItemDto() {
    }

    public NavMenuItemDto(Long navMenuItemId, String label, String link, Integer sortOrder,
            List<NavMenuItemDto> children) {
        this.navMenuItemId = navMenuItemId;
        this.label = label;
        this.link = link;
        this.sortOrder = sortOrder;
        this.children = children;
    }

    public Long getNavMenuItemId() { return navMenuItemId; }
    public void setNavMenuItemId(Long navMenuItemId) { this.navMenuItemId = navMenuItemId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public List<NavMenuItemDto> getChildren() { return children; }
    public void setChildren(List<NavMenuItemDto> children) { this.children = children; }
}
