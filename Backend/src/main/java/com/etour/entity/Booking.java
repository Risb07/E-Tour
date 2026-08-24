package com.etour.entity;


import com.etour.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "booking")
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "booking_id")
	private Long bookingId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	@JsonIgnoreProperties({ "user", "hibernateLazyInitializer", "handler" })
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "schedule_id", nullable = false)
	@JsonIgnoreProperties({ "tour", "hibernateLazyInitializer", "handler" })
	private TourSchedule schedule;

	@NotNull
	@Column(name = "booking_date")
	private LocalDate bookingDate;

	@NotNull
	@Column(name = "total_amount")
	private BigDecimal totalAmount;

	@Column(name = "order_number", length = 40)
	private String orderNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "booking_status", nullable = false, length = 20)
	private BookingStatus bookingStatus = BookingStatus.PENDING;

	@Column(name = "number_of_passengers", nullable = false)
	private Integer numberOfPassengers;

	/**
	 * Party composition as chosen on the tour page. Stored so the passenger
	 * step knows how many adult and child forms to render, and so the booking
	 * records what was actually sold rather than re-deriving it from dates of
	 * birth after the fact.
	 *
	 * Null on bookings created before these columns existed; readers fall back
	 * to numberOfPassengers.
	 */
	@Column(name = "adult_count")
	private Integer adultCount;

	@Column(name = "child_count")
	private Integer childCount;

	public Integer getAdultCount() { return adultCount; }
	public void setAdultCount(Integer adultCount) { this.adultCount = adultCount; }
	public Integer getChildCount() { return childCount; }
	public void setChildCount(Integer childCount) { this.childCount = childCount; }

	public Long getBookingId() {
		return bookingId;
	}

	public void setBookingId(Long bookingId) {
		this.bookingId = bookingId;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public TourSchedule getSchedule() {
		return schedule;
	}

	public void setSchedule(TourSchedule schedule) {
		this.schedule = schedule;
	}

	public LocalDate getBookingDate() {
		return bookingDate;
	}

	public void setBookingDate(LocalDate bookingDate) {
		this.bookingDate = bookingDate;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public BookingStatus getBookingStatus() {
		return bookingStatus;
	}

	public void setBookingStatus(BookingStatus bookingStatus) {
		this.bookingStatus = bookingStatus;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public Integer getNumberOfPassengers() {
		return numberOfPassengers;
	}

	public void setNumberOfPassengers(Integer numberOfPassengers) {
		this.numberOfPassengers = numberOfPassengers;
	}

}
