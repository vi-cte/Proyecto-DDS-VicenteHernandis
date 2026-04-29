package com.votify.backend.repository;

import com.votify.backend.entity.EventSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// Repositorio JPA para acceder a la configuración singleton del evento.
public interface EventSettingsRepository extends JpaRepository<EventSettingsEntity, Long> {
}
