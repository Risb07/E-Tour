package com.etour.entity;

import com.etour.enums.TourContentType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tour_content")
public class TourContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tour_content_id")
    private Long tourContentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    @JsonIgnoreProperties({ "categories", "tourCosts", "hibernateLazyInitializer", "handler" })
    private Tour tour;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 30)
    private TourContentType contentType;

    @Column(name = "language_code", length = 10)
    private String languageCode = "en";

    @NotBlank
    @Column(name = "content_text", nullable = false, columnDefinition = "TEXT")
    private String contentText;

    private Boolean status = true;

    public Long getTourContentId() { return tourContentId; }
    public void setTourContentId(Long tourContentId) { this.tourContentId = tourContentId; }
    public Tour getTour() { return tour; }
    public void setTour(Tour tour) { this.tour = tour; }
    public TourContentType getContentType() { return contentType; }
    public void setContentType(TourContentType contentType) { this.contentType = contentType; }
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}
