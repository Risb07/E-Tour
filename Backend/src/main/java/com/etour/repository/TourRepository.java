package com.etour.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.etour.entity.Tour;
import com.etour.enums.TourCode;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long>, JpaSpecificationExecutor<Tour> {

      List<Tour> findByTourCode(TourCode tourCode);

      Optional<Tour> findByTitleIgnoreCase(String title);

      boolean existsByTitleIgnoreCase(String title);

      /**
       * ACTIVE tour count per category, for the category tiles. One grouped
       * query for every category rather than a count per card.
       * Returns rows of [categoryId, count].
       */
      @org.springframework.data.jpa.repository.Query(
                  "select c.categoryId, count(t) from Tour t join t.categories c "
                              + "where t.status = com.etour.enums.TourStatus.ACTIVE group by c.categoryId")
      java.util.List<Object[]> countActiveToursByCategory();
}
