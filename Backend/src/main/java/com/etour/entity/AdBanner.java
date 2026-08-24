package com.etour.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "ad_banner")
public class AdBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ad_id")
    private Long adId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Size(max = 500, message = "Link URL cannot exceed 500 characters")
    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @NotBlank(message = "Position is required")
    @Size(max = 20, message = "Position cannot exceed 20 characters")
    @Column(nullable = false, length = 20)
    private String position;

    @Column(nullable = false)
    private Boolean status = true;

    public AdBanner() {
    }

    public Long getAdId() { return adId; }
    public void setAdId(Long adId) { this.adId = adId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}
