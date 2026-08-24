package com.etour.enums;

/** Comparison used by a multipath rule. */
public enum RuleOperator {
    EQUALS,
    CONTAINS,
    GREATER_THAN,
    LESS_THAN,
    /** Inclusive range - uses both matchValue and matchValueTo. */
    BETWEEN,
    /** No comparison; pairs with RuleMatchField.ALL. */
    ANY
}
