package co.edu.unicauca.laborDocente.api.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataTerceroDTO {
    private Integer oidTercero;
    private String usuario;
    private Integer oidTipoIdentificacion;
    private String tipoIdentificacion;
    private String identificacion;
    private String primerApellido;
    private String segundoApellido;
    private String primerNombre;
    private String segundoNombre;
    private String correo;
    private String celular;
    private List<ProgramaDTO> programa;
}
