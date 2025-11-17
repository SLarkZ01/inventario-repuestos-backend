package com.repobackend.api.factura.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.factura.model.Factura;
import org.bson.types.ObjectId;

public interface FacturaRepository extends MongoRepository<Factura, String> {
    Factura findByNumeroFactura(String numeroFactura);
    List<Factura> findByRealizadoPor(ObjectId userId);
    List<Factura> findByClienteId(String clienteId);
}
