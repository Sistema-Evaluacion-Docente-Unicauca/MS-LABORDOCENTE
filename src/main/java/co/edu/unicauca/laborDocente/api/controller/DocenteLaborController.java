package co.edu.unicauca.laborDocente.api.controller;

import co.edu.unicauca.laborDocente.api.dto.ActividadDTOTransformada;
import co.edu.unicauca.laborDocente.api.dto.ApiResponse;
import co.edu.unicauca.laborDocente.api.dto.UsuarioDocenteDTO;
import co.edu.unicauca.laborDocente.api.service.DocenteLaborService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador encargado de exponer el endpoint para consultar la labor docente.
 */
@RestController
@RequestMapping("/api/labor-docente")
@RequiredArgsConstructor
public class DocenteLaborController {

    private final DocenteLaborService docenteLaborService;

    /**
     * Consulta la labor docente según los parámetros recibidos.
     *
     * @param idFacultad     ID de la facultad (obligatorio)
     * @param idPeriodo      ID del periodo académico (obligatorio)
     * @param idDepartamento ID del departamento (opcional)
     * @return lista de labores docentes encontradas
     */
    @GetMapping
    public ResponseEntity<List<ActividadDTOTransformada>> cargarLaborDocente(
            @RequestParam Integer idFacultad,
            @RequestParam Integer idPeriodo,
            @RequestParam(required = false) Integer idDepartamento) {

        List<ActividadDTOTransformada> resultado = docenteLaborService.cargarLaborDocente(idFacultad, idPeriodo, idDepartamento);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/usuarios-docentes")
    public ResponseEntity<ApiResponse<Void>> guardarUsuariosDocentes(
            @RequestParam Integer idFacultad,
            @RequestParam Integer idPeriodo,
            @RequestParam(required = false) Integer idDepartamento) {

        ApiResponse<Void> respuesta = docenteLaborService.generarUsuariosDocentes(idFacultad, idPeriodo, idDepartamento);
        return ResponseEntity.status(respuesta.getCodigo()).body(respuesta);
    }
}
