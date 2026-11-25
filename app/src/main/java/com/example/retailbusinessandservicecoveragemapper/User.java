package com.example.retailbusinessandservicecoveragemapper;

/**
 * User extends AccountEntity, demonstrating Inheritance and Polymorphism.
 */
public class User extends AccountEntity { // ===== INHERITANCE: User extends AccountEntity =====

    // ==========================================
    // ===== ENCAPSULATION: Private fields =====
    // ==========================================
    private long id;
    private String name;
    private String password;
    private String question1;
    private String answer1;
    private String question2;
    private String answer2;

    public User(long id, String name, String username, String password, String role, String question1, String answer1, String question2, String answer2) {
        super(username, role); // INHERITANCE: calling super constructor
        this.id = id;
        this.name = name;
        this.password = password;
        this.question1 = question1 != null ? question1 : "What is the system name?";
        this.answer1 = answer1 != null ? answer1 : "Track-It";
        this.question2 = question2 != null ? question2 : "What municipality is this?";
        this.answer2 = answer2 != null ? answer2 : "Nasugbu";
    }

    public User(String name, String username, String password, String role, String question1, String answer1, String question2, String answer2) {
        this(-1L, name, username, password, role, question1, answer1, question2, answer2);
    }

    public User(long id, String name, String username, String password, String role) {
        this(id, name, username, password, role, "What is the system name?", "Track-It", "What municipality is this?", "Nasugbu");
    }

    public User(String name, String username, String password, String role) {
        this(-1L, name, username, password, role, "What is the system name?", "Track-It", "What municipality is this?", "Nasugbu");
    }

    // ==========================================
    // ===== POLYMORPHISM: Overriding method =====
    // ==========================================
    @Override
    public boolean hasAdminPrivileges() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    // ==========================================
    // ===== ENCAPSULATION: Getters & Setters =====
    // ==========================================
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getQuestion1() { return question1; }
    public void setQuestion1(String question1) { this.question1 = question1; }

    public String getAnswer1() { return answer1; }
    public void setAnswer1(String answer1) { this.answer1 = answer1; }

    public String getQuestion2() { return question2; }
    public void setQuestion2(String question2) { this.question2 = question2; }

    public String getAnswer2() { return answer2; }
    public void setAnswer2(String answer2) { this.answer2 = answer2; }
}
