package com.repobackend.api.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.model.Factura;

public interface FacturaRepository extends MongoRepository<Factura, String> {
    Factura findByNumeroFactura(String numeroFactura);
    List<Factura> findByRealizadoPor(String userId);
    List<Factura> findByClienteId(String clienteId);
}
