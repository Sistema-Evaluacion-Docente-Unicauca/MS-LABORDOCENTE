package co.edu.unicauca.laborDocente.api.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Utilidad para generar encabezados HTTP con token JWT.
 */
public class AuthHeaderUtil {

    public static HttpEntity<Void> crearRequest(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(limpiarToken(token));
        return new HttpEntity<>(headers);
    }

    public static <T> HttpEntity<T> crearRequest(String token, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(limpiarToken(token));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private static String limpiarToken(String token) {
        return token.replace("Bearer ", "").replace("\"", "").trim();
    }
}
