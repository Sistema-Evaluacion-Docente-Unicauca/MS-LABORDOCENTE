package co.edu.unicauca.laborDocente.api.dto;

import lombok.Data;

/**
 * Detalles personales y profesionales del docente.
 */
@Data
public class InformacionDocente {
    private String dedicacion;
    private String estado;
    private String correoInstitucional;
    private String tipoContratacion;
    private String nivelEstudios;
    private String categoria;
    private String departamento;
    private String identificacion;
    private String nombreCompleto;
    private String facultad;
    private String username;
}
