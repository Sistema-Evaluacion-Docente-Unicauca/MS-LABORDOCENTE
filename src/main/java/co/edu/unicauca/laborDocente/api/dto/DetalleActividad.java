package co.edu.unicauca.laborDocente.api.dto;

import lombok.Data;

/**
 * Detalles específicos de una actividad dentro de la labor docente.
 */
@Data
public class DetalleActividad {
    private String actoAdministrativo;
    private int semanas;
    private float horas;
    private String actividad;
}
