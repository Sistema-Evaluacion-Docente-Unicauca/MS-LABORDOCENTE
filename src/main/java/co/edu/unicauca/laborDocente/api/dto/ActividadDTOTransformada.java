package co.edu.unicauca.laborDocente.api.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ActividadDTOTransformada {
    private Map<String, Object> tipoActividad;
    private Integer oidEvaluador;
    private Integer oidEvaluado;
    private Integer oidEstadoActividad;
    private String nombreActividad;
    private Double horas;
    private Double semanas;
    private Boolean informeEjecutivo;
    private List<AtributoValorDTO> atributos;
    private Integer idLaborDocente;
    private boolean esLaborDocente;
}
