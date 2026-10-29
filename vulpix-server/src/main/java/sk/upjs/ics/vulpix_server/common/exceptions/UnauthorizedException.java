package sk.upjs.ics.vulpix_server.common.exceptions;

import org.springframework.http.HttpStatus;
import sk.upjs.ics.exceptions.ApiExceptionCodes;

public final class UnauthorizedException extends HttpException {
  public UnauthorizedException(ApiExceptionCodes code) {
    super(HttpStatus.UNAUTHORIZED, code);
  }
}
