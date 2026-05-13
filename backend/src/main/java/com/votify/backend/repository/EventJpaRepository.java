package com.votify.backend.repository;

import com.votify.backend.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Repositorio JPA para eventos.
public interface EventJpaRepository extends JpaRepository<EventEntity, Long> {
    Optional<EventEntity> findFirstByActiveTrueOrderByIdDesc();
    List<EventEntity> findAllByActiveTrueOrderByEventDateDescIdDesc();
    List<EventEntity> findAllByOrderByActiveDescEventDateDescIdDesc();
}
