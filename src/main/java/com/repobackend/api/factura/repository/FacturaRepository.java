package com.repobackend.api.factura.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.repobackend.api.factura.model.Factura;
import org.bson.types.ObjectId;

public interface FacturaRepository extends MongoRepository<Factura, String> {
    Factura findByNumeroFactura(String numeroFactura);
    List<Factura> findByRealizadoPor(ObjectId userId);
    List<Factura> findByClienteId(String clienteId);

    /**
     * Busca facturas donde el usuario es el cliente O el que la creó.
     * Útil para listar facturas tanto propias como las que fueron creadas para el usuario.
     */
    @Query("{ $or: [ { 'clienteId': ?0 }, { 'realizadoPor': ?1 } ] }")
    List<Factura> findByClienteIdOrRealizadoPor(String clienteId, ObjectId realizadoPor);
}
