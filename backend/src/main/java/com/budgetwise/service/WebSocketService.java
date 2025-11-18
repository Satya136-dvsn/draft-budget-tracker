package com.budgetwise.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendDashboardUpdate(Long userId) {
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/dashboard", "refresh");
    }

    public void sendBudgetAlert(Long userId, String message) {
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/alerts", message);
    }

    public void sendNotification(Long userId, String notification) {
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/notifications", notification);
    }
}
