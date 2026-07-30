package com.etour.dto.response;

import java.util.List;

import com.etour.entity.Itinerary;
import com.etour.entity.Review;
import com.etour.entity.Tour;
import com.etour.entity.TourSchedule;
import com.etour.dto.ReviewSummary;
import com.etour.dto.response.TourDetailsResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourDetailsResponse {

      private Tour tour;

      private List<TourSchedule> schedules;

      private List<Itinerary> itinerary;

      private List<Review> reviews;

      private ReviewSummary reviewSummary;

}