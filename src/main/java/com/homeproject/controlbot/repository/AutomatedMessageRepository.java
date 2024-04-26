package com.homeproject.controlbot.repository;

import com.homeproject.controlbot.entity.AutomatedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutomatedMessageRepository extends JpaRepository<AutomatedMessage, Long> {
}
