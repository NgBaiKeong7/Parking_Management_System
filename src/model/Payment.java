package model;

import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private String licensePlate;
    private LocalDateTime paymentTime;
    private double amount;
    private String paymentMethod;
    private double fineAmount;
    private double parkingFee;
    
    public Payment(String licensePlate, double amount, String paymentMethod, 
                   double fineAmount, double parkingFee) {
        this.licensePlate = licensePlate;
        this.paymentTime = LocalDateTime.now();
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.fineAmount = fineAmount;
        this.parkingFee = parkingFee;
    }
    
    // Getters and setters
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public String getLicensePlate() { return licensePlate; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getFineAmount() { return fineAmount; }
    public double getParkingFee() { return parkingFee; }
}