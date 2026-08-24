package com.etour.enums;

/**
 * Lifecycle of a contact enquiry submitted from the public Contact page.
 *
 * NEW is the state every enquiry starts in, so an unattended inbox is
 * obvious at a glance. The admin module moves an enquiry forward; nothing
 * transitions automatically.
 */
public enum EnquiryStatus {

    /** Just submitted, nobody has looked at it yet. */
    NEW,

    /** An agent has read it and is working on a reply. */
    IN_PROGRESS,

    /** Answered and closed. */
    RESOLVED
}
