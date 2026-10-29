package sk.upjs.ics.vulpix_server.common.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sk.upjs.ics.exceptions.ApiExceptionCodes;

// Nest JS style exceptions
// https://docs.nestjs.com/exception-filters#custom-exceptions
@Getter
@RequiredArgsConstructor
public class HttpException extends RuntimeException {
  private final HttpStatus status;
  private final ApiExceptionCodes code;
}
