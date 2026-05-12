package com.blaise.SPTTS.CONTROLLERS;

import com.blaise.SPTTS.SERVICES.ChatService;
import com.blaise.SPTTS.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/{roomId}/sendMessage")
    public void sendMessageToRoom(@Payload ChatMessage chatMessage, @DestinationVariable String roomId) {
        chatMessage.setChatId(roomId);
        ChatMessage saved = chatService.save(chatMessage);
        messagingTemplate.convertAndSend("/topic/room/" + roomId, saved);
    }

    @GetMapping("/messages/{roomId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> findChatMessages(@PathVariable String roomId) {
        return ResponseEntity.ok(chatService.findByChatId(roomId));
    }
}
