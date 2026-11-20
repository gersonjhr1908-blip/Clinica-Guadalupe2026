package com.clinicaguadalupe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> manejarIntegridad(DataIntegrityViolationException exception) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("estado", 409);
        respuesta.put("error", "Operación no permitida");
        respuesta.put("mensaje", "No se puede eliminar porque el registro tiene información relacionada. Desactívalo en su lugar.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(respuesta);
    }

    @ExceptionHandler(DatoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> manejarDatoDuplicado(
            DatoDuplicadoException exception
    ) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("fecha", LocalDateTime.now());
        respuesta.put("estado", 409);
        respuesta.put("error", "Dato duplicado");
        respuesta.put("mensaje", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(respuesta);
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<Map<String, Object>> manejarOperacionNoPermitida(
            OperacionNoPermitidaException exception
    ) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("fecha", LocalDateTime.now());
        respuesta.put("estado", 409);
        respuesta.put("error", "Operación no permitida");
        respuesta.put("mensaje", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(respuesta);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidaciones(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errores = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errores.put(error.getField(), error.getDefaultMessage())
                );

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("fecha", LocalDateTime.now());
        respuesta.put("estado", 400);
        respuesta.put("error", "Datos inválidos");
        respuesta.put("campos", errores);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(
            RecursoNoEncontradoException exception
    ) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("fecha", LocalDateTime.now());
        respuesta.put("estado", 404);
        respuesta.put("error", "Recurso no encontrado");
        respuesta.put("mensaje", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(respuesta);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, Object>> manejarCredencialesInvalidas(
            CredencialesInvalidasException exception
    ) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("fecha", LocalDateTime.now());
        respuesta.put("estado", 401);
        respuesta.put("error", "No autorizado");
        respuesta.put("mensaje", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(respuesta);
    }
}
