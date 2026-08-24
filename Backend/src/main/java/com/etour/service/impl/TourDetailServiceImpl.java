package com.etour.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.etour.dto.TourDetailDtos.ItineraryDto;
import com.etour.dto.TourDetailDtos.JourneyDetailDto;
import com.etour.dto.TourDetailDtos.StayMealDto;
import com.etour.dto.TourDetailDtos.TourAddonDto;
import com.etour.dto.TourDetailDtos.TourContentDto;
import com.etour.dto.TourDetailDtos.TourMediaDto;
import com.etour.entity.Itinerary;
import com.etour.entity.JourneyDetail;
import com.etour.entity.Location;
import com.etour.entity.StayMeal;
import com.etour.entity.Tour;
import com.etour.entity.TourAddon;
import com.etour.entity.TourContent;
import com.etour.entity.TourMedia;
import com.etour.enums.MediaType;
import com.etour.enums.PriceType;
import com.etour.enums.TabContext;
import com.etour.enums.TourContentType;
import com.etour.enums.TravelMode;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.ItineraryRepository;
import com.etour.repository.JourneyDetailRepository;
import com.etour.repository.LocationRepository;
import com.etour.repository.StayMealRepository;
import com.etour.repository.TourAddonRepository;
import com.etour.repository.TourContentRepository;
import com.etour.repository.TourMediaRepository;
import com.etour.repository.TourRepository;
import com.etour.service.TourDetailService;

@Service
public class TourDetailServiceImpl implements TourDetailService {

    private final TourRepository tourRepository;
    private final LocationRepository locationRepository;
    private final JourneyDetailRepository journeyDetailRepository;
    private final StayMealRepository stayMealRepository;
    private final TourContentRepository tourContentRepository;
    private final TourMediaRepository tourMediaRepository;
    private final TourAddonRepository tourAddonRepository;
    private final ItineraryRepository itineraryRepository;

    public TourDetailServiceImpl(TourRepository tourRepository, LocationRepository locationRepository,
            JourneyDetailRepository journeyDetailRepository, StayMealRepository stayMealRepository,
            TourContentRepository tourContentRepository, TourMediaRepository tourMediaRepository,
            TourAddonRepository tourAddonRepository, ItineraryRepository itineraryRepository) {
        this.tourRepository = tourRepository;
        this.locationRepository = locationRepository;
        this.journeyDetailRepository = journeyDetailRepository;
        this.stayMealRepository = stayMealRepository;
        this.tourContentRepository = tourContentRepository;
        this.tourMediaRepository = tourMediaRepository;
        this.tourAddonRepository = tourAddonRepository;
        this.itineraryRepository = itineraryRepository;
    }

    private Tour requireTour(Long tourId) {
        return tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found: " + tourId));
    }

    // ---------- Journey ----------

    @Override
    public JourneyDetailDto addJourneyLeg(Long tourId, JourneyDetailDto dto) {
        Tour tour = requireTour(tourId);
        JourneyDetail entity = new JourneyDetail();
        entity.setTour(tour);
        entity.setSequenceNo(dto.getSequenceNo());
        if (dto.getFromLocationId() != null) {
            entity.setFromLocation(locationRepository.findById(dto.getFromLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("From location not found")));
        }
        if (dto.getToLocationId() != null) {
            entity.setToLocation(locationRepository.findById(dto.getToLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("To location not found")));
        }
        entity.setModeOfTravel(dto.getModeOfTravel() == null ? TravelMode.ROAD : TravelMode.valueOf(dto.getModeOfTravel()));
        entity.setNotes(dto.getNotes());
        return toDto(journeyDetailRepository.save(entity));
    }

    @Override
    public List<JourneyDetailDto> getJourney(Long tourId) {
        return journeyDetailRepository.findByTour_TourIdOrderBySequenceNoAsc(tourId).stream()
                .map(this::toDto).toList();
    }

    private JourneyDetailDto toDto(JourneyDetail e) {
        JourneyDetailDto d = new JourneyDetailDto();
        d.setJourneyId(e.getJourneyId());
        d.setSequenceNo(e.getSequenceNo());
        if (e.getFromLocation() != null) {
            d.setFromLocationId(e.getFromLocation().getLocationId());
            d.setFromLocationName(e.getFromLocation().getLocationName());
        }
        if (e.getToLocation() != null) {
            d.setToLocationId(e.getToLocation().getLocationId());
            d.setToLocationName(e.getToLocation().getLocationName());
        }
        d.setModeOfTravel(e.getModeOfTravel().name());
        d.setNotes(e.getNotes());
        return d;
    }

    // ---------- Stay & Meals ----------

    @Override
    public StayMealDto addStayMeal(Long tourId, StayMealDto dto) {
        Tour tour = requireTour(tourId);
        StayMeal entity = new StayMeal();
        entity.setTour(tour);
        entity.setDayNumber(dto.getDayNumber());
        if (dto.getLocationId() != null) {
            entity.setLocation(locationRepository.findById(dto.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found")));
        }
        entity.setHotelName(dto.getHotelName());
        entity.setBreakfast(Boolean.TRUE.equals(dto.getBreakfast()));
        entity.setLunch(Boolean.TRUE.equals(dto.getLunch()));
        entity.setDinner(Boolean.TRUE.equals(dto.getDinner()));
        return toDto(stayMealRepository.save(entity));
    }

    @Override
    public List<StayMealDto> getStayMeals(Long tourId) {
        return stayMealRepository.findByTour_TourIdOrderByDayNumberAsc(tourId).stream()
                .map(this::toDto).toList();
    }

    @Override
    public void deleteStayMeal(Long tourId, Long stayMealId) {
        StayMeal existing = stayMealRepository.findById(stayMealId)
                .orElseThrow(() -> new ResourceNotFoundException("Stay & meal entry not found: " + stayMealId));
        if (!existing.getTour().getTourId().equals(tourId)) {
            throw new ResourceNotFoundException("Stay & meal entry not found for this tour");
        }
        // Hard delete, like itinerary days - stay_meal has no status flag, and
        // getStayMeals returns every row for the tour.
        stayMealRepository.delete(existing);
    }

    private StayMealDto toDto(StayMeal e) {
        StayMealDto d = new StayMealDto();
        d.setStayMealId(e.getStayMealId());
        d.setDayNumber(e.getDayNumber());
        if (e.getLocation() != null) {
            d.setLocationId(e.getLocation().getLocationId());
            d.setLocationName(e.getLocation().getLocationName());
        }
        d.setHotelName(e.getHotelName());
        d.setBreakfast(e.getBreakfast());
        d.setLunch(e.getLunch());
        d.setDinner(e.getDinner());
        return d;
    }

    // ---------- Content tabs ----------

    @Override
    public TourContentDto upsertContent(Long tourId, TourContentDto dto) {
        Tour tour = requireTour(tourId);
        TourContentType type = TourContentType.valueOf(dto.getContentType());
        String language = dto.getLanguageCode() == null ? "en" : dto.getLanguageCode();

        // Genuinely an upsert: a tour has at most one row per tab per language.
        // This previously always inserted, so re-saving a tab left the old row
        // behind and the tour page rendered that tab twice.
        TourContent entity = tourContentRepository
                .findFirstByTour_TourIdAndContentTypeAndLanguageCode(tourId, type, language)
                .orElseGet(TourContent::new);

        entity.setTour(tour);
        entity.setContentType(type);
        entity.setLanguageCode(language);
        entity.setContentText(dto.getContentText());
        entity.setStatus(true);
        return toDto(tourContentRepository.save(entity));
    }

    @Override
    public List<TourContentDto> getContent(Long tourId) {
        return tourContentRepository.findByTour_TourIdAndStatusTrue(tourId).stream()
                .map(this::toDto).toList();
    }

    @Override
    public void deleteContent(Long tourId, Long tourContentId) {
        TourContent existing = tourContentRepository.findById(tourContentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour content not found: " + tourContentId));
        if (!existing.getTour().getTourId().equals(tourId)) {
            throw new ResourceNotFoundException("Tour content not found for this tour");
        }
        // Soft delete, consistent with deleteMedia/deleteAddon and with
        // getContent only returning status=true rows.
        existing.setStatus(false);
        tourContentRepository.save(existing);
    }

    private TourContentDto toDto(TourContent e) {
        TourContentDto d = new TourContentDto();
        d.setTourContentId(e.getTourContentId());
        d.setContentType(e.getContentType().name());
        d.setLanguageCode(e.getLanguageCode());
        d.setContentText(e.getContentText());
        return d;
    }

    // ---------- Media ----------

    @Override
    public TourMediaDto addMedia(Long tourId, TourMediaDto dto) {
        Tour tour = requireTour(tourId);
        TourMedia entity = new TourMedia();
        entity.setTour(tour);
        entity.setMediaType(MediaType.valueOf(dto.getMediaType()));
        entity.setFilePath(dto.getFilePath());
        entity.setMimeType(dto.getMimeType());
        entity.setTabContext(dto.getTabContext() == null ? TabContext.GALLERY : TabContext.valueOf(dto.getTabContext()));
        entity.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        entity.setStatus(true);
        return toDto(tourMediaRepository.save(entity));
    }

    @Override
    public List<TourMediaDto> getMedia(Long tourId) {
        return tourMediaRepository.findByTour_TourIdAndStatusTrue(tourId).stream()
                .map(this::toDto).toList();
    }

    @Override
    public void deleteMedia(Long tourId, Long mediaId) {
        TourMedia media = tourMediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));
        if (!media.getTour().getTourId().equals(tourId)) {
            throw new ResourceNotFoundException("Media not found for this tour");
        }
        // Soft delete, consistent with the rest of the schema's status flags.
        media.setStatus(false);
        tourMediaRepository.save(media);
    }

    private TourMediaDto toDto(TourMedia e) {
        TourMediaDto d = new TourMediaDto();
        d.setMediaId(e.getMediaId());
        d.setMediaType(e.getMediaType().name());
        d.setFilePath(e.getFilePath());
        d.setMimeType(e.getMimeType());
        d.setTabContext(e.getTabContext().name());
        d.setDisplayOrder(e.getDisplayOrder());
        return d;
    }

    // ---------- Add-ons ----------

    @Override
    public TourAddonDto addAddon(Long tourId, TourAddonDto dto) {
        Tour tour = requireTour(tourId);
        TourAddon entity = new TourAddon();
        entity.setTour(tour);
        entity.setAddonName(dto.getAddonName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setPriceType(dto.getPriceType() == null ? PriceType.PER_PERSON : PriceType.valueOf(dto.getPriceType()));
        entity.setIsOptional(dto.getIsOptional() == null ? true : dto.getIsOptional());
        entity.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        entity.setStatus(true);
        return toDto(tourAddonRepository.save(entity));
    }

    @Override
    public List<TourAddonDto> getAddons(Long tourId) {
        return tourAddonRepository.findByTour_TourIdAndStatusTrue(tourId).stream()
                .map(this::toDto).toList();
    }

    private TourAddonDto toDto(TourAddon e) {
        TourAddonDto d = new TourAddonDto();
        d.setAddonId(e.getAddonId());
        d.setAddonName(e.getAddonName());
        d.setDescription(e.getDescription());
        d.setPrice(e.getPrice());
        d.setPriceType(e.getPriceType().name());
        d.setIsOptional(e.getIsOptional());
        d.setDisplayOrder(e.getDisplayOrder());
        return d;
    }

    @Override
    public TourAddonDto updateAddon(Long tourId, Long addonId, TourAddonDto dto) {
        TourAddon addon = requireOwnedAddon(tourId, addonId);
        addon.setAddonName(dto.getAddonName());
        addon.setDescription(dto.getDescription());
        addon.setPrice(dto.getPrice());
        addon.setPriceType(dto.getPriceType() == null ? PriceType.PER_PERSON : PriceType.valueOf(dto.getPriceType()));
        addon.setIsOptional(dto.getIsOptional() == null ? true : dto.getIsOptional());
        addon.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        return toDto(tourAddonRepository.save(addon));
    }

    @Override
    public void deleteAddon(Long tourId, Long addonId) {
        TourAddon addon = requireOwnedAddon(tourId, addonId);
        // Soft delete, consistent with deleteMedia and with getAddons only
        // returning status=true rows.
        addon.setStatus(false);
        tourAddonRepository.save(addon);
    }

    private TourAddon requireOwnedAddon(Long tourId, Long addonId) {
        TourAddon addon = tourAddonRepository.findById(addonId)
                .orElseThrow(() -> new ResourceNotFoundException("Add-on not found"));
        if (!addon.getTour().getTourId().equals(tourId)) {
            throw new ResourceNotFoundException("Add-on not found for this tour");
        }
        return addon;
    }

    // ---------- Itinerary ----------

    @Override
    public ItineraryDto addItineraryDay(Long tourId, ItineraryDto dto) {
        Tour tour = requireTour(tourId);
        Itinerary entity = new Itinerary();
        entity.setTour(tour);
        entity.setDayNumber(dto.getDayNumber());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        return toDto(itineraryRepository.save(entity));
    }

    @Override
    public List<ItineraryDto> getItinerary(Long tourId) {
        return itineraryRepository.findByTour_TourIdOrderByDayNumberAsc(tourId).stream()
                .map(this::toDto).toList();
    }

    @Override
    public ItineraryDto updateItineraryDay(Long tourId, Long itineraryId, ItineraryDto dto) {
        Itinerary existing = requireOwnedItineraryDay(tourId, itineraryId);
        existing.setDayNumber(dto.getDayNumber());
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        return toDto(itineraryRepository.save(existing));
    }

    @Override
    public void deleteItineraryDay(Long tourId, Long itineraryId) {
        Itinerary existing = requireOwnedItineraryDay(tourId, itineraryId);
        itineraryRepository.delete(existing);
    }

    private Itinerary requireOwnedItineraryDay(Long tourId, Long itineraryId) {
        Itinerary existing = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary day not found: " + itineraryId));
        if (!existing.getTour().getTourId().equals(tourId)) {
            throw new ResourceNotFoundException("Itinerary day not found for this tour");
        }
        return existing;
    }

    private ItineraryDto toDto(Itinerary e) {
        ItineraryDto d = new ItineraryDto();
        d.setItineraryId(e.getItineraryId());
        d.setDayNumber(e.getDayNumber());
        d.setTitle(e.getTitle());
        d.setDescription(e.getDescription());
        return d;
    }
}
