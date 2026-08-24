package com.etour.enums;

/**
 * Which attribute of a Tour a multipath rule inspects.
 *
 * Deliberately limited to fields that already exist on the tour table - a rule
 * engine that can reach arbitrary data becomes impossible to reason about and
 * hard to index.
 */
public enum RuleMatchField {
    /** Tour.tourCode enum: ADV / INT / DEV / DOM. */
    TOUR_CODE,
    /** Case-insensitive substring of Tour.title. */
    TITLE,
    /** Tour.basePrice, numeric comparison. */
    BASE_PRICE,
    /** Tour.durationDays, numeric comparison. */
    DURATION_DAYS,
    /** Matches every ACTIVE tour - useful for a catch-all "All Tours" category. */
    ALL
}
