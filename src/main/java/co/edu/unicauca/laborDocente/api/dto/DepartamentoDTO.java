package co.edu.unicauca.laborDocente.api.dto;

import lombok.Data;

/**
 * Representa un departamento asociado a una facultad.
 */
@Data
public class DepartamentoDTO {
    private Long id;
    private String label;
}
