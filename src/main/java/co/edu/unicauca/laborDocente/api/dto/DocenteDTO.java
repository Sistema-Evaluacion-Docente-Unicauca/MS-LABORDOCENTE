package co.edu.unicauca.laborDocente.api.dto;

import lombok.Data;

/**
 * Representa la información básica de un docente.
 */
@Data
public class DocenteDTO {
    private Long id;
    private String identificacion;
    private String nombreCompleto;
}
