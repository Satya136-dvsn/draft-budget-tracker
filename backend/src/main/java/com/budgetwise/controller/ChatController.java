package com.budgetwise.controller;

import com.budgetwise.dto.ChatRequestDto;
import com.budgetwise.dto.ChatResponseDto;
import com.budgetwise.security.UserPrincipal;
import com.budgetwise.service.ChatAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatAssistantService chatAssistantService;

    @PostMapping
    public ResponseEntity<ChatResponseDto> chat(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody ChatRequestDto request) {
        ChatResponseDto response = chatAssistantService.chat(
                request.getMessage(),
                request.getConversationId(),
                userPrincipal.getId()
        );
        return ResponseEntity.ok(response);
    }
}
