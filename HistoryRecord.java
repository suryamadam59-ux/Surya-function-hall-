package com.functionhall.model;
/** One row of the Booking & Payment History page (bookings JOIN halls LEFT JOIN payments). */
public class HistoryRecord {
    public String bookingId, customerName, hallName, eventDate, foodType, bookingStatus, paymentMethod, transactionId, paymentDate, paymentStatus;
    public double totalAmount, refundAmount;
}
