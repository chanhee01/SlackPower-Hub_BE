package com.example.IotController.domain.Device.repository;

import com.example.IotController.domain.Device.model.Plugs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlugRepository extends JpaRepository<Plugs, Long> {
    List<Plugs> findByModeTrue();
    List<Plugs> findByModeFalse();
}
