package co.edu.unicauca.laborDocente.api.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Representa un tipo de actividad dentro de la labor docente.
 */
@Data
public class ActividadDTO {
    private String tipoActividad;
    private List<Map<String, Object>> detalles;
}
