package com.repobackend.api.stock.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.stock.model.Stock;

public interface StockRepository extends MongoRepository<Stock, String> {
    List<Stock> findByProductoId(String productoId);
    List<Stock> findByProductoIdAndAlmacenIdIn(String productoId, java.util.List<String> almacenIds);
    Stock findByProductoIdAndAlmacenId(String productoId, String almacenId);
}
