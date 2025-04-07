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

@Service
@RequiredArgsConstructor
@Slf4j
public class DocenteLaborServiceImpl implements DocenteLaborService {

    private final KiraClient kiraClient;
    private final SedClient sedClient;
    private final ActividadTransformer actividadTransformer;
    private final UsuarioDocenteGenerator usuarioDocenteGenerator;

    @Transactional
    @Override
    public ApiResponse<Void> procesarLaborDocente(Integer idFacultad, Integer idPeriodo, Integer idDepartamento) {
        List<Integer> departamentos = kiraClient.obtenerDepartamentos(idFacultad, idDepartamento);
        Map<Integer, List<DocenteDTO>> docentesPorDepartamento = obtenerDocentesPorDepartamento(departamentos, idPeriodo);
        Map<Integer, LaborDocenteDTO> labores = obtenerLabores(docentesPorDepartamento);

        // Generar usuarios docentes
        ApiResponse<Void> usuariosResponse = generarUsuariosDocentes(new ArrayList<>(labores.values()));

        // Generar actividades
        ApiResponse<Void> actividadesResponse = cargarActividades(labores);

        if (usuariosResponse.getCodigo() != 200 || actividadesResponse.getCodigo() != 200) {
            String error = usuariosResponse.getMensaje() + " | " + actividadesResponse.getMensaje();
            return new ApiResponse<>(207, "Error parcial: " + error, null);
        }

        return new ApiResponse<>(200, usuariosResponse.getMensaje() + " | " + actividadesResponse.getMensaje(), null);
    }

    private Map<Integer, List<DocenteDTO>> obtenerDocentesPorDepartamento(List<Integer> departamentos, Integer idPeriodo) {
        Map<Integer, List<DocenteDTO>> mapa = new HashMap<>();
        for (Integer idDepto : departamentos) {
            mapa.put(idDepto, kiraClient.obtenerDocentes(idDepto, idPeriodo));
        }
        return mapa;
    }

    private Map<Integer, LaborDocenteDTO> obtenerLabores(Map<Integer, List<DocenteDTO>> docentesPorDepto) {
        Map<Integer, LaborDocenteDTO> mapa = new HashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Void>> tareas = new ArrayList<>();

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
        executor.shutdown();
        return mapa;
    }

    private ApiResponse<Void> generarUsuariosDocentes(List<LaborDocenteDTO> labores) {
        List<UsuarioDocenteDTO> usuarios = usuarioDocenteGenerator.generarDesdeLabor(labores);
        String mensaje = sedClient.guardarUsuarios(usuarios);
        return new ApiResponse<>(200, mensaje, null);
    }

    private ApiResponse<Void> cargarActividades(Map<Integer, LaborDocenteDTO> labores) {
        Map<String, Integer> atributos = sedClient.obtenerAtributos();
        Map<String, Integer> tiposActividad = sedClient.obtenerTiposActividad();
        List<ActividadDTOTransformada> transformadas = new ArrayList<>();

        labores.forEach((idDocente, labor) -> {
            List<ActividadDTO> actividades = labor.getActividades();
            if (actividades != null && !actividades.isEmpty()) {
                transformadas.addAll(actividadTransformer.transformar(
                        actividades,
                        atributos,
                        tiposActividad,
                        0,
                        Integer.valueOf(labor.getInformacionDocente().getIdentificacion())
                ));
            }
        });

        String mensaje = sedClient.guardarActividades(transformadas);
        return new ApiResponse<>(200, mensaje, null);
    }
}
