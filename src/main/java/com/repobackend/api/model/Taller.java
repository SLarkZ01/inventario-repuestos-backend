package com.repobackend.api.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "talleres")
public class Taller {
    @Id
    private String id;
    private String ownerId;
    private String nombre;
    private boolean activo = true;
    private Date fechaCreacion = new Date();
    private List<String> almacenes; // list of almacen ids
    // miembros stored as simple objects in a real app consider a dedicated class
    private java.util.List<java.util.Map<String, Object>> miembros;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public List<String> getAlmacenes() { return almacenes; }
    public void setAlmacenes(List<String> almacenes) { this.almacenes = almacenes; }
    public java.util.List<java.util.Map<String, Object>> getMiembros() { return miembros; }
    public void setMiembros(java.util.List<java.util.Map<String, Object>> miembros) { this.miembros = miembros; }
}
