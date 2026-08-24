package com.etour.service;

import com.etour.dto.MultipathPreviewResponse;

public interface MultipathGeneratorService {

    /** Dry run - computes what would change without writing anything. */
    MultipathPreviewResponse preview();

    /**
     * Applies every active rule. Idempotent: links that already exist are
     * skipped, so re-running after adding tours only adds what is missing.
     */
    MultipathPreviewResponse apply();
}
