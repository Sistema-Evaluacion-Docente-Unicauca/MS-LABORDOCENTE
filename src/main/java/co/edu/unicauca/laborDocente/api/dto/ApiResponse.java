package co.edu.unicauca.laborDocente.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Clase de respuesta estándar para todas las API REST.
 *
 * @param <T> Tipo de dato que se devolverá en la respuesta (puede ser cualquier objeto o `null`).
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private int codigo;
    private String mensaje;
    private T data;
}