package com.etour.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    @JsonIgnoreProperties({
            "parentCategory",
            "hibernateLazyInitializer",
            "handler"
    })
    private Category parentCategory;

    @ManyToMany(mappedBy = "categories")
    @JsonIgnore
    private Set<Tour> tours = new HashSet<>();

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name cannot exceed 100 characters")
    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 255, message = "Image URL cannot exceed 255 characters")
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean status = true;

    @NotBlank(message = "Category code is required")
    @Pattern(regexp = "DOM|ADV|INT", message = "Category code must be DOM, ADV or INT")
    @Column(name = "category_code", length = 10)
    private String categoryCode;

    @NotBlank(message = "Featured flag is required")
    @Pattern(regexp = "Y|N", message = "Featured flag must be Y or N")
    @Column(name = "is_featured", length = 1)
    private String isFeatured;

    public Category() {

    }

    public Category(Long categoryId,
            Category parentCategory,
            String categoryName,
            String description,
            String imageUrl,
            Boolean status,
            String categoryCode,
            String isFeatured) {

        this.categoryId = categoryId;
        this.parentCategory = parentCategory;
        this.categoryName = categoryName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
        this.categoryCode = categoryCode;
        this.isFeatured = isFeatured;
    }

    @Transient
    private Long parentCategoryId;

    /**
     * Number of ACTIVE tours in this category, populated by the service.
     *
     * The `tours` collection above is @JsonIgnore (it would drag every tour
     * into the response), so the frontend previously read `category.tours`
     * and always got 0 - a category card said "0 tours" no matter what.
     * This exposes a real count, and counts only ACTIVE tours so the number
     * matches what the category page will actually show.
     */
    @Transient
    private Long activeTourCount;

    public Long getActiveTourCount() {
        return activeTourCount;
    }

    public void setActiveTourCount(Long activeTourCount) {
        this.activeTourCount = activeTourCount;
    }

    public Long getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Category getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(Category parentCategory) {
        this.parentCategory = parentCategory;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(String isFeatured) {
        this.isFeatured = isFeatured;
    }

}