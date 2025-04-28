package co.edu.unicauca.laborDocente.api.util;

import co.edu.unicauca.laborDocente.api.client.KiraClient;
import co.edu.unicauca.laborDocente.api.client.SedClient;
import co.edu.unicauca.laborDocente.api.dto.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generador de objetos UsuarioDocenteDTO a partir de informacionDocente de KIRA.
 */
@Component
@RequiredArgsConstructor
public class UsuarioDocenteGenerator {

    private final SedClient sedClient;
    private final KiraClient kiraUsuarioService;

    private static final Logger logger = LoggerFactory.getLogger(UsuarioDocenteGenerator.class);

    public List<UsuarioDocenteDTO> generarDesdeLabor(List<LaborDocenteDTO> labores, String token) {
        List<UsuarioDocenteDTO> resultado = new ArrayList<>();

        try {
            Map<String, Integer> rolesPorNombre = sedClient.obtenerRoles(token);

            for (LaborDocenteDTO labor : labores) {
                InformacionDocente info = labor.getInformacionDocente();
                if (info == null || info.getUsername() == null || info.getUsername().isEmpty())
                    continue;

                List<String> rolesActivos = kiraUsuarioService.obtenerRolesActivosPorUsername(info.getUsername());

                List<RolDTO> roles = rolesActivos.stream()
                        .map(rolNombre -> {
                            Integer idRol = rolesPorNombre.get(rolNombre.toUpperCase());
                            if (idRol != null) {
                                return new RolDTO(idRol);
                            } else {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(ArrayList::new));

                if (roles.isEmpty()) {
                    Integer rolDocente = rolesPorNombre.getOrDefault("DOCENTE", 1);
                    roles.add(new RolDTO(rolDocente));
                }

                UsuarioDocenteDTO usuario = construirUsuario(info, roles);
                resultado.add(usuario);
            }
        } catch (Exception e) {
            logger.error("Error al generar usuarios desde labor", e);
            throw new RuntimeException("Error al generar usuarios desde labor: " + e.getMessage(), e);
        }
        return resultado;

    }

    private UsuarioDocenteDTO construirUsuario(InformacionDocente info, List<RolDTO> roles) {
        try {
            String[] partesNombre = dividirNombre(info.getNombreCompleto());
    
            UsuarioDocenteDTO usuario = new UsuarioDocenteDTO();
            usuario.setNombres(partesNombre[0]);
            usuario.setApellidos(partesNombre[1]);
            usuario.setCorreo(info.getCorreoInstitucional());
            usuario.setUsername(info.getUsername());
            usuario.setIdentificacion(info.getIdentificacion());
            usuario.setEstadoUsuario(new EstadoUsuarioDTO(1));
            usuario.setUsuarioDetalle(construirDetalle(info));
            usuario.setRoles(roles);
            return usuario;
        } catch (Exception e) {
            logger.error("Error al construir usuario", e);
            throw new RuntimeException("Error al construir usuario: " + e.getMessage(), e);
        }
    }    

    private UsuarioDetalleDTO construirDetalle(InformacionDocente info) {
        try {
            return new UsuarioDetalleDTO(
                    info.getFacultad() != null ? info.getFacultad().trim() : null,
                    info.getDepartamento() != null ? info.getDepartamento().trim() : null,
                    info.getCategoria() != null ? info.getCategoria().trim() : null,
                    info.getTipoContratacion() != null ? info.getTipoContratacion().trim() : null,
                    info.getDedicacion() != null ? info.getDedicacion().trim() : null,
                    info.getNivelEstudios() != null ? info.getNivelEstudios().trim() : null
            );
        } catch (Exception e) {
            logger.error("Error al construir detalle de usuario", e);
            throw new RuntimeException("Error al construir detalle de usuario: " + e.getMessage(), e);
        }
    }    

    private String[] dividirNombre(String nombreCompleto) {
        try {
            if (nombreCompleto == null || nombreCompleto.isBlank()) {
                return new String[]{"", ""};
            }
            String[] partes = nombreCompleto.trim().split(" ");
            if (partes.length < 2) {
                return new String[]{nombreCompleto, ""};
            }
            String apellidos = partes[partes.length - 2] + " " + partes[partes.length - 1];
            String nombres = String.join(" ", Arrays.copyOf(partes, partes.length - 2));
            return new String[]{nombres, apellidos};
        } catch (Exception e) {
            logger.error("Error al dividir nombre completo", e);
            throw new RuntimeException("Error al dividir nombre completo: " + e.getMessage(), e);
        }
    }
}
