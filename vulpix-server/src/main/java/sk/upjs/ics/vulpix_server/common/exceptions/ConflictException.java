package sk.upjs.ics.vulpix_server.common.exceptions;

import org.springframework.http.HttpStatus;
import sk.upjs.ics.exceptions.ApiExceptionCodes;

public final class ConflictException extends HttpException {
  public ConflictException(ApiExceptionCodes code) {
    super(HttpStatus.CONFLICT, code);
  }
}
