package com.wealthwise.dto;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long          id;
    private String        type;
    private String        title;
    private String        message;
    private boolean       read;
    private LocalDateTime createdAt;

    public NotificationResponse() {}

    public NotificationResponse(Long id, String type, String title,
                                String message, boolean read, LocalDateTime createdAt) {
        this.id        = id;
        this.type      = type;
        this.title     = title;
        this.message   = message;
        this.read      = read;
        this.createdAt = createdAt;
    }

    public Long          getId()        { return id; }
    public String        getType()      { return type; }
    public String        getTitle()     { return title; }
    public String        getMessage()   { return message; }
    public boolean       isRead()       { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id)                  { this.id = id; }
    public void setType(String type)            { this.type = type; }
    public void setTitle(String title)          { this.title = title; }
    public void setMessage(String message)      { this.message = message; }
    public void setRead(boolean read)           { this.read = read; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }
}
