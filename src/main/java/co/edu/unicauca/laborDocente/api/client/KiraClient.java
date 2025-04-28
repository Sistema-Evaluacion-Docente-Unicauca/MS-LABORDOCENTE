package co.edu.unicauca.laborDocente.api.client;

import co.edu.unicauca.laborDocente.api.dto.DataTerceroDTO;
import co.edu.unicauca.laborDocente.api.dto.DepartamentoDTO;
import co.edu.unicauca.laborDocente.api.dto.DocenteDTO;
import co.edu.unicauca.laborDocente.api.dto.LaborDocenteDTO;
import co.edu.unicauca.laborDocente.api.dto.ProgramaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cliente encargado de consumir los servicios de la API externa de KIRA.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KiraClient {

    private final RestTemplate restTemplate;

    @Value("${external.api.kira.base-url}")
    private String baseUrl;

    @Value("${external.api.kira.obtener-departamentos}")
    private String pathDepartamentos;

    @Value("${external.api.kira.obtener-docentes}")
    private String pathDocentes;

    @Value("${external.api.kira.obtener-data-tercero}")
    private String pathDataTercero;

    @Value("${external.api.kira.obtener-labor-docente}")
    private String pathLaborDocente;

    public List<Integer> obtenerDepartamentos(Integer idFacultad, Integer idDepartamento) {
        if (idDepartamento != null) {
            return List.of(idDepartamento);
        }

        String url = buildUrl(baseUrl, pathDepartamentos) + "?facultad=" + idFacultad;
        log.debug("Consultando departamentos en: {}", url);

        try {
            ResponseEntity<DepartamentoDTO[]> response = restTemplate.getForEntity(url, DepartamentoDTO[].class);
            DepartamentoDTO[] departamentos = response.getBody();
            if (departamentos != null) {
                return Arrays.stream(departamentos).map(DepartamentoDTO::getId).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Error al consultar departamentos para facultad {}: {}", idFacultad, e.getMessage());
        }

        return List.of();
    }

    public List<DocenteDTO> obtenerDocentes(Integer idDepto, Integer idPeriodo) {
        String url = buildUrl(baseUrl, pathDocentes) + "?departamento=" + idDepto + "&periodo=" + idPeriodo;
        log.debug("Consultando docentes en: {}", url);

        try {
            ResponseEntity<DocenteDTO[]> response = restTemplate.getForEntity(url, DocenteDTO[].class);
            DocenteDTO[] docentes = response.getBody();
            if (docentes != null) {
                return Arrays.asList(docentes);
            }
        } catch (Exception e) {
            log.error("Error al consultar docentes del departamento {} en el periodo {}: {}", idDepto, idPeriodo,
                    e.getMessage());
        }

        return List.of();
    }

    public LaborDocenteDTO obtenerLabor(Integer idDocente) {
        String url = buildUrl(baseUrl, pathLaborDocente) + "?id=" + idDocente;
        try {
            ResponseEntity<LaborDocenteDTO> response = restTemplate.getForEntity(url, LaborDocenteDTO.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("No se pudo obtener la labor docente para el ID {}: {}", idDocente, e.getMessage());
            return null;
        }
    }

    public List<String> obtenerRolesActivosPorUsername(String username) {
        String url = buildUrl(baseUrl, pathDataTercero) + username;
        DataTerceroDTO dataTercero = restTemplate.getForObject(url, DataTerceroDTO.class);
    
        if (dataTercero == null || dataTercero.getPrograma() == null) {
            return List.of();
        }
    
        return dataTercero.getPrograma().stream()
            .filter(programa -> "ACTIVO".equalsIgnoreCase(programa.getEstado()))
            .map(ProgramaDTO::getRol).distinct().collect(Collectors.toList());
    }    

    private String buildUrl(String base, String path) {
        return base.replaceAll("/$", "") + "/" + path.replaceAll("^/", "");
    }
}