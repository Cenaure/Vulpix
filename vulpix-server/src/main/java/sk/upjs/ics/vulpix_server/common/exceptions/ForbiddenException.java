package sk.upjs.ics.vulpix_server.common.exceptions;

import org.springframework.http.HttpStatus;
import sk.upjs.ics.exceptions.ApiExceptionCodes;

public final class ForbiddenException extends HttpException {
  public ForbiddenException(ApiExceptionCodes code) {
    super(HttpStatus.FORBIDDEN, code);
  }
}
