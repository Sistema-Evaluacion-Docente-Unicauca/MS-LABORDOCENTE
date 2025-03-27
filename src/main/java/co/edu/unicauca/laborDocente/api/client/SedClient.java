package co.edu.unicauca.laborDocente.api.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import co.edu.unicauca.laborDocente.api.dto.ActividadDTOTransformada;
import co.edu.unicauca.laborDocente.api.dto.UsuarioDocenteDTO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cliente encargado de consumir los servicios del sistema SED.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SedClient {

    private final RestTemplate restTemplate;

    @Value("${external.api.sed.base-url}")
    private String sedBaseUrl;

    public Map<String, Integer> obtenerAtributos() {
        String url = sedBaseUrl.replaceAll("/$", "") + "/api/eavatributo";
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
            List<Map<String, Object>> atributos = (List<Map<String, Object>>) data.get("content");

            return atributos.stream()
                    .collect(Collectors.toMap(
                            a -> a.get("nombre").toString().toUpperCase(),
                            a -> Integer.parseInt(a.get("oideavAtributo").toString())
                    ));
        } catch (Exception e) {
            log.error("Error al consultar atributos desde SED: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    public Map<String, Integer> obtenerTiposActividad() {
        String url = sedBaseUrl.replaceAll("/$", "") + "/api/tipo-actividad";
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            List<Map<String, Object>> tipos = (List<Map<String, Object>>) response.getBody().get("content");

            return tipos.stream()
                    .collect(Collectors.toMap(
                            t -> t.get("nombre").toString().toUpperCase(),
                            t -> Integer.parseInt(t.get("oidTipoActividad").toString())
                    ));
        } catch (Exception e) {
            log.error("Error al consultar tipos de actividad desde SED: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    public Map<String, Integer> obtenerRoles() {
        String url = sedBaseUrl.replaceAll("/$", "") + "/api/roles";
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<?, ?> body = response.getBody();
            List<Map<String, Object>> roles = (List<Map<String, Object>>) body.get("content");

            return roles.stream()
                    .collect(Collectors.toMap(
                            r -> r.get("nombre").toString().toUpperCase(),
                            r -> Integer.parseInt(r.get("oid").toString())
                    ));
        } catch (Exception e) {
            log.error("Error al consultar roles desde SED: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    public String guardarUsuarios(List<UsuarioDocenteDTO> usuarios) {
        String url = sedBaseUrl.replaceAll("/$", "") + "/api/usuarios";
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, usuarios, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().get("mensaje").toString();
            } else {
                return "Respuesta inesperada del sistema SED.";
            }
        } catch (Exception e) {
            log.error("Error al guardar usuarios en SED: {}", e.getMessage());
            throw new RuntimeException("Error al guardar usuarios en SED.", e);
        }
    }

    public String guardarActividades(List<ActividadDTOTransformada> actividades) {
        String url = sedBaseUrl.replaceAll("/$", "") + "/api/actividades";
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, actividades, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().get("mensaje").toString();
            } else {
                return "Respuesta inesperada del sistema SED.";
            }
        } catch (Exception e) {
            log.error("Error al guardar actividades en SED: {}", e.getMessage());
            throw new RuntimeException("Error al guardar actividades en SED.", e);
        }
    }
}
