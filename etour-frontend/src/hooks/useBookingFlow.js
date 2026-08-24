import { useContext } from "react";
import { BookingFlowContext } from "../context/BookingFlowContext";

export function useBookingFlow() {
  const context = useContext(BookingFlowContext);
  if (!context) {
    throw new Error("useBookingFlow must be used within a BookingFlowProvider");
  }
  return context;
}
