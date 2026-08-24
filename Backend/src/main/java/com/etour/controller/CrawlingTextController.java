package com.etour.controller;

import java.util.List;

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

import com.etour.entity.CrawlingText;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.CrawlingTextRepository;

import jakarta.validation.Valid;

/**
 * BRD 3.2 - Home page crawling (scrolling) ticker text. Text is managed in the
 * database, multiple items, ordered. Read endpoints are public.
 */
@RestController
@RequestMapping("/api/crawling-text")
public class CrawlingTextController {

    private final CrawlingTextRepository crawlingTextRepository;

    public CrawlingTextController(CrawlingTextRepository crawlingTextRepository) {
        this.crawlingTextRepository = crawlingTextRepository;
    }

    @GetMapping
    public ResponseEntity<List<CrawlingText>> getActiveTexts(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        if (includeInactive) {
            return ResponseEntity.ok(crawlingTextRepository.findAll());
        }
        return ResponseEntity.ok(crawlingTextRepository.findByIsActiveTrueOrderBySortOrderAsc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrawlingText> getTextById(@PathVariable Long id) {
        CrawlingText text = crawlingTextRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crawling text not found with id : " + id));
        return ResponseEntity.ok(text);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CrawlingText> createText(@Valid @RequestBody CrawlingText text) {
        return new ResponseEntity<>(crawlingTextRepository.save(text), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CrawlingText> updateText(@PathVariable Long id, @Valid @RequestBody CrawlingText request) {
        CrawlingText existing = crawlingTextRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crawling text not found with id : " + id));
        existing.setText(request.getText());
        existing.setSortOrder(request.getSortOrder());
        existing.setIsActive(request.getIsActive());
        return ResponseEntity.ok(crawlingTextRepository.save(existing));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteText(@PathVariable Long id) {
        CrawlingText text = crawlingTextRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crawling text not found with id : " + id));
        crawlingTextRepository.delete(text);
        return ResponseEntity.ok("Crawling text deleted successfully");
    }
}
