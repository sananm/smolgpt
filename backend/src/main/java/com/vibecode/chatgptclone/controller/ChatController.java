package com.vibecode.chatgptclone.controller;

import com.vibecode.chatgptclone.model.Chat;
import com.vibecode.chatgptclone.model.Message;
import com.vibecode.chatgptclone.model.User;
import com.vibecode.chatgptclone.repository.ChatRepository;
import com.vibecode.chatgptclone.repository.MessageRepository;
import com.vibecode.chatgptclone.service.LLMService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final LLMService llmService;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public ChatController(
            SimpMessagingTemplate messagingTemplate,
            LLMService llmService,
            ChatRepository chatRepository,
            MessageRepository messageRepository
    ) {
        this.messagingTemplate = messagingTemplate;
        this.llmService = llmService;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Chat chat = chatRepository.findById(chatMessage.getChatId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        // Save user message
        Message userMessage = new Message();
        userMessage.setContent(chatMessage.getContent());
        userMessage.setUserMessage(true);
        userMessage.setChat(chat);
        messageRepository.save(userMessage);

        // Get context and generate response
        String context = llmService.getContext(chat);
        String response = llmService.generateResponse(chatMessage.getContent(), context);

        // Save assistant message
        Message assistantMessage = new Message();
        assistantMessage.setContent(response);
        assistantMessage.setUserMessage(false);
        assistantMessage.setChat(chat);
        messageRepository.save(assistantMessage);

        // Update chat context
        llmService.updateContext(chat, List.of(userMessage, assistantMessage));
        chatRepository.save(chat);

        // Send response back to client
        messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/messages",
                new ChatMessage(chat.getId(), response, false)
        );
    }

    public static class ChatMessage {
        private Long chatId;
        private String content;
        private boolean isUserMessage;

        public ChatMessage() {}

        public ChatMessage(Long chatId, String content, boolean isUserMessage) {
            this.chatId = chatId;
            this.content = content;
            this.isUserMessage = isUserMessage;
        }

        // Getters and setters
        public Long getChatId() {
            return chatId;
        }

        public void setChatId(Long chatId) {
            this.chatId = chatId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isUserMessage() {
            return isUserMessage;
        }

        public void setUserMessage(boolean userMessage) {
            isUserMessage = userMessage;
        }
    }
} 