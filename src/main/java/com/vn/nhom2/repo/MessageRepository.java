package com.vn.nhom2.repo;

import com.vn.nhom2.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
    List<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = ?1")
    Long countMessagesByConversationId(Long conversationId);
    void deleteByConversationId(Long conversationId);
}