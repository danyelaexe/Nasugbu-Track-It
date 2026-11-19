package com.example.retailbusinessandservicecoveragemapper;

/**
 * Represents the service coverage area associated with a branch.
 * Part of OBJECT RELATIONSHIP (Composition).
 */
public class CoverageArea {

    // ==========================================
    // ===== ENCAPSULATION: Private fields =====
    // ==========================================
    private double radiusKm;
    private String coverageType;

    public CoverageArea(double radiusKm) {
        this.radiusKm = radiusKm;
        this.coverageType = "Standard Delivery Radius";
    }

    // ==========================================
    // ===== ENCAPSULATION: Getters & Setters =====
    // ==========================================
    public double getRadiusKm() { return radiusKm; }
    public void setRadiusKm(double radiusKm) { this.radiusKm = radiusKm; }

    public String getCoverageType() { return coverageType; }
    public void setCoverageType(String coverageType) { this.coverageType = coverageType; }

    public double getRadiusMeters() {
        return radiusKm * 1000.0;
    }
}
