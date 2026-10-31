package sk.upjs.ics.vulpix_server.common.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class HttpExceptionHandler {

  @ExceptionHandler(HttpException.class)
  public ResponseEntity<ApiException> handle(HttpException exception) {
    return ResponseEntity.status(exception.getStatus())
        .body(new ApiException(exception.getCode().name()));
  }
}
