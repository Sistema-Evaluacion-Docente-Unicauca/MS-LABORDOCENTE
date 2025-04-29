package co.edu.unicauca.laborDocente.api.dto;

import java.util.List;

import lombok.Data;

@Data
public class LaborDocenteDTO {
    private Integer idLaborDocente;
    private InformacionDocente informacionDocente;
    private List<ActividadDTO> actividades;
}
