package co.edu.unicauca.laborDocente.api.util;

import co.edu.unicauca.laborDocente.api.dto.ActividadDTO;
import co.edu.unicauca.laborDocente.api.dto.ActividadDTOTransformada;
import co.edu.unicauca.laborDocente.api.dto.AtributoValorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Clase responsable de transformar actividades KIRA a la estructura interna del
 * sistema SED.
 */
@Component
@Slf4j
public class ActividadTransformer {

    public List<ActividadDTOTransformada> transformar(List<ActividadDTO> actividadesOriginales,Map<String, Integer> atributos, Map<String, Integer> tiposActividad,
            Integer oidEvaluador, Integer oidEvaluado) {

        List<ActividadDTOTransformada> resultado = new ArrayList<>();

        for (ActividadDTO actividad : actividadesOriginales) {
            String nombreActividad = actividad.getTipoActividad();
            Integer oidTipoActividad = tiposActividad.getOrDefault(nombreActividad.toUpperCase(), 0);

            for (Map<String, Object> detalle : actividad.getDetalles()) {
                ActividadDTOTransformada nueva = new ActividadDTOTransformada();

                nueva.setTipoActividad(Map.of("oidTipoActividad", oidTipoActividad));
                nueva.setOidEvaluador(oidEvaluador);
                nueva.setOidEvaluado(oidEvaluado);
                nueva.setOidEstadoActividad(1);
                nueva.setNombreActividad(nombreActividad);

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
                    }).filter(Objects::nonNull).collect(Collectors.toList());

                nueva.setAtributos(atributosTransformados);
                resultado.add(nueva);
            }
        }

        return resultado;
    }
}
