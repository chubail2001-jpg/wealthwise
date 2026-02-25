package com.wealthwise.controller;

import com.wealthwise.dto.NotificationResponse;
import com.wealthwise.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private NotificationService notifService;

    /** Triggers auto-generation then returns all notifications. */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAll() {
        return ResponseEntity.ok(notifService.getAll());
    }

    /** Returns just the unread count (used by the sidebar bell badge). */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        return ResponseEntity.ok(Map.of("count", notifService.getUnreadCount()));
    }

    /** Mark a single notification as read. */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(notifService.markRead(id));
    }

    /** Mark all notifications as read. */
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllRead() {
        notifService.markAllRead();
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }
}
