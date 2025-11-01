package com.repobackend.api.movimiento.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.movimiento.dto.MovimientoRequest;
import com.repobackend.api.movimiento.dto.MovimientoResponse;
import com.repobackend.api.movimiento.model.Movimiento;
import com.repobackend.api.movimiento.service.MovimientoService;

import jakarta.validation.Valid;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/movimientos")
@Tag(name = "Movimientos", description = "Registro de entradas y salidas de stock")
public class MovimientoController {
    private final MovimientoService movimientoService;

    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @Operation(
        summary = "Crear movimiento",
        description = "Registra una entrada (INGRESO) o salida (EGRESO) de stock. Los tipos válidos son: INGRESO, EGRESO, VENTA, DEVOLUCION, AJUSTE.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del movimiento",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Ingreso de mercancía",
                        value = "{\"tipo\":\"INGRESO\",\"productoId\":\"507f191e810c19729de860ea\",\"cantidad\":50,\"referencia\":\"OC-2024-001\",\"notas\":\"Compra a proveedor\",\"realizadoPor\":\"507f1f77bcf86cd799439011\"}"
                    ),
                    @ExampleObject(
                        name = "Egreso por venta",
                        value = "{\"tipo\":\"VENTA\",\"productoId\":\"507f191e810c19729de860ea\",\"cantidad\":5,\"referencia\":\"FAC-2024-123\",\"notas\":\"Venta cliente ABC\"}"
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Movimiento creado exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"movimiento\":{\"id\":\"507f191e810c19729de860eb\",\"tipo\":\"INGRESO\",\"productoId\":\"507f191e810c19729de860ea\",\"cantidad\":50,\"fecha\":\"2024-10-30T10:30:00Z\"}}")
                )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
        }
    )
    @PostMapping
    public ResponseEntity<?> crearMovimiento(@Valid @RequestBody MovimientoRequest body) {
        MovimientoResponse resp = movimientoService.crearMovimiento(body);
        return ResponseEntity.status(201).body(Map.of("movimiento", resp));
    }

    @Operation(
        summary = "Listar movimientos",
        description = "Lista movimientos de stock. Puede filtrarse por producto o por tipo de movimiento.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "productoId", description = "ID del producto para filtrar", example = "507f191e810c19729de860ea"),
            @io.swagger.v3.oas.annotations.Parameter(name = "tipo", description = "Tipo de movimiento (INGRESO, EGRESO, VENTA, DEVOLUCION, AJUSTE)", example = "INGRESO")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de movimientos",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"movimientos\":[{\"id\":\"507f191e810c19729de860eb\",\"tipo\":\"INGRESO\",\"productoId\":\"507f191e810c19729de860ea\",\"cantidad\":50,\"fecha\":\"2024-10-30T10:30:00Z\"}]}"
                    )
                )
            )
        }
    )
    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) String productoId, @RequestParam(required = false) String tipo) {
        if (productoId != null) {
            List<Movimiento> res = movimientoService.listarPorProducto(productoId);
            List<MovimientoResponse> dto = res.stream().map(this::toDto).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("movimientos", dto));
        }
        if (tipo != null) {
            List<Movimiento> res = movimientoService.listarPorTipo(tipo);
            List<MovimientoResponse> dto = res.stream().map(this::toDto).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("movimientos", dto));
        }
        List<Movimiento> all = movimientoService.listarTodos();
        List<MovimientoResponse> dto = all.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("movimientos", dto));
    }

    @Operation(summary = "Obtener movimiento por id", description = "Devuelve un movimiento por su id",
        responses = {@ApiResponse(responseCode = "200", description = "Movimiento encontrado", content = @Content),
                     @ApiResponse(responseCode = "404", description = "Movimiento no encontrado", content = @Content)})
    @GetMapping("/{id}")
    public ResponseEntity<?> getMovimiento(@PathVariable String id) {
        var maybe = movimientoService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Movimiento no encontrado"));
        return ResponseEntity.ok(Map.of("movimiento", toDto(maybe.get())));
    }

    // NOTA: No existe handler PUT para /{id} porque los movimientos son registros históricos
    // inmutables. Si se intenta hacer PUT en esta ruta, Spring devolverá 405 Method Not Allowed
    // y el header Allow incluirá los métodos permitidos (GET, POST). Si se requiere una
    // corrección, cree un movimiento compensatorio mediante POST /api/movimientos.

    private MovimientoResponse toDto(Movimiento m) {
        MovimientoResponse r = new MovimientoResponse();
        r.id = m.getId();
        r.tipo = m.getTipo();
        r.productoId = m.getProductoId();
        r.cantidad = m.getCantidad();
        r.referencia = m.getReferencia();
        r.notas = m.getNotas();
        r.creadoEn = m.getCreadoEn();
        if (m.getRealizadoPor() != null) r.realizadoPor = m.getRealizadoPor().toHexString();
        else r.realizadoPor = null;
        return r;
    }
}
