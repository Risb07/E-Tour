package com.etour.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.etour.entity.Content;

public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByPageNameAndStatusTrue(String pageName);
    Optional<Content> findByContentKeyAndLanguageCode(String contentKey, String languageCode);
}
