package com.example.retailbusinessandservicecoveragemapper;

/**
 * Abstract base class representing system accounts.
 * Demonstrates ABSTRACTION.
 */
public abstract class AccountEntity {

    // ==========================================
    // ===== ENCAPSULATION: Protected fields =====
    // ==========================================
    protected String username;
    protected String role;

    public AccountEntity(String username, String role) {
        this.username = username;
        this.role = role;
    }

    // ==========================================
    // ===== ABSTRACTION: Abstract method ======
    // ==========================================
    public abstract boolean hasAdminPrivileges();

    // ==========================================
    // ===== ENCAPSULATION: Getters & Setters =====
    // ==========================================
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
