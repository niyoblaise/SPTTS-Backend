package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.entity.ChatMessage;
import com.blaise.SPTTS.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository repository;

    public ChatMessage save(ChatMessage message) {
        message.setTimestamp(java.time.Instant.now());
        return repository.save(message);
    }

    public List<ChatMessage> findByChatId(String chatId) {
        return repository.findByChatIdOrderByTimestampAsc(chatId);
    }
}
