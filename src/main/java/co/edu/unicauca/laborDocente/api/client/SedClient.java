package co.edu.unicauca.laborDocente.api.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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

    public Map<String, Long> obtenerAtributos() {
        String url = sedBaseUrl.replaceAll("/$", "") + "/api/eavatributo";
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
            List<Map<String, Object>> atributos = (List<Map<String, Object>>) data.get("content");

            return atributos.stream()
                    .collect(Collectors.toMap(
                            a -> a.get("nombre").toString().toUpperCase(),
                            a -> Long.parseLong(a.get("oideavAtributo").toString())
                    ));
        } catch (Exception e) {
            log.error("Error al consultar atributos desde SED: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    public Map<String, Long> obtenerTiposActividad() {
        String url = sedBaseUrl.replaceAll("/$", "") + "/api/tipo-actividad";
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            List<Map<String, Object>> tipos = (List<Map<String, Object>>) response.getBody().get("content");

            return tipos.stream()
                    .collect(Collectors.toMap(
                            t -> t.get("nombre").toString().toUpperCase(),
                            t -> Long.parseLong(t.get("oidTipoActividad").toString())
                    ));
        } catch (Exception e) {
            log.error("Error al consultar tipos de actividad desde SED: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
