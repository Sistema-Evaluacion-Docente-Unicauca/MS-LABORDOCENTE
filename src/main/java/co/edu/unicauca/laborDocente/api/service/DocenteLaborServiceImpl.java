package co.edu.unicauca.laborDocente.api.service;

import co.edu.unicauca.laborDocente.api.client.KiraClient;
import co.edu.unicauca.laborDocente.api.client.SedClient;
import co.edu.unicauca.laborDocente.api.dto.ActividadDTO;
import co.edu.unicauca.laborDocente.api.dto.ActividadDTOTransformada;
import co.edu.unicauca.laborDocente.api.dto.ApiResponse;
import co.edu.unicauca.laborDocente.api.dto.DocenteDTO;
import co.edu.unicauca.laborDocente.api.dto.LaborDocenteDTO;
import co.edu.unicauca.laborDocente.api.dto.UsuarioDocenteDTO;
import co.edu.unicauca.laborDocente.api.util.ActividadTransformer;
import co.edu.unicauca.laborDocente.api.util.UsuarioDocenteGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servicio que orquesta la carga de labores docentes y su transformación para
 * el sistema SED.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocenteLaborServiceImpl implements DocenteLaborService {

    private final KiraClient kiraClient;
    private final SedClient sedClient;
    private final ActividadTransformer actividadTransformer;
    private final UsuarioDocenteGenerator usuarioDocenteGenerator;

    @Override
    public ApiResponse<Void> cargarLaborDocente(Integer idFacultad, Integer idPeriodo, Integer idDepartamento) {
        Map<String, Integer> atributos = sedClient.obtenerAtributos();
        Map<String, Integer> tiposActividad = sedClient.obtenerTiposActividad();
        List<ActividadDTOTransformada> resultado = new ArrayList<>();

        List<Integer> departamentos = kiraClient.obtenerDepartamentos(idFacultad, idDepartamento);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<List<ActividadDTOTransformada>>> futuros = new ArrayList<>();

        for (Integer idDepto : departamentos) {
            List<DocenteDTO> docentes = kiraClient.obtenerDocentes(idDepto, idPeriodo);

            for (DocenteDTO docente : docentes) {
                CompletableFuture<List<ActividadDTOTransformada>> futuro = CompletableFuture.supplyAsync(() -> {
                    LaborDocenteDTO labor = kiraClient.obtenerLabor(docente.getId());
                    if (labor == null)
                        return List.of();

                    return actividadTransformer.transformar(
                            labor.getActividades(),
                            atributos,
                            tiposActividad,
                            0,
                            Integer.valueOf(docente.getIdentificacion()));
                }, executor);

                futuros.add(futuro);
            }
        }

        futuros.stream()
                .map(CompletableFuture::join)
                .forEach(resultado::addAll);

        executor.shutdown();

        String mensaje = sedClient.guardarActividades(resultado);

        String resumen = String.format("%s Carga realizada para facultad %d, periodo %d y departamento %s.",
                mensaje, idFacultad, idPeriodo, idDepartamento != null ? idDepartamento : "TODOS");

        return new ApiResponse<>(200, resumen, null);
    }


    @Override
    public ApiResponse<Void> generarUsuariosDocentes(Integer idFacultad, Integer idPeriodo, Integer idDepartamento) {
        List<Integer> departamentos = kiraClient.obtenerDepartamentos(idFacultad, idDepartamento);
        ExecutorService executor = Executors.newFixedThreadPool(10);

        List<CompletableFuture<LaborDocenteDTO>> futuros = new ArrayList<>();

        for (Integer idDepto : departamentos) {
            List<DocenteDTO> docentes = kiraClient.obtenerDocentes(idDepto, idPeriodo);

            for (DocenteDTO docente : docentes) {
                CompletableFuture<LaborDocenteDTO> futuro = CompletableFuture.supplyAsync(
                        () -> kiraClient.obtenerLabor(docente.getId()), executor);
                futuros.add(futuro);
            }
        }

        // Esperar resultados y filtrar nulos
        List<LaborDocenteDTO> laboresObtenidas = futuros.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();

        executor.shutdown();

        List<UsuarioDocenteDTO> usuarios = usuarioDocenteGenerator.generarDesdeLabor(laboresObtenidas);

        // Enviar usuarios a SED y obtener mensaje
        String mensajeRespuesta = sedClient.guardarUsuarios(usuarios);

        return new ApiResponse<>(200, mensajeRespuesta, null);
    }
}
