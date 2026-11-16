package com.example.retailbusinessandservicecoveragemapper;

import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Model class representing a Retail Branch in Nasugbu.
 * Implemented in Java to support Java + Kotlin hybrid tech stack.
 */
public class Branch {
    private long id;
    private String name;
    private String address;
    private String contact;
    private String category;
    private double latitude;
    private double longitude;
    private double coverageRadiusKm;

    public Branch(long id, String name, String address, String contact, String category, double latitude, double longitude, double coverageRadiusKm) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.coverageRadiusKm = coverageRadiusKm;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getCoverageRadiusKm() {
        return coverageRadiusKm;
    }

    public void setCoverageRadiusKm(double coverageRadiusKm) {
        this.coverageRadiusKm = coverageRadiusKm;
    }

    public LatLng getLocation() {
        return new LatLng(latitude, longitude);
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("address", address);
        json.put("contact", contact);
        json.put("category", category != null ? category : "General Retail");
        json.put("latitude", latitude);
        json.put("longitude", longitude);
        json.put("coverageRadiusKm", coverageRadiusKm > 0 ? coverageRadiusKm : 2.0);
        return json;
    }

    public static Branch fromJSONObject(JSONObject json) throws JSONException {
        return new Branch(
                json.getLong("id"),
                json.getString("name"),
                json.getString("address"),
                json.optString("contact", ""),
                json.optString("category", "General Retail"),
                json.getDouble("latitude"),
                json.getDouble("longitude"),
                json.optDouble("coverageRadiusKm", 2.0)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return id == branch.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
