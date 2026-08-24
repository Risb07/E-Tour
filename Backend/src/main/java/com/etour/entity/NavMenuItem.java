package com.etour.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "nav_menu_item")
public class NavMenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nav_menu_item_id")
    private Long navMenuItemId;

    @NotBlank(message = "Label is required")
    @Size(max = 100, message = "Label cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String label;

    @NotBlank(message = "Link is required")
    @Size(max = 255, message = "Link cannot exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String link;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_item_id")
    @JsonIgnoreProperties({ "children", "hibernateLazyInitializer", "handler" })
    private NavMenuItem parentItem;

    @OneToMany(mappedBy = "parentItem")
    @JsonIgnoreProperties({ "parentItem", "hibernateLazyInitializer", "handler" })
    private List<NavMenuItem> children = new ArrayList<>();

    public NavMenuItem() {
    }

    public Long getNavMenuItemId() { return navMenuItemId; }
    public void setNavMenuItemId(Long navMenuItemId) { this.navMenuItemId = navMenuItemId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public NavMenuItem getParentItem() { return parentItem; }
    public void setParentItem(NavMenuItem parentItem) { this.parentItem = parentItem; }
    public List<NavMenuItem> getChildren() { return children; }
    public void setChildren(List<NavMenuItem> children) { this.children = children; }
}
