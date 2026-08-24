package com.etour.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Grouping the smaller "Tour Page tab" DTOs in one file on purpose - these
 * are simple, single-tour-scoped detail records with no independent
 * lifecycle of their own, so a family of tiny classes here is easier to
 * navigate than eleven near-identical files.
 */
public class TourDetailDtos {

    public static class JourneyDetailDto {
        private Long journeyId;
        @NotNull private Integer sequenceNo;
        private Long fromLocationId;
        private String fromLocationName;
        private Long toLocationId;
        private String toLocationName;
        private String modeOfTravel;
        private String notes;

        public Long getJourneyId() { return journeyId; }
        public void setJourneyId(Long journeyId) { this.journeyId = journeyId; }
        public Integer getSequenceNo() { return sequenceNo; }
        public void setSequenceNo(Integer sequenceNo) { this.sequenceNo = sequenceNo; }
        public Long getFromLocationId() { return fromLocationId; }
        public void setFromLocationId(Long fromLocationId) { this.fromLocationId = fromLocationId; }
        public String getFromLocationName() { return fromLocationName; }
        public void setFromLocationName(String fromLocationName) { this.fromLocationName = fromLocationName; }
        public Long getToLocationId() { return toLocationId; }
        public void setToLocationId(Long toLocationId) { this.toLocationId = toLocationId; }
        public String getToLocationName() { return toLocationName; }
        public void setToLocationName(String toLocationName) { this.toLocationName = toLocationName; }
        public String getModeOfTravel() { return modeOfTravel; }
        public void setModeOfTravel(String modeOfTravel) { this.modeOfTravel = modeOfTravel; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class StayMealDto {
        private Long stayMealId;
        @NotNull private Integer dayNumber;
        private Long locationId;
        private String locationName;
        private String hotelName;
        private Boolean breakfast;
        private Boolean lunch;
        private Boolean dinner;

        public Long getStayMealId() { return stayMealId; }
        public void setStayMealId(Long stayMealId) { this.stayMealId = stayMealId; }
        public Integer getDayNumber() { return dayNumber; }
        public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }
        public Long getLocationId() { return locationId; }
        public void setLocationId(Long locationId) { this.locationId = locationId; }
        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }
        public String getHotelName() { return hotelName; }
        public void setHotelName(String hotelName) { this.hotelName = hotelName; }
        public Boolean getBreakfast() { return breakfast; }
        public void setBreakfast(Boolean breakfast) { this.breakfast = breakfast; }
        public Boolean getLunch() { return lunch; }
        public void setLunch(Boolean lunch) { this.lunch = lunch; }
        public Boolean getDinner() { return dinner; }
        public void setDinner(Boolean dinner) { this.dinner = dinner; }
    }

    public static class TourContentDto {
        private Long tourContentId;
        @NotBlank private String contentType; // PASSPORT_VISA / WEATHER / DOS_DONTS / TERMS_CONDITIONS
        private String languageCode = "en";
        @NotBlank private String contentText;

        public Long getTourContentId() { return tourContentId; }
        public void setTourContentId(Long tourContentId) { this.tourContentId = tourContentId; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public String getLanguageCode() { return languageCode; }
        public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
        public String getContentText() { return contentText; }
        public void setContentText(String contentText) { this.contentText = contentText; }
    }

    public static class TourMediaDto {
        private Long mediaId;
        @NotBlank private String mediaType; // IMAGE / VIDEO / PDF / DOCUMENT / AUDIO / MAP
        @NotBlank private String filePath;
        private String mimeType;
        private String tabContext = "GALLERY";
        private Integer displayOrder = 0;

        public Long getMediaId() { return mediaId; }
        public void setMediaId(Long mediaId) { this.mediaId = mediaId; }
        public String getMediaType() { return mediaType; }
        public void setMediaType(String mediaType) { this.mediaType = mediaType; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
        public String getTabContext() { return tabContext; }
        public void setTabContext(String tabContext) { this.tabContext = tabContext; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    }

    public static class ItineraryDto {
        private Long itineraryId;
        @NotNull private Integer dayNumber;
        private String title;
        private String description;

        public Long getItineraryId() { return itineraryId; }
        public void setItineraryId(Long itineraryId) { this.itineraryId = itineraryId; }
        public Integer getDayNumber() { return dayNumber; }
        public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class TourAddonDto {
        private Long addonId;
        @NotBlank private String addonName;
        private String description;
        @NotNull private BigDecimal price;
        private String priceType = "PER_PERSON";
        private Boolean isOptional = true;
        private Integer displayOrder = 0;

        public Long getAddonId() { return addonId; }
        public void setAddonId(Long addonId) { this.addonId = addonId; }
        public String getAddonName() { return addonName; }
        public void setAddonName(String addonName) { this.addonName = addonName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public String getPriceType() { return priceType; }
        public void setPriceType(String priceType) { this.priceType = priceType; }
        public Boolean getIsOptional() { return isOptional; }
        public void setIsOptional(Boolean isOptional) { this.isOptional = isOptional; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    }
}
