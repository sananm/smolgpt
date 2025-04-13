package com.vibecode.chatgptclone.service.impl;

import com.vibecode.chatgptclone.model.Chat;
import com.vibecode.chatgptclone.model.Message;
import com.vibecode.chatgptclone.service.LLMService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocalLLMService implements LLMService {

    @Value("${llm.model.path}")
    private String modelPath;

    @Value("${llm.context.window}")
    private int contextWindow;

    @Value("${llm.temperature}")
    private double temperature;

    @Override
    public String generateResponse(String prompt, String context) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python",
                "llm_inference.py",
                "--model", modelPath,
                "--prompt", prompt,
                "--context", context,
                "--temperature", String.valueOf(temperature)
            );

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String response = reader.lines().collect(Collectors.joining("\n"));
            process.waitFor();
            
            return response;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to generate response from LLM", e);
        }
    }

    @Override
    public void updateContext(Chat chat, List<Message> messages) {
        StringBuilder contextBuilder = new StringBuilder();
        
        // Add previous context if exists
        if (chat.getContext() != null) {
            contextBuilder.append(chat.getContext());
        }
        
        // Add new messages to context
        for (Message message : messages) {
            contextBuilder.append("\n")
                    .append(message.isUserMessage() ? "User: " : "Assistant: ")
                    .append(message.getContent());
        }
        
        // Truncate context if it exceeds the window size
        String newContext = contextBuilder.toString();
        if (newContext.length() > contextWindow) {
            newContext = newContext.substring(newContext.length() - contextWindow);
        }
        
        chat.setContext(newContext);
    }

    @Override
    public String getContext(Chat chat) {
        return chat.getContext() != null ? chat.getContext() : "";
    }
} 