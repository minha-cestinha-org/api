package minhacestinha.api.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Resposta padrão de erro")
public record ErrorResponse(
        int status,
        String message,
        String details,
        String path,
        List<String> errors,
        LocalDateTime timestamp
) {

    public static ErrorResponse of(int status, String message, String details, String path, List<String> errors) {
        return new ErrorResponse(status, message, details, path, errors, LocalDateTime.now());
    }
}
