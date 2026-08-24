package com.etour.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.etour.dto.TourDetailDtos.StayMealDto;
import com.etour.dto.TourDetailDtos.TourContentDto;
import com.etour.entity.StayMeal;
import com.etour.entity.Tour;
import com.etour.entity.TourContent;
import com.etour.enums.TourContentType;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.ItineraryRepository;
import com.etour.repository.JourneyDetailRepository;
import com.etour.repository.LocationRepository;
import com.etour.repository.StayMealRepository;
import com.etour.repository.TourAddonRepository;
import com.etour.repository.TourContentRepository;
import com.etour.repository.TourMediaRepository;
import com.etour.repository.TourRepository;

/**
 * Unit tests for the tour-page detail tabs: "Good to know" (tour_content) and
 * "Stay & Meals" (stay_meal).
 *
 * Plain Mockito - no Spring context and no database, so these run in
 * milliseconds and fail for exactly one reason: the service logic changed.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TourDetailServiceImpl")
class TourDetailServiceImplTest {

    private static final Long TOUR_ID = 1L;
    private static final Long OTHER_TOUR_ID = 2L;

    @Mock private TourRepository tourRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private JourneyDetailRepository journeyDetailRepository;
    @Mock private StayMealRepository stayMealRepository;
    @Mock private TourContentRepository tourContentRepository;
    @Mock private TourMediaRepository tourMediaRepository;
    @Mock private TourAddonRepository tourAddonRepository;
    @Mock private ItineraryRepository itineraryRepository;

    @InjectMocks private TourDetailServiceImpl service;

    private Tour tour;

    @BeforeEach
    void setUp() {
        tour = new Tour();
        tour.setTourId(TOUR_ID);
    }

    /** A stay_meal row that belongs to the given tour. */
    private StayMeal stayMealOwnedBy(Long ownerTourId, Long stayMealId) {
        Tour owner = new Tour();
        owner.setTourId(ownerTourId);
        StayMeal entity = new StayMeal();
        entity.setStayMealId(stayMealId);
        entity.setTour(owner);
        entity.setDayNumber(1);
        return entity;
    }

    /** A tour_content row that belongs to the given tour. */
    private TourContent contentOwnedBy(Long ownerTourId, Long contentId, TourContentType type) {
        Tour owner = new Tour();
        owner.setTourId(ownerTourId);
        TourContent entity = new TourContent();
        entity.setTourContentId(contentId);
        entity.setTour(owner);
        entity.setContentType(type);
        entity.setLanguageCode("en");
        entity.setContentText("existing text");
        entity.setStatus(true);
        return entity;
    }

    // ---------------------------------------------------------------- content

    @Nested
    @DisplayName("upsertContent")
    class UpsertContent {

        @Test
        @DisplayName("updates the existing row instead of inserting a duplicate")
        void updatesExistingRow() {
            // Regression test. This method is named "upsert" but used to build
            // a `new TourContent()` unconditionally, so saving the Weather tab
            // twice left two active rows and the tour page rendered the
            // section twice with no way to remove either.
            TourContent existing = contentOwnedBy(TOUR_ID, 55L, TourContentType.WEATHER);
            when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(tour));
            when(tourContentRepository.findFirstByTour_TourIdAndContentTypeAndLanguageCode(
                    TOUR_ID, TourContentType.WEATHER, "en")).thenReturn(Optional.of(existing));
            when(tourContentRepository.save(any(TourContent.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TourContentDto dto = new TourContentDto();
            dto.setContentType("WEATHER");
            dto.setContentText("Warm and dry in June.");
            dto.setLanguageCode("en");

            TourContentDto saved = service.upsertContent(TOUR_ID, dto);

            ArgumentCaptor<TourContent> captor = ArgumentCaptor.forClass(TourContent.class);
            verify(tourContentRepository).save(captor.capture());

            // Same row id back means it edited rather than inserted.
            assertThat(captor.getValue().getTourContentId()).isEqualTo(55L);
            assertThat(captor.getValue().getContentText()).isEqualTo("Warm and dry in June.");
            assertThat(saved.getTourContentId()).isEqualTo(55L);
        }

        @Test
        @DisplayName("inserts a new row when the tab does not exist yet")
        void insertsWhenAbsent() {
            when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(tour));
            when(tourContentRepository.findFirstByTour_TourIdAndContentTypeAndLanguageCode(
                    TOUR_ID, TourContentType.TERMS_CONDITIONS, "en")).thenReturn(Optional.empty());
            when(tourContentRepository.save(any(TourContent.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TourContentDto dto = new TourContentDto();
            dto.setContentType("TERMS_CONDITIONS");
            dto.setContentText("Free cancellation up to 30 days before departure.");

            service.upsertContent(TOUR_ID, dto);

            ArgumentCaptor<TourContent> captor = ArgumentCaptor.forClass(TourContent.class);
            verify(tourContentRepository).save(captor.capture());
            assertThat(captor.getValue().getTourContentId()).isNull();
            assertThat(captor.getValue().getStatus()).isTrue();
        }

        @Test
        @DisplayName("defaults the language to 'en' when the client omits it")
        void defaultsLanguage() {
            when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(tour));
            when(tourContentRepository.findFirstByTour_TourIdAndContentTypeAndLanguageCode(
                    TOUR_ID, TourContentType.DOS_DONTS, "en")).thenReturn(Optional.empty());
            when(tourContentRepository.save(any(TourContent.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TourContentDto dto = new TourContentDto();
            dto.setContentType("DOS_DONTS");
            dto.setContentText("Remove shoes at temples.");
            dto.setLanguageCode(null);

            service.upsertContent(TOUR_ID, dto);

            ArgumentCaptor<TourContent> captor = ArgumentCaptor.forClass(TourContent.class);
            verify(tourContentRepository).save(captor.capture());
            assertThat(captor.getValue().getLanguageCode()).isEqualTo("en");
        }

        @Test
        @DisplayName("rejects an unknown tour")
        void rejectsUnknownTour() {
            when(tourRepository.findById(99L)).thenReturn(Optional.empty());

            TourContentDto dto = new TourContentDto();
            dto.setContentType("WEATHER");
            dto.setContentText("irrelevant");

            assertThatThrownBy(() -> service.upsertContent(99L, dto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Tour not found");
            verify(tourContentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteContent")
    class DeleteContent {

        @Test
        @DisplayName("soft deletes so getContent stops returning the row")
        void softDeletes() {
            TourContent existing = contentOwnedBy(TOUR_ID, 55L, TourContentType.WEATHER);
            when(tourContentRepository.findById(55L)).thenReturn(Optional.of(existing));

            service.deleteContent(TOUR_ID, 55L);

            ArgumentCaptor<TourContent> captor = ArgumentCaptor.forClass(TourContent.class);
            verify(tourContentRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isFalse();
            verify(tourContentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("refuses to delete a row belonging to a different tour")
        void rejectsCrossTenantDelete() {
            // Guards against /api/tours/2/content/{id} deleting tour 1's row
            // just because the id is guessable.
            TourContent otherTours = contentOwnedBy(OTHER_TOUR_ID, 55L, TourContentType.WEATHER);
            when(tourContentRepository.findById(55L)).thenReturn(Optional.of(otherTours));

            assertThatThrownBy(() -> service.deleteContent(TOUR_ID, 55L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("not found for this tour");
            verify(tourContentRepository, never()).save(any());
        }

        @Test
        @DisplayName("reports a missing row as not found")
        void rejectsMissingRow() {
            when(tourContentRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteContent(TOUR_ID, 404L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ------------------------------------------------------------- stay/meals

    @Nested
    @DisplayName("addStayMeal")
    class AddStayMeal {

        @Test
        @DisplayName("treats omitted meal flags as false rather than null")
        void nullMealFlagsBecomeFalse() {
            // The entity's Boolean columns would otherwise persist as NULL and
            // the tour page's `entry.breakfast &&` checks would silently skip
            // the badge instead of showing "No meals included".
            when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(tour));
            when(stayMealRepository.save(any(StayMeal.class))).thenAnswer(inv -> inv.getArgument(0));

            StayMealDto dto = new StayMealDto();
            dto.setDayNumber(1);
            dto.setHotelName("Hotel Rajmahal");
            dto.setBreakfast(null);
            dto.setLunch(null);
            dto.setDinner(null);

            StayMealDto saved = service.addStayMeal(TOUR_ID, dto);

            assertThat(saved.getBreakfast()).isFalse();
            assertThat(saved.getLunch()).isFalse();
            assertThat(saved.getDinner()).isFalse();
            assertThat(saved.getHotelName()).isEqualTo("Hotel Rajmahal");
        }

        @Test
        @DisplayName("keeps the meal flags that were set")
        void keepsSetMealFlags() {
            when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(tour));
            when(stayMealRepository.save(any(StayMeal.class))).thenAnswer(inv -> inv.getArgument(0));

            StayMealDto dto = new StayMealDto();
            dto.setDayNumber(2);
            dto.setBreakfast(true);
            dto.setLunch(false);
            dto.setDinner(true);

            StayMealDto saved = service.addStayMeal(TOUR_ID, dto);

            assertThat(saved.getBreakfast()).isTrue();
            assertThat(saved.getLunch()).isFalse();
            assertThat(saved.getDinner()).isTrue();
            assertThat(saved.getDayNumber()).isEqualTo(2);
        }

        @Test
        @DisplayName("rejects an unknown tour")
        void rejectsUnknownTour() {
            when(tourRepository.findById(99L)).thenReturn(Optional.empty());

            StayMealDto dto = new StayMealDto();
            dto.setDayNumber(1);

            assertThatThrownBy(() -> service.addStayMeal(99L, dto))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(stayMealRepository, never()).save(any());
        }

        @Test
        @DisplayName("rejects a location id that does not exist")
        void rejectsUnknownLocation() {
            when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(tour));
            when(locationRepository.findById(777L)).thenReturn(Optional.empty());

            StayMealDto dto = new StayMealDto();
            dto.setDayNumber(1);
            dto.setLocationId(777L);

            assertThatThrownBy(() -> service.addStayMeal(TOUR_ID, dto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Location not found");
            verify(stayMealRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteStayMeal")
    class DeleteStayMeal {

        @Test
        @DisplayName("hard deletes the row - stay_meal has no status flag")
        void deletesOwnedRow() {
            StayMeal owned = stayMealOwnedBy(TOUR_ID, 10L);
            when(stayMealRepository.findById(10L)).thenReturn(Optional.of(owned));

            service.deleteStayMeal(TOUR_ID, 10L);

            verify(stayMealRepository).delete(owned);
        }

        @Test
        @DisplayName("refuses to delete a row belonging to a different tour")
        void rejectsCrossTenantDelete() {
            StayMeal otherTours = stayMealOwnedBy(OTHER_TOUR_ID, 10L);
            when(stayMealRepository.findById(10L)).thenReturn(Optional.of(otherTours));

            assertThatThrownBy(() -> service.deleteStayMeal(TOUR_ID, 10L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("not found for this tour");
            verify(stayMealRepository, never()).delete(any());
        }

        @Test
        @DisplayName("reports a missing row as not found")
        void rejectsMissingRow() {
            when(stayMealRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteStayMeal(TOUR_ID, 404L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(stayMealRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("getStayMeals")
    class GetStayMeals {

        @Test
        @DisplayName("maps entities to DTOs in the order the repository returns them")
        void mapsToDtos() {
            StayMeal day1 = stayMealOwnedBy(TOUR_ID, 1L);
            day1.setDayNumber(1);
            day1.setHotelName("Hotel One");
            day1.setBreakfast(true);
            StayMeal day2 = stayMealOwnedBy(TOUR_ID, 2L);
            day2.setDayNumber(2);
            day2.setHotelName("Hotel Two");

            when(stayMealRepository.findByTour_TourIdOrderByDayNumberAsc(TOUR_ID))
                    .thenReturn(List.of(day1, day2));

            List<StayMealDto> result = service.getStayMeals(TOUR_ID);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getHotelName()).isEqualTo("Hotel One");
            assertThat(result.get(0).getBreakfast()).isTrue();
            assertThat(result.get(1).getDayNumber()).isEqualTo(2);
        }

        @Test
        @DisplayName("returns an empty list for a tour with no entries")
        void emptyWhenNone() {
            when(stayMealRepository.findByTour_TourIdOrderByDayNumberAsc(anyLong()))
                    .thenReturn(List.of());

            assertThat(service.getStayMeals(TOUR_ID)).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteMedia")
    class DeleteMedia {

        @Test
        @DisplayName("refuses to delete media belonging to a different tour")
        void rejectsCrossTenantDelete() {
            com.etour.entity.TourMedia media = new com.etour.entity.TourMedia();
            Tour owner = new Tour();
            owner.setTourId(OTHER_TOUR_ID);
            media.setTour(owner);
            media.setStatus(true);
            when(tourMediaRepository.findById(eq(7L))).thenReturn(Optional.of(media));

            assertThatThrownBy(() -> service.deleteMedia(TOUR_ID, 7L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(tourMediaRepository, never()).save(any());
        }
    }
}
