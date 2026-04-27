package com.votify.backend.repository;

import com.votify.backend.entity.EventSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventSettingsRepository extends JpaRepository<EventSettingsEntity, Long> {
}
