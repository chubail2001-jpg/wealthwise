package com.wealthwise.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    private String fullName;

    @Email
    @Column(unique = true)
    private String email;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public User() {}

    public User(String username, String password, String fullName, String email) {
        this.username  = username;
        this.password  = password;
        this.fullName  = fullName;
        this.email     = email;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long          getId()        { return id; }
    public String        getUsername()  { return username; }
    public String        getPassword()  { return password; }
    public String        getFullName()  { return fullName; }
    public String        getEmail()     { return email; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id)               { this.id = id; }
    public void setUsername(String u)        { this.username = u; }
    public void setPassword(String p)        { this.password = p; }
    public void setFullName(String n)        { this.fullName = n; }
    public void setEmail(String e)           { this.email = e; }
}
