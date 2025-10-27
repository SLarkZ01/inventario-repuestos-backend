package com.repobackend.api.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.model.Movimiento;

public interface MovimientoRepository extends MongoRepository<Movimiento, String> {
    List<Movimiento> findByProductoId(String productoId);
    List<Movimiento> findByTipo(String tipo);
}
