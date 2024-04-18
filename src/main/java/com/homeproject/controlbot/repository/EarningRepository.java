package com.homeproject.controlbot.repository;

import com.homeproject.controlbot.entity.Earning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EarningRepository extends JpaRepository<Earning, Long> {
}
