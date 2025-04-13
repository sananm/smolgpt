package com.vibecode.chatgptclone.controller;

import com.vibecode.chatgptclone.model.Chat;
import com.vibecode.chatgptclone.model.Message;
import com.vibecode.chatgptclone.model.User;
import com.vibecode.chatgptclone.repository.ChatRepository;
import com.vibecode.chatgptclone.repository.MessageRepository;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatRestController {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public ChatRestController(ChatRepository chatRepository, MessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    @PostMapping
    public ResponseEntity<Chat> createChat(@RequestBody CreateChatRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        Chat chat = new Chat();
        chat.setTitle(request.getTitle());
        chat.setUser(user);
        
        return ResponseEntity.ok(chatRepository.save(chat));
    }

    @GetMapping
    public ResponseEntity<List<Chat>> getUserChats(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(chatRepository.findByUserOrderByCreatedAtDesc(user));
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<Chat> getChat(@PathVariable Long chatId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return chatRepository.findByIdAndUser_Id(chatId, user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<Message>> getChatMessages(@PathVariable Long chatId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return chatRepository.findByIdAndUser_Id(chatId, user.getId())
                .map(chat -> ResponseEntity.ok(messageRepository.findByChatOrderByCreatedAtAsc(chat)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long chatId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return chatRepository.findByIdAndUser_Id(chatId, user.getId())
                .map(chat -> {
                    chatRepository.delete(chat);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class CreateChatRequest {
        private String title;
    }
} 