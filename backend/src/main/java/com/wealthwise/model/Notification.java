package com.wealthwise.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    public enum NotificationType {
        BUDGET_EXCEEDED,
        GOAL_REACHED,
        INCOME_DETECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 512)
    private String message;

    /** Prevents duplicate notifications for the same event.
     *  Format examples: "BUDGET_EXCEEDED-2026-02", "GOAL_REACHED-3", "INCOME_DETECTED-2026-02" */
    @Column(nullable = false)
    private String refKey;

    @Column(nullable = false)
    private boolean read = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Notification() {}

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // --- Getters ---
    public Long             getId()        { return id; }
    public User             getUser()      { return user; }
    public NotificationType getType()      { return type; }
    public String           getTitle()     { return title; }
    public String           getMessage()   { return message; }
    public String           getRefKey()    { return refKey; }
    public boolean          isRead()       { return read; }
    public LocalDateTime    getCreatedAt() { return createdAt; }

    // --- Setters ---
    public void setId(Long id)                    { this.id = id; }
    public void setUser(User user)                { this.user = user; }
    public void setType(NotificationType type)    { this.type = type; }
    public void setTitle(String title)            { this.title = title; }
    public void setMessage(String message)        { this.message = message; }
    public void setRefKey(String refKey)          { this.refKey = refKey; }
    public void setRead(boolean read)             { this.read = read; }
}
