package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import utils.Constants.FineScheme;

public class Fine {
    private int fineId;
    private String licensePlate;
    private String fineType;
    private double amount;
    private LocalDateTime issueTime;
    private boolean paid;
    private int overstayHours;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    
    // Constructor for new fines
    public Fine(String licensePlate, String fineType, double amount, int overstayHours) {
        this.licensePlate = licensePlate;
        this.fineType = fineType;
        this.amount = amount;
        this.overstayHours = overstayHours;
        this.issueTime = LocalDateTime.now();
        this.paid = false;
        this.paymentMethod = null;
        this.paymentTime = null;
    }
    
    // Calculate fine based on scheme and overstay hours
    public static double calculateFine(FineScheme scheme, int overstayHours) {
        // Only calculate fine if overstay hours > 0
        if (overstayHours <= 0) {
            return 0.0;
        }
        
        switch (scheme) {
            case FIXED:
                // Option A: Fixed Fine Scheme - Flat RM 50
                return 50.0;
                
            case PROGRESSIVE:
                // Option B: Progressive Fine Scheme
                if (overstayHours <= 24) {
                    return 50.0; // First 24 hours overstay: RM 50
                } else if (overstayHours <= 48) {
                    return 150.0; // Hours 24-48 overstay: Additional RM 100 (total 150)
                } else if (overstayHours <= 72) {
                    return 300.0; // Hours 48-72 overstay: Additional RM 150 (total 300)
                } else {
                    return 500.0; // Above 72 hours overstay: Additional RM 200 (total 500)
                }
                
            case HOURLY:
                // Option C: Hourly Fine Scheme - RM 20 per hour overstay
                return 20.0 * overstayHours;
                
            default:
                return 0.0;
        }
    }
    
    // Create overstay fine (only when hours > 24)
    public static Fine createOverstayFine(String licensePlate, FineScheme scheme, long parkedHours) {
        if (parkedHours <= 24) {
            return null; // No fine for 24 hours or less
        }
        
        int overstayHours = (int) (parkedHours - 24);
        double amount = calculateFine(scheme, overstayHours);
        String fineType = "Overstay Fine (" + overstayHours + " hours overstay)";
        
        return new Fine(licensePlate, fineType, amount, overstayHours);
    }
    
    // Mark fine as paid
    public void markAsPaid(String paymentMethod) {
        this.paid = true;
        this.paymentMethod = paymentMethod;
        this.paymentTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getFineId() {
        return fineId;
    }
    
    public void setFineId(int fineId) {
        this.fineId = fineId;
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
    
    public String getFineType() {
        return fineType;
    }
    
    public void setFineType(String fineType) {
        this.fineType = fineType;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public LocalDateTime getIssueTime() {
        return issueTime;
    }
    
    public void setIssueTime(LocalDateTime issueTime) {
        this.issueTime = issueTime;
    }
    
    public boolean isPaid() {
        return paid;
    }
    
    public void setPaid(boolean paid) {
        this.paid = paid;
        if (paid && paymentTime == null) {
            this.paymentTime = LocalDateTime.now();
        }
    }
    
    public int getOverstayHours() {
        return overstayHours;
    }
    
    public void setOverstayHours(int overstayHours) {
        this.overstayHours = overstayHours;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }
    
    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }
    
    public String getFormattedIssueTime() {
        return issueTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public String getFormattedPaymentTime() {
        if (paymentTime == null) return "Not Paid";
        return paymentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    @Override
    public String toString() {
        return String.format("Fine{id=%d, plate='%s', type='%s', amount=RM%.2f, paid=%s, overstay=%d hrs}",
                fineId, licensePlate, fineType, amount, paid ? "Yes" : "No", overstayHours);
    }
}