package com.example.retailbusinessandservicecoveragemapper;

import com.google.android.gms.maps.model.LatLng;

public abstract class MapEntity {

    // ==========================================
    // ===== ENCAPSULATION: Protected fields =====
    // ==========================================
    protected double latitude;
    protected double longitude;

    public MapEntity(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // ==========================================
    // ===== ABSTRACTION: Abstract methods =====
    // ==========================================
    public abstract String getEntityName();
    public abstract String getCategoryName();

    // ==========================================
    // ===== ENCAPSULATION: Getters & Setters =====
    // ==========================================
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        if (latitude >= -90 && latitude <= 90) {
            this.latitude = latitude;
        }
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        if (longitude >= -180 && longitude <= 180) {
            this.longitude = longitude;
        }
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }
}
