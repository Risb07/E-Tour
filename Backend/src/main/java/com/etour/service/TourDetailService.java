package com.etour.service;

import java.util.List;

import com.etour.dto.TourDetailDtos.ItineraryDto;
import com.etour.dto.TourDetailDtos.JourneyDetailDto;
import com.etour.dto.TourDetailDtos.StayMealDto;
import com.etour.dto.TourDetailDtos.TourAddonDto;
import com.etour.dto.TourDetailDtos.TourContentDto;
import com.etour.dto.TourDetailDtos.TourMediaDto;

public interface TourDetailService {

    // Journey
    JourneyDetailDto addJourneyLeg(Long tourId, JourneyDetailDto dto);
    List<JourneyDetailDto> getJourney(Long tourId);

    // Stay & Meals
    StayMealDto addStayMeal(Long tourId, StayMealDto dto);
    List<StayMealDto> getStayMeals(Long tourId);
    void deleteStayMeal(Long tourId, Long stayMealId);

    // Content tabs (Passport/Visa, Weather, Do's & Don'ts, Terms)
    TourContentDto upsertContent(Long tourId, TourContentDto dto);
    List<TourContentDto> getContent(Long tourId);
    void deleteContent(Long tourId, Long tourContentId);

    // Media (gallery/video/brochure)
    TourMediaDto addMedia(Long tourId, TourMediaDto dto);
    List<TourMediaDto> getMedia(Long tourId);
    void deleteMedia(Long tourId, Long mediaId);

    // Add-ons
    TourAddonDto addAddon(Long tourId, TourAddonDto dto);
    List<TourAddonDto> getAddons(Long tourId);
    TourAddonDto updateAddon(Long tourId, Long addonId, TourAddonDto dto);
    void deleteAddon(Long tourId, Long addonId);

    // Itinerary
    List<ItineraryDto> getItinerary(Long tourId);
    ItineraryDto addItineraryDay(Long tourId, ItineraryDto dto);
    ItineraryDto updateItineraryDay(Long tourId, Long itineraryId, ItineraryDto dto);
    void deleteItineraryDay(Long tourId, Long itineraryId);
}
