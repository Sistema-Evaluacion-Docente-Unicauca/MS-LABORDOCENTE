package co.edu.unicauca.laborDocente.api.util;

import co.edu.unicauca.laborDocente.api.dto.ActividadDTO;
import co.edu.unicauca.laborDocente.api.dto.ActividadDTOTransformada;
import co.edu.unicauca.laborDocente.api.dto.AtributoValorDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Clase responsable de transformar actividades KIRA a la estructura interna del sistema SED.
 */
@Component
@Slf4j
public class ActividadTransformer {

    private static final Logger logger = LoggerFactory.getLogger(ActividadTransformer.class);

    public List<ActividadDTOTransformada> transformar(List<ActividadDTO> actividadesOriginales,
            Map<String, Integer> atributos, Map<String, Integer> tiposActividad, Integer oidEvaluador, Integer oidEvaluado, Integer idLaborDocente) {
        
        List<ActividadDTOTransformada> resultado = new ArrayList<>();

        for (ActividadDTO actividad : actividadesOriginales) {
            Integer oidTipoActividad = obtenerOidTipoActividad(actividad, tiposActividad, idLaborDocente);

            for (Map<String, Object> detalle : actividad.getDetalles()) {
                ActividadDTOTransformada nueva = transformarDetalle(detalle, oidTipoActividad, oidEvaluador, oidEvaluado, idLaborDocente, atributos);
                resultado.add(nueva);
            }
        }

        return resultado;
    }

    private String normalizarNombreActividad(String nombreActividad) {
        if (nombreActividad == null) {
            return null;
        }

        if ("TRABAJOSINVESTIGACIÓN".equalsIgnoreCase(nombreActividad)) {
            return "TRABAJOS DE INVESTIGACION";
        }
        return nombreActividad;
    }

    private Integer obtenerOidTipoActividad(ActividadDTO actividad, Map<String, Integer> tiposActividad, Integer idLaborDocente) {
        String nombreActividad = normalizarNombreActividad(actividad.getTipoActividad());
        actividad.setTipoActividad(nombreActividad); // Actualizas el DTO corregido
        Integer oidTipoActividad = tiposActividad.getOrDefault(nombreActividad.toUpperCase(), 0);
    
        if (oidTipoActividad == 0) {
            logger.warn("No se encontró el tipo de actividad para: '{}', con idLaborDocente: '{}'. Se asignó el valor predeterminado '0'.", nombreActividad, idLaborDocente);
        }
    
        return oidTipoActividad;
    }

    private ActividadDTOTransformada transformarDetalle(Map<String, Object> detalle, Integer oidTipoActividad, Integer oidEvaluador, 
            Integer oidEvaluado, Integer idLaborDocente, Map<String, Integer> atributos) {
        
        ActividadDTOTransformada nueva = new ActividadDTOTransformada();

        nueva.setTipoActividad(Map.of("oidTipoActividad", oidTipoActividad));
        nueva.setOidEvaluador(oidEvaluador);
        nueva.setOidEvaluado(oidEvaluado);
        nueva.setOidEstadoActividad(1);

        nueva.setSemanas(detalle.get("semanas") != null ? Double.valueOf(detalle.get("semanas").toString()) : 0.0);
        nueva.setHoras(detalle.get("horasSemanales") != null ? Double.valueOf(detalle.get("horasSemanales").toString()) : 0.0);
        nueva.setInformeEjecutivo(false);

        List<AtributoValorDTO> atributosTransformados = detalle.entrySet().stream()
                .filter(e -> !List.of("semanas", "horasSemanales").contains(e.getKey()))
                .map(e -> {
                    String key = e.getKey().toUpperCase();
                    if (!atributos.containsKey(key)) {
                        return null;
                    }
                    return new AtributoValorDTO(key, e.getValue() != null ? e.getValue().toString() : null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        nueva.setAtributos(atributosTransformados);
        nueva.setIdLaborDocente(idLaborDocente);
        nueva.setEsLaborDocente(true);

        return nueva;
    }
}
