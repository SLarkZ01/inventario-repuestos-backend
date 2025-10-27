package com.repobackend.api.model;

public class ClienteEmbebido {
    private String nombre;
    private String documento; // nullable
    private String direccion; // nullable

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
