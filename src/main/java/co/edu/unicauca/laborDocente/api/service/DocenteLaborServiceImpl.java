package co.edu.unicauca.laborDocente.api.service;

import co.edu.unicauca.laborDocente.api.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio que consulta los datos de labor docente usando la
 * API externa de KIRA.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocenteLaborServiceImpl implements DocenteLaborService {

    private final RestTemplate restTemplate;

    @Value("${external.api.kira.base-url}")
    private String baseUrl;

    @Value("${external.api.kira.obtener-departamentos}")
    private String pathDepartamentos;

    @Value("${external.api.kira.obtener-docentes}")
    private String pathDocentes;

    @Value("${external.api.kira.obtener-labor-docente}")
    private String pathLaborDocente;

    @Value("${external.api.sed.base-url}")
    private String sedBaseUrl;

    private Map<String, Long> cacheAtributos = new HashMap<>();

    private Map<String, Long> cacheTiposActividad = new HashMap<>();

    @Override
    public List<ActividadDTOTransformada> cargarLaborDocente(Long idFacultad, Long idPeriodo, Long idDepartamento) {
        cargarEavAtributos();
        cargarTiposActividadDesdeSed();
        List<ActividadDTOTransformada> resultado = new ArrayList<>();
        List<Long> departamentos = obtenerDepartamentos(idFacultad, idDepartamento);
    
        for (Long idDepto : departamentos) {
            List<DocenteDTO> docentes = obtenerDocentesPorDepartamentoYPeriodo(idDepto, idPeriodo);
    
            for (DocenteDTO docente : docentes) {
                LaborDocenteDTO labor = obtenerLaborDocente(docente.getId());
                if (labor != null) {
                    List<ActividadDTOTransformada> actividadesTransformadas = transformarActividades(
                        labor.getActividades(),
                        "123",
                        docente.getIdentificacion()
                    );
                    resultado.addAll(actividadesTransformadas);
                }
            }
        }
    
        return resultado;
    }    

    private List<Long> obtenerDepartamentos(Long idFacultad, Long idDepartamento) {
        if (idDepartamento != null) {
            return List.of(idDepartamento);
        }

        String url = buildUrl(baseUrl, pathDepartamentos) + "?facultad=" + idFacultad;
        log.debug("Consultando departamentos en: {}", url);

        try {
            ResponseEntity<DepartamentoDTO[]> response = restTemplate.getForEntity(url, DepartamentoDTO[].class);
            DepartamentoDTO[] departamentos = response.getBody();
            if (departamentos != null) {
                return Arrays.stream(departamentos)
                        .map(DepartamentoDTO::getId)
                        .toList();
            }
        } catch (Exception e) {
            log.error("Error al consultar departamentos para facultad {}: {}", idFacultad, e.getMessage());
        }

        return List.of();
    }

    private List<DocenteDTO> obtenerDocentesPorDepartamentoYPeriodo(Long idDepto, Long idPeriodo) {
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

    private LaborDocenteDTO obtenerLaborDocente(Long idDocente) {
        String url = buildUrl(baseUrl, pathLaborDocente) + "?id=" + idDocente;
        try {
            ResponseEntity<LaborDocenteDTO> response = restTemplate.getForEntity(url, LaborDocenteDTO.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("No se pudo obtener la labor docente para el ID {}: {}", idDocente, e.getMessage());
            return null;
        }
    }

    private void cargarEavAtributos() {
        String url = sedBaseUrl.replaceAll("/$", "") + "/api/eavatributo";
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
            List<Map<String, Object>> atributos = (List<Map<String, Object>>) data.get("content");
            cacheAtributos = atributos.stream()
                    .collect(Collectors.toMap(
                            a -> a.get("nombre").toString().toUpperCase(),
                            a -> Long.parseLong(a.get("oideavAtributo").toString())
                    ));
        } catch (Exception e) {
            log.error("Error al cargar atributos desde SED: {}", e.getMessage());
        }
    }

    private String buildUrl(String base, String path) {
        return base.replaceAll("/$", "") + "/" + path.replaceAll("^/", "");
    }

    private Long obtenerIdPorNombre(String nombre) {
        return cacheTiposActividad.getOrDefault(nombre.toUpperCase(), 0L);
    }

    public List<ActividadDTOTransformada> transformarActividades(List<ActividadDTO> actividadesOriginales, String oidEvaluador, String oidEvaluado) {
        List<ActividadDTOTransformada> resultado = new ArrayList<>();

        for (ActividadDTO actividad : actividadesOriginales) {
            String nombreActividad = actividad.getTipoActividad();

            for (Map<String, Object> detalle : actividad.getDetalles()) {
                ActividadDTOTransformada nueva = new ActividadDTOTransformada();

                nueva.setTipoActividad(Map.of("oidTipoActividad", obtenerIdPorNombre(nombreActividad)));
                nueva.setOidEvaluador(oidEvaluador);
                nueva.setOidEvaluado(oidEvaluado);
                nueva.setOidEstadoActividad(3L);
                nueva.setNombreActividad(nombreActividad);

                nueva.setSemanas(detalle.get("semanas") != null ? Double.valueOf(detalle.get("semanas").toString()) : 0.0);
                nueva.setHoras(detalle.get("horasSemanales") != null ? Double.valueOf(detalle.get("horasSemanales").toString()) : 0.0);
                nueva.setInformeEjecutivo(false);

                List<AtributoValorDTO> atributos = detalle.entrySet().stream()
                        .filter(e -> !List.of("semanas", "horasSemanales").contains(e.getKey()))
                        .map(e -> {
                            String key = e.getKey().toUpperCase();
                            if (!cacheAtributos.containsKey(key)) {
                                return null;
                            }
                            return new AtributoValorDTO(
                                key,
                                e.getValue() != null ? e.getValue().toString() : null
                            );
                        })
                        .filter(Objects::nonNull)
                        .toList();

                nueva.setAtributos(atributos);
                resultado.add(nueva);
            }
        }

        return resultado;
    }

    private void cargarTiposActividadDesdeSed() {
        String url = sedBaseUrl.replaceAll("/$", "") + "/api/tipo-actividad";
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<?, ?> body = response.getBody();
            List<Map<String, Object>> tipos = (List<Map<String, Object>>) body.get("content");
    
            cacheTiposActividad = tipos.stream()
                .collect(Collectors.toMap(
                    t -> t.get("nombre").toString().toUpperCase(),
                    t -> Long.parseLong(t.get("oidTipoActividad").toString())
                ));
    
            log.info("Tipos de actividad cargados: {}", cacheTiposActividad.keySet());
    
        } catch (Exception e) {
            log.error("Error al cargar tipos de actividad desde SED: {}", e.getMessage());
        }
    }
}
