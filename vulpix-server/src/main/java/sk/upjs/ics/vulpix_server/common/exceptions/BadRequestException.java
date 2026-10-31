package sk.upjs.ics.vulpix_server.common.exceptions;

import org.springframework.http.HttpStatus;
import sk.upjs.ics.exceptions.ApiExceptionCodes;

public final class BadRequestException extends HttpException {
  public BadRequestException(ApiExceptionCodes code) {
    super(HttpStatus.BAD_REQUEST, code);
  }
}
