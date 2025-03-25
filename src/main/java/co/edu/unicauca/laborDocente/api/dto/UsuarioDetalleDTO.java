package co.edu.unicauca.laborDocente.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDetalleDTO {
    private String facultad;
    private String departamento;
    private String categoria;
    private String contratacion;
    private String dedicacion;
    private String estudios;
}