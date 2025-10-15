package com.repobackend.api.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "categorias")
public class Categoria {
    @Id
    private String id; // Mongo ObjectId as hex string

    // additional string id separate from _id (matches schema 'id' as string)
    @Field("id")
    private String idString;
    private String nombre;
    private String descripcion;
    private Integer iconoRecurso;

    private Date creadoEn = new Date();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdString() { return idString; }
    public void setIdString(String idString) { this.idString = idString; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getIconoRecurso() { return iconoRecurso; }
    public void setIconoRecurso(Integer iconoRecurso) { this.iconoRecurso = iconoRecurso; }

    public Date getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Date creadoEn) { this.creadoEn = creadoEn; }
}
