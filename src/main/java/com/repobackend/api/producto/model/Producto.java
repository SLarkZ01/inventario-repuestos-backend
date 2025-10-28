package com.repobackend.api.producto.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

    // media items: lista de objetos con keys estandarizadas. Ej: {"type":"image","publicId":"abc123","url":"https://res.cloudinary.com/...","order":0}
    private List<Map<String, Object>> listaMedios;

    // Especificaciones técnicas estructuradas para mostrar la tabla (marca, cilindrada, peso, compatibilidad, etc)
    // Se almacena como mapa clave->valor o lista de pares según preferencia del cliente; aquí usamos Map para flexibilidad.
    private Map<String, String> specs;

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

    public List<Map<String, Object>> getListaMedios() { return listaMedios; }
    public void setListaMedios(List<Map<String, Object>> listaMedios) { this.listaMedios = listaMedios; }

    public Map<String, String> getSpecs() { return specs; }
    public void setSpecs(Map<String, String> specs) { this.specs = specs; }

    public Date getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Date creadoEn) { this.creadoEn = creadoEn; }

    // Helper transient: devuelve la URL de la miniatura (primera imagen) si existe en listaMedios.
    public String getThumbnailUrl() {
        if (listaMedios == null || listaMedios.isEmpty()) return null;
        Map<String, Object> first = listaMedios.get(0);
        // Preferir url si existe, sino construir desde publicId usando Cloudinary si se decide
        if (first.containsKey("url") && first.get("url") instanceof String) return (String) first.get("url");
        if (first.containsKey("publicId") && first.get("publicId") instanceof String) {
            // ruta por defecto de Cloudinary (sin transformar): https://res.cloudinary.com/{cloudName}/image/upload/{publicId}.jpg
            // No tenemos cloudName aquí; la app/nextjs debe construir la URL con su cloudName config o el backend podría exponer un baseUrl.
            return (String) first.get("publicId"); // devolver publicId para que el cliente construya la URL
        }
        return null;
    }
}
