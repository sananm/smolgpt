package com.vibecode.chatgptclone.repository;

import com.vibecode.chatgptclone.model.Chat;
import com.vibecode.chatgptclone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUser(User user);
    List<Chat> findByUserOrderByCreatedAtDesc(User user);
    Optional<Chat> findByIdAndUser_Id(Long id, Long userId);
} 