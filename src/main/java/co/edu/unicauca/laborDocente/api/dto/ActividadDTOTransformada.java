package co.edu.unicauca.laborDocente.api.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ActividadDTOTransformada {
    private Map<String, Object> tipoActividad; // {"oidTipoActividad": 1} o nombre como string
    private String oidEvaluador;
    private String oidEvaluado;
    private Long oidEstadoActividad;
    private String nombreActividad;
    private Double horas;
    private Double semanas;
    private Boolean informeEjecutivo;
    private List<AtributoValorDTO> atributos;
}
