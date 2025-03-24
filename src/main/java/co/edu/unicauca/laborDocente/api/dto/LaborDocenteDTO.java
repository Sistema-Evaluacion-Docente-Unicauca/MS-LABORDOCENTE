package co.edu.unicauca.laborDocente.api.dto;

import java.util.List;

import lombok.Data;

@Data
public class LaborDocenteDTO {
    private InformacionDocente informacionDocente;
    private List<ActividadDTO> actividades;
}
