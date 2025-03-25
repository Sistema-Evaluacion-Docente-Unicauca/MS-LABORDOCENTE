package co.edu.unicauca.laborDocente.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDocenteDTO {
    private String nombres;
    private String apellidos;
    private String correo;
    private String username;
    private String identificacion;
    private EstadoUsuarioDTO estadoUsuario;
    private UsuarioDetalleDTO usuarioDetalle;
    private List<RolDTO> roles;
}