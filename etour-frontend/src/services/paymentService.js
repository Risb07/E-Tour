import { httpClient } from "./httpClient";
import { API_ENDPOINTS } from "../constants/apiEndpoints";

/** Non-card payment (existing simulated flow). */
export function payForBooking(payload) {
  return httpClient(API_ENDPOINTS.PAYMENTS.BASE, { method: "POST", body: payload });
}

/**
 * Booking + money breakdown for the payment page. Computed server-side, so
 * the displayed total is exactly what will be charged.
 */
export function fetchPaymentSummary(bookingId) {
  return httpClient(API_ENDPOINTS.PAYMENTS.SUMMARY(bookingId));
}

/**
 * Card payment.
 *
 * The card number and CVV are sent once for this request and are never stored
 * client-side (no localStorage, no context, no logging). The server uses them
 * for the gateway call and keeps only brand + last four.
 *
 * @param {{bookingId:number, paymentMethod:"CREDIT_CARD"|"DEBIT_CARD",
 *   cardNumber:string, cardHolderName:string, expiryMonth:number,
 *   expiryYear:number, cvv:string, saveCard?:boolean}} payload
 */
export function payWithCard(payload) {
  return httpClient(API_ENDPOINTS.PAYMENTS.CARD, { method: "POST", body: payload });
}

export function fetchPayment(paymentId) {
  return httpClient(API_ENDPOINTS.PAYMENTS.BY_ID(paymentId));
}
