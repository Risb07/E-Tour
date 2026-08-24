package com.etour.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entity.NavMenuItem;

public interface NavMenuItemRepository extends JpaRepository<NavMenuItem, Long> {

    List<NavMenuItem> findByActiveTrueAndParentItemIsNullOrderBySortOrderAsc();

    List<NavMenuItem> findAllByOrderBySortOrderAsc();

    /**
     * All active items in one query - parents and children together. The
     * caller builds the tree in memory, which avoids both the N+1 of loading
     * each parent's children separately and the lazy-loading fragility of
     * serialising the entity's `children` collection directly.
     */
    List<NavMenuItem> findByActiveTrueOrderBySortOrderAsc();
}
