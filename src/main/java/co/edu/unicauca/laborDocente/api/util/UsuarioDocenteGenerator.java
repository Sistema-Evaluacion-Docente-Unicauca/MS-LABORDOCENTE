package co.edu.unicauca.laborDocente.api.util;

import co.edu.unicauca.laborDocente.api.client.SedClient;
import co.edu.unicauca.laborDocente.api.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * Generador de objetos UsuarioDocenteDTO a partir de informacionDocente de
 * KIRA.
 */
@Component
@RequiredArgsConstructor
public class UsuarioDocenteGenerator {

    private final SedClient sedClient;

    public List<UsuarioDocenteDTO> generarDesdeLabor(List<LaborDocenteDTO> labores) {
        List<UsuarioDocenteDTO> resultado = new ArrayList<>();
        Map<String, Integer> rolesPorNombre = sedClient.obtenerRoles();
        Integer rolDocente = rolesPorNombre.getOrDefault("DOCENTE", 1);

        for (LaborDocenteDTO labor : labores) {
            InformacionDocente info = labor.getInformacionDocente();
            if (info == null)
                continue;

            UsuarioDocenteDTO usuario = construirUsuario(info, rolDocente);
            resultado.add(usuario);
        }

        return resultado;
    }

    private UsuarioDocenteDTO construirUsuario(InformacionDocente info, Integer oidRol) {
        String[] partesNombre = dividirNombre(info.getNombreCompleto());

        UsuarioDocenteDTO usuario = new UsuarioDocenteDTO();
        usuario.setNombres(partesNombre[0]);
        usuario.setApellidos(partesNombre[1]);
        usuario.setCorreo(info.getCorreoInstitucional());
        usuario.setUsername(info.getUsername());
        usuario.setIdentificacion(info.getIdentificacion());
        usuario.setEstadoUsuario(new EstadoUsuarioDTO(1));
        usuario.setUsuarioDetalle(construirDetalle(info));
        usuario.setRoles(List.of(new RolDTO(oidRol)));

        return usuario;
    }

    private UsuarioDetalleDTO construirDetalle(InformacionDocente info) {
        return new UsuarioDetalleDTO(
                info.getFacultad() != null ? info.getFacultad().trim() : null,
                info.getDepartamento() != null ? info.getDepartamento().trim() : null,
                info.getCategoria() != null ? info.getCategoria().trim() : null,
                info.getTipoContratacion() != null ? info.getTipoContratacion().trim() : null,
                info.getDedicacion() != null ? info.getDedicacion().trim() : null,
                info.getNivelEstudios() != null ? info.getNivelEstudios().trim() : null
        );
    }    

    private String[] dividirNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return new String[] { "", "" };
        }
        String[] partes = nombreCompleto.trim().split(" ");
        if (partes.length < 2) {
            return new String[] { nombreCompleto, "" };
        }
        String apellidos = partes[partes.length - 2] + " " + partes[partes.length - 1];
        String nombres = String.join(" ", Arrays.copyOf(partes, partes.length - 2));
        return new String[] { nombres, apellidos };
    }
}
