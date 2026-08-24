package com.etour.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.etour.entity.TourContent;
import com.etour.enums.TourContentType;

public interface TourContentRepository extends JpaRepository<TourContent, Long> {
    List<TourContent> findByTour_TourIdAndStatusTrue(Long tourId);

    /**
     * One row per tour + tab + language. Used to make saving a "Good to know"
     * tab genuinely an upsert - without this lookup every save inserted a new
     * row and the tour page then rendered the same tab several times over.
     */
    Optional<TourContent> findFirstByTour_TourIdAndContentTypeAndLanguageCode(
            Long tourId, TourContentType contentType, String languageCode);
}
