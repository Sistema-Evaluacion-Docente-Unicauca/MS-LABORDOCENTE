package co.edu.unicauca.laborDocente.api.service;

import co.edu.unicauca.laborDocente.api.client.KiraClient;
import co.edu.unicauca.laborDocente.api.client.SedClient;
import co.edu.unicauca.laborDocente.api.dto.ActividadDTO;
import co.edu.unicauca.laborDocente.api.dto.ActividadDTOTransformada;
import co.edu.unicauca.laborDocente.api.dto.DocenteDTO;
import co.edu.unicauca.laborDocente.api.dto.LaborDocenteDTO;
import co.edu.unicauca.laborDocente.api.util.ActividadTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio que orquesta la carga de labores docentes y su transformación para el sistema SED.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocenteLaborServiceImpl implements DocenteLaborService {

    private final KiraClient kiraClient;
    private final SedClient sedClient;
    private final ActividadTransformer actividadTransformer;

    @Override
    public List<ActividadDTOTransformada> cargarLaborDocente(Long idFacultad, Long idPeriodo, Long idDepartamento) {
        Map<String, Long> atributos = sedClient.obtenerAtributos();
        Map<String, Long> tiposActividad = sedClient.obtenerTiposActividad();
        List<ActividadDTOTransformada> resultado = new ArrayList<>();

        List<Long> departamentos = kiraClient.obtenerDepartamentos(idFacultad, idDepartamento);

        for (Long idDepto : departamentos) {
            List<DocenteDTO> docentes = kiraClient.obtenerDocentes(idDepto, idPeriodo);

            for (DocenteDTO docente : docentes) {
                LaborDocenteDTO labor = kiraClient.obtenerLabor(docente.getId());
                if (labor != null) {
                    List<ActividadDTO> actividadesOriginales = labor.getActividades();
                    List<ActividadDTOTransformada> actividadesTransformadas = actividadTransformer.transformar(
                            actividadesOriginales,
                            atributos,
                            tiposActividad,
                            null,
                            docente.getIdentificacion()
                    );
                    resultado.addAll(actividadesTransformadas);
                }
            }
        }

        return resultado;
    }
}    
