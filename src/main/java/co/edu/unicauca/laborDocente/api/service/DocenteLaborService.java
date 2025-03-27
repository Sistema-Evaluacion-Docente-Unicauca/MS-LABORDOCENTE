package co.edu.unicauca.laborDocente.api.service;

import co.edu.unicauca.laborDocente.api.dto.ActividadDTOTransformada;
import co.edu.unicauca.laborDocente.api.dto.ApiResponse;

import java.util.List;

/**
 * Servicio que define las operaciones para consultar la labor docente.
 */
public interface DocenteLaborService {

    /**
     * Consulta las labores docentes según la facultad, el periodo y opcionalmente
     * un departamento.
     *
     * @param idFacultad     ID de la facultad
     * @param idPeriodo      ID del periodo académico
     * @param idDepartamento ID del departamento (opcional)
     * @return lista de labores docentes encontradas
     */
    ApiResponse<Void> cargarLaborDocente(Integer idFacultad, Integer idPeriodo, Integer idDepartamento);

    ApiResponse<Void> generarUsuariosDocentes(Integer idFacultad, Integer idPeriodo, Integer idDepartamento);
}
