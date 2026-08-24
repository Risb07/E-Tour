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

import com.etour.entity.Content;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.ContentRepository;

import jakarta.validation.Valid;

/**
 * BRD 2.1 - database-driven site content with multilingual support
 * (language_code; English default). All page labels/messages/text are managed
 * here rather than hard-coded. Read endpoints are public.
 */
@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final ContentRepository contentRepository;

    public ContentController(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    /**
     * All active site content, optionally narrowed by language and page.
     *
     * Null-safety matters here: page_name and language_code are both nullable
     * columns, so calling equalsIgnoreCase() on the entity value would throw a
     * NullPointerException (500) as soon as one row had either left blank.
     * The comparison is done from the parameter side instead.
     *
     * Inactive rows (status = false) are excluded so admins can retire a piece
     * of content without deleting it.
     */
    @GetMapping
    public ResponseEntity<List<Content>> getContent(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String pageName,
            @RequestParam(defaultValue = "false") boolean includeInactive) {

        List<Content> all = contentRepository.findAll();
        return ResponseEntity.ok(all.stream()
                .filter(c -> includeInactive || !Boolean.FALSE.equals(c.getStatus()))
                .filter(c -> language == null || language.isBlank()
                        || language.equalsIgnoreCase(c.getLanguageCode()))
                .filter(c -> pageName == null || pageName.isBlank()
                        || pageName.equalsIgnoreCase(c.getPageName()))
                .sorted(java.util.Comparator.comparing(
                        c -> c.getDisplayOrder() == null ? 0 : c.getDisplayOrder()))
                .toList());
    }

    @GetMapping("/{key}")
    public ResponseEntity<Content> getContentByKey(
            @PathVariable String key,
            @RequestParam(defaultValue = "en") String language) {
        Content content = contentRepository.findByContentKeyAndLanguageCode(key, language)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Content not found for key : " + key + " / language : " + language));
        return ResponseEntity.ok(content);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Content> createContent(@Valid @RequestBody Content content) {
        return new ResponseEntity<>(contentRepository.save(content), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Content> updateContent(@PathVariable Long id, @Valid @RequestBody Content request) {
        Content existing = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id : " + id));
        existing.setContentKey(request.getContentKey());
        existing.setPageName(request.getPageName());
        existing.setLanguageCode(request.getLanguageCode());
        existing.setContentValue(request.getContentValue());
        existing.setMediaUrl(request.getMediaUrl());
        existing.setLinkUrl(request.getLinkUrl());
        existing.setDisplayOrder(request.getDisplayOrder());
        existing.setStatus(request.getStatus());
        return ResponseEntity.ok(contentRepository.save(existing));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContent(@PathVariable Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id : " + id));
        contentRepository.delete(content);
        return ResponseEntity.ok("Content deleted successfully");
    }
}
