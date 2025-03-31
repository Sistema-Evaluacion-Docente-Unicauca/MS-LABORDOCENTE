package co.edu.unicauca.laborDocente.api.service;

import co.edu.unicauca.laborDocente.api.dto.ActividadDTOTransformada;
import co.edu.unicauca.laborDocente.api.dto.ApiResponse;

import java.util.List;

/**
 * Servicio que define las operaciones para consultar la labor docente.
 */
public interface DocenteLaborService {
    ApiResponse<Void> procesarLaborDocente(Integer idFacultad, Integer idPeriodo, Integer idDepartamento);
}
