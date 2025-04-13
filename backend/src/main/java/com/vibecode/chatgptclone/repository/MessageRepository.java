package com.vibecode.chatgptclone.repository;

import com.vibecode.chatgptclone.model.Chat;
import com.vibecode.chatgptclone.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatOrderByCreatedAtAsc(Chat chat);
    List<Message> findByChatAndIsUserMessageOrderByCreatedAtAsc(Chat chat, boolean isUserMessage);
} 