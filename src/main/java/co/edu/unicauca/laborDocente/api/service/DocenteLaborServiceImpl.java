package co.edu.unicauca.laborDocente.api.service;

import co.edu.unicauca.laborDocente.api.client.KiraClient;
import co.edu.unicauca.laborDocente.api.client.SedClient;
import co.edu.unicauca.laborDocente.api.dto.*;
import co.edu.unicauca.laborDocente.api.util.ActividadTransformer;
import co.edu.unicauca.laborDocente.api.util.UsuarioDocenteGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocenteLaborServiceImpl implements DocenteLaborService {

    private final KiraClient kiraClient;
    private final SedClient sedClient;
    private final ActividadTransformer actividadTransformer;
    private final UsuarioDocenteGenerator usuarioDocenteGenerator;

    private static final Logger logger = LoggerFactory.getLogger(DocenteLaborServiceImpl.class);

    @Override
    public ApiResponse<Void> procesarLaborDocente(Integer idFacultad, Integer idPeriodo, Integer idDepartamento, String token) {
        try {
            // 1. Consultar labor docente (una sola vez)
            List<Integer> departamentos = kiraClient.obtenerDepartamentos(idFacultad, idDepartamento);
            Map<Integer, List<DocenteDTO>> docentesPorDepartamento = obtenerDocentesPorDepartamento(departamentos, idPeriodo);
            Map<Integer, LaborDocenteDTO> labores = obtenerLabores(docentesPorDepartamento);

            // 2. Guardar usuarios docentes
            ApiResponse<Void> usuariosResponse = procesarUsuariosDocentes(labores, token);

            /*if (usuariosResponse.getCodigo() != 200) {
                return new ApiResponse<>(usuariosResponse.getCodigo(),"Error al guardar usuarios: " + usuariosResponse.getMensaje(), null);
            }*/

            // 3. Guardar actividades docentes
            ApiResponse<Void> actividadesResponse = procesarActividadesDocentes(labores, token);

            if (actividadesResponse.getCodigo() != 200) {
                // Usuarios guardados correctamente, pero actividades fallaron
                return new ApiResponse<>(207,"Usuarios guardados correctamente, pero error al guardar actividades: " + actividadesResponse.getMensaje(), null);
            }

            // 4. Todo correcto
            return new ApiResponse<>(200, "Usuarios y actividades guardados correctamente.", null);

        } catch (IllegalArgumentException e) {
            logger.error("Error en parámetros: {}", e.getMessage());
            return new ApiResponse<>(400, "Error en parámetros: " + e.getMessage(), null);

        } catch (Exception e) {
            logger.error("Error inesperado al procesar la labor docente", e);
            return new ApiResponse<>(500, "Error inesperado al procesar la labor docente: " + e.getMessage(), null);
        }
    }

    @Transactional
    public ApiResponse<Void> procesarUsuariosDocentes(Map<Integer, LaborDocenteDTO> labores, String token) {
        try {
            List<UsuarioDocenteDTO> usuarios = usuarioDocenteGenerator.generarDesdeLabor(new ArrayList<>(labores.values()), token);
            String mensaje = sedClient.guardarUsuarios(usuarios, token);
            return new ApiResponse<>(200, mensaje, null);

        } catch (IllegalArgumentException e) {
            logger.error("Error en parámetros al generar usuarios docentes", e);
            return new ApiResponse<>(400, "Error en parámetros al generar usuarios docentes: " + e.getMessage(), null);

        } catch (Exception e) {
            logger.error("Error inesperado al generar usuarios docentes", e);
            return new ApiResponse<>(500, "Error inesperado al generar usuarios docentes: " + e.getMessage(), null);
        }
    }

    @Transactional
    public ApiResponse<Void> procesarActividadesDocentes(Map<Integer, LaborDocenteDTO> labores, String token) {
        try {
            Map<String, Integer> atributos = sedClient.obtenerAtributos(token);
            Map<String, Integer> tiposActividad = sedClient.obtenerTiposActividad(token);
            List<ActividadDTOTransformada> transformadas = new ArrayList<>();

            labores.forEach((idDocente, labor) -> {
                List<ActividadDTO> actividades = labor.getActividades();
                if (actividades != null && !actividades.isEmpty()) {
                    transformadas.addAll(actividadTransformer.transformar(actividades, atributos, tiposActividad, 0, Integer.valueOf(labor.getInformacionDocente().getIdentificacion())));
                }
            });

            String mensaje = sedClient.guardarActividades(transformadas, token);
            return new ApiResponse<>(200, mensaje, null);

        } catch (IllegalArgumentException e) {
            logger.error("Error en parámetros al cargar actividades docentes", e);
            return new ApiResponse<>(400, "Error en parámetros al cargar actividades docentes: " + e.getMessage(), null);

        } catch (Exception e) {
            logger.error("Error inesperado al cargar actividades docentes", e);
            return new ApiResponse<>(500, "Error inesperado al cargar actividades docentes: " + e.getMessage(), null);
        }
    }

    private Map<Integer, List<DocenteDTO>> obtenerDocentesPorDepartamento(List<Integer> departamentos, Integer idPeriodo) {
        Map<Integer, List<DocenteDTO>> mapa = new HashMap<>();
        try {
            for (Integer idDepto : departamentos) {
                mapa.put(idDepto, kiraClient.obtenerDocentes(idDepto, idPeriodo));
            }
        } catch (Exception e) {
            logger.error("Error al obtener docentes por departamento", e);
            throw new RuntimeException("Error al obtener docentes por departamento: " + e.getMessage(), e);
        }
        return mapa;
    }    

    private Map<Integer, LaborDocenteDTO> obtenerLabores(Map<Integer, List<DocenteDTO>> docentesPorDepto) {
        Map<Integer, LaborDocenteDTO> mapa = new HashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Void>> tareas = new ArrayList<>();
        try {
            for (List<DocenteDTO> docentes : docentesPorDepto.values()) {
                for (DocenteDTO docente : docentes) {
                    CompletableFuture<Void> tarea = CompletableFuture.runAsync(() -> {
                        LaborDocenteDTO labor = kiraClient.obtenerLabor(docente.getId());
                        if (labor != null) {
                            mapa.put(docente.getId(), labor);
                        }
                    }, executor);
                    tareas.add(tarea);
                }
            }
    
            CompletableFuture.allOf(tareas.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            logger.error("Error al obtener labores docentes", e);
            throw new RuntimeException("Error al obtener labores docentes: " + e.getMessage(), e);
        } finally {            
            executor.shutdown();
        }
        return mapa;
    }
}
