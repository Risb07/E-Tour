package com.etour.notification.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.etour.notification.domain.Notification;
import com.etour.notification.domain.NotificationStatus;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByIdempotencyKey(String idempotencyKey);

    Page<Notification> findByStatusOrderByCreatedAtDesc(NotificationStatus status, Pageable pageable);

    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(NotificationStatus status);

    /**
     * Claims the next batch of due rows.
     *
     * <p>{@code SKIP LOCKED} rather than a plain pessimistic lock: with more
     * than one instance of this service running, a plain lock makes the second
     * worker block until the first commits, so the two serialise and the second
     * adds nothing. Skipping locked rows lets each worker take a disjoint slice
     * and send in parallel, and it is also what guarantees a message is never
     * picked up twice and therefore never sent twice.
     */
    @Query(value = """
            SELECT * FROM notification
            WHERE status = 'PENDING'
              AND (next_attempt_at IS NULL OR next_attempt_at <= :now)
            ORDER BY created_at
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Notification> claimDueBatch(@Param("now") Instant now, @Param("batchSize") int batchSize);
}
