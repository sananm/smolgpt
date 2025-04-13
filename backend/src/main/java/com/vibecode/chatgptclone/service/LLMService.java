package com.vibecode.chatgptclone.service;

import com.vibecode.chatgptclone.model.Chat;
import com.vibecode.chatgptclone.model.Message;

import java.util.List;

public interface LLMService {
    String generateResponse(String prompt, String context);
    void updateContext(Chat chat, List<Message> messages);
    String getContext(Chat chat);
} 