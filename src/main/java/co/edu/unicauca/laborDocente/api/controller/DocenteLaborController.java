package co.edu.unicauca.laborDocente.api.controller;

import co.edu.unicauca.laborDocente.api.dto.ApiResponse;
import co.edu.unicauca.laborDocente.api.service.DocenteLaborService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador encargado de exponer el endpoint para consultar la labor docente.
 */
@RestController
@RequestMapping("/api/labor-docente")
@RequiredArgsConstructor
public class DocenteLaborController {

    private final DocenteLaborService docenteLaborService;

    /**
     * Realiza la carga de labor docente y creación de usuarios docentes a partir de la información de KIRA y la guarda en el sistema SED.
     *
     * @param idFacultad     ID de la facultad (obligatorio)
     * @param idPeriodo      ID del periodo académico (obligatorio)
     * @param idDepartamento ID del departamento (opcional)
     * @return ApiResponse con mensaje de estado
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> procesarLaborDocente(
            @RequestParam Integer idFacultad,
            @RequestParam Integer idPeriodo,
            @RequestParam(required = false) Integer idDepartamento,
            HttpServletRequest request) {
    
        String token = request.getHeader("Authorization");
        ApiResponse<Void> respuesta = docenteLaborService.procesarLaborDocente(idFacultad, idPeriodo, idDepartamento, token);
        return ResponseEntity.status(respuesta.getCodigo()).body(respuesta);
    }
}
