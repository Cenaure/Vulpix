package sk.upjs.ics.vulpix_server.common.exceptions;

import org.springframework.http.HttpStatus;
import sk.upjs.ics.exceptions.ApiExceptionCodes;

public final class NotFoundException extends HttpException {
  public NotFoundException(ApiExceptionCodes code) {
    super(HttpStatus.NOT_FOUND, code);
  }
}
