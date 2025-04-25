package co.edu.unicauca.laborDocente.api.service;

import co.edu.unicauca.laborDocente.api.dto.ApiResponse;

/**
 * Servicio que define las operaciones para consultar la labor docente.
 */
public interface DocenteLaborService {
    ApiResponse<Void> procesarLaborDocente(Integer idFacultad, Integer idPeriodo, Integer idDepartamento, String token);
}
