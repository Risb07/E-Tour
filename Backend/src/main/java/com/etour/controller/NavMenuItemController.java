package com.etour.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etour.dto.NavMenuItemDto;
import com.etour.entity.NavMenuItem;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.NavMenuItemRepository;

import jakarta.validation.Valid;

/**
 * BRD 2.1 general reqs - top menu bar navigation, database driven, with
 * mouse-over/multi-level support via parent/child items. Read endpoints are
 * public.
 */
@RestController
@RequestMapping("/api/nav-menu")
public class NavMenuItemController {

    private final NavMenuItemRepository navMenuItemRepository;

    public NavMenuItemController(NavMenuItemRepository navMenuItemRepository) {
        this.navMenuItemRepository = navMenuItemRepository;
    }

    /**
     * Public menu tree: active top-level items, each with its active children
     * sorted by sort_order.
     *
     * Built from ONE query and assembled in memory. Previously this returned
     * the entity and relied on lazy-loading `children` during serialisation,
     * which produced empty dropdowns whenever the session had closed, and
     * included retired children when it hadn't.
     *
     * With includeInactive=true (admin screen) the flat list of entities is
     * returned instead, since admins need to see and re-enable hidden rows.
     */
    @GetMapping
    public ResponseEntity<?> getTopLevelItems(
            @RequestParam(defaultValue = "false") boolean includeInactive) {

        if (includeInactive) {
            return ResponseEntity.ok(navMenuItemRepository.findAllByOrderBySortOrderAsc());
        }

        List<NavMenuItem> active = navMenuItemRepository.findByActiveTrueOrderBySortOrderAsc();

        Map<Long, List<NavMenuItemDto>> childrenByParent = new LinkedHashMap<>();
        for (NavMenuItem item : active) {
            if (item.getParentItem() == null) continue;
            childrenByParent
                    .computeIfAbsent(item.getParentItem().getNavMenuItemId(), k -> new ArrayList<>())
                    .add(toDto(item, List.of()));
        }

        List<NavMenuItemDto> tree = active.stream()
                .filter(item -> item.getParentItem() == null)
                .map(item -> toDto(item,
                        childrenByParent.getOrDefault(item.getNavMenuItemId(), List.of())))
                .toList();

        return ResponseEntity.ok(tree);
    }

    private NavMenuItemDto toDto(NavMenuItem item, List<NavMenuItemDto> children) {
        return new NavMenuItemDto(item.getNavMenuItemId(), item.getLabel(), item.getLink(),
                item.getSortOrder(), children);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NavMenuItem> getItemById(@PathVariable Long id) {
        NavMenuItem item = navMenuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nav menu item not found with id : " + id));
        return ResponseEntity.ok(item);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<NavMenuItem> createItem(@Valid @RequestBody NavMenuItem item) {
        if (item.getParentItem() != null && item.getParentItem().getNavMenuItemId() != null) {
            NavMenuItem parent = navMenuItemRepository.findById(item.getParentItem().getNavMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent menu item not found"));
            item.setParentItem(parent);
        } else {
            item.setParentItem(null);
        }
        return new ResponseEntity<>(navMenuItemRepository.save(item), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<NavMenuItem> updateItem(@PathVariable Long id, @Valid @RequestBody NavMenuItem request) {
        NavMenuItem existing = navMenuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nav menu item not found with id : " + id));
        if (request.getParentItem() != null && request.getParentItem().getNavMenuItemId() != null) {
            NavMenuItem parent = navMenuItemRepository.findById(request.getParentItem().getNavMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent menu item not found"));
            existing.setParentItem(parent);
        } else {
            existing.setParentItem(null);
        }
        existing.setLabel(request.getLabel());
        existing.setLink(request.getLink());
        existing.setSortOrder(request.getSortOrder());
        existing.setActive(request.getActive());
        return ResponseEntity.ok(navMenuItemRepository.save(existing));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable Long id) {
        NavMenuItem item = navMenuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nav menu item not found with id : " + id));
        navMenuItemRepository.delete(item);
        return ResponseEntity.ok("Nav menu item deleted successfully");
    }
}
