package com.repobackend.api.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "almacenes")
public class Almacen {
    @Id
    private String id;
    private String tallerId;
    private String nombre;
    private String ubicacion;
    private boolean activo = true;
    private Date fechaCreacion = new Date();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTallerId() { return tallerId; }
    public void setTallerId(String tallerId) { this.tallerId = tallerId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
