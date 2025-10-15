package com.repobackend.api.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "productos")
public class Producto {
    @Id
    private String id; // Mongo ObjectId as hex string

    // additional string id separate from _id (matches schema 'id' as string)
    @Field("id")
    private String idString;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private String categoriaId;
    private Integer imagenRecurso;

    // media items: list of objects with idRecurso (int) and tipo (string)
    private List<java.util.Map<String, Object>> listaMedios;

    private Date creadoEn = new Date();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdString() { return idString; }
    public void setIdString(String idString) { this.idString = idString; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getCategoriaId() { return categoriaId; }
    public void setCategoriaId(String categoriaId) { this.categoriaId = categoriaId; }

    public Integer getImagenRecurso() { return imagenRecurso; }
    public void setImagenRecurso(Integer imagenRecurso) { this.imagenRecurso = imagenRecurso; }

    public List<java.util.Map<String, Object>> getListaMedios() { return listaMedios; }
    public void setListaMedios(List<java.util.Map<String, Object>> listaMedios) { this.listaMedios = listaMedios; }

    public Date getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Date creadoEn) { this.creadoEn = creadoEn; }
}
