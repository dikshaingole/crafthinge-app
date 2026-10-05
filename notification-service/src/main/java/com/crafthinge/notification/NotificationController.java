package com.crafthinge.notification;

import com.crafthinge.common.security.AuthUser;
import com.crafthinge.notification.NotificationHandler.Notification;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    private final NotificationHandler handler;

    public NotificationController(NotificationHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/api/notifications/me")
    public List<Notification> mine() {
        return handler.forCustomer(AuthUser.current().id());
    }
}
