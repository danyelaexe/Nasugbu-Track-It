package com.example.retailbusinessandservicecoveragemapper;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Objects;

/**
 * Branch extends MapEntity and contains a CoverageArea.
 * Demonstrates INHERITANCE, POLYMORPHISM, ENCAPSULATION, and OBJECT RELATIONSHIP.
 */
public class Branch extends MapEntity { // ===== INHERITANCE: Branch extends MapEntity =====

    // ==========================================
    // ===== ENCAPSULATION: Private fields =====
    // ==========================================
    private long id;
    private String name;
    private String address;
    private String contact;
    private String category;
    private double coverageRadiusKm;

    // ========================================================
    // ===== OBJECT RELATIONSHIP (Composition): Branch has-a CoverageArea =====
    // ========================================================
    private CoverageArea coverageArea;

    public Branch(long id, String name, String address, String contact, String category, double latitude, double longitude, double coverageRadiusKm) {
        super(latitude, longitude); // INHERITANCE: calling super constructor
        this.id = id;
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.category = category;
        this.coverageRadiusKm = coverageRadiusKm;
        this.coverageArea = new CoverageArea(coverageRadiusKm); // OBJECT RELATIONSHIP
    }

    // ==========================================
    // ===== POLYMORPHISM: Overriding methods =====
    // ==========================================
    @Override
    public String getEntityName() {
        return name;
    }

    @Override
    public String getCategoryName() {
        return category;
    }

    // ==========================================
    // ===== ENCAPSULATION: Getters & Setters =====
    // ==========================================
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getCoverageRadiusKm() { return coverageRadiusKm; }
    public void setCoverageRadiusKm(double coverageRadiusKm) {
        this.coverageRadiusKm = coverageRadiusKm;
        if (this.coverageArea != null) {
            this.coverageArea.setRadiusKm(coverageRadiusKm);
        }
    }

    public CoverageArea getCoverageArea() { return coverageArea; }
    public void setCoverageArea(CoverageArea coverageArea) { this.coverageArea = coverageArea; }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("address", address);
        json.put("contact", contact);
        json.put("category", category != null ? category : "Grocery");
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
                json.optString("category", "Grocery"),
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
