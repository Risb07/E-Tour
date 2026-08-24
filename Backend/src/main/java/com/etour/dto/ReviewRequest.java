package com.etour.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for a customer's own review. Deliberately carries ONLY the
 * fields a customer is allowed to set - the tour comes from the URL and the
 * customer from the JWT principal, so neither can be spoofed via the body.
 *
 * The Review entity can't be used as the request type here: it has @NotNull
 * customer/tour fields that the client must not supply, which forced those
 * endpoints to skip @Valid entirely and left rating/comment unvalidated.
 */
public class ReviewRequest {

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "rating cannot be below 1")
    @Max(value = 5, message = "rating cannot be above 5")
    private Integer rating;

    @NotBlank(message = "comment cannot be blank")
    @Size(max = 2000, message = "comment cannot exceed 2000 characters")
    private String comment;

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
