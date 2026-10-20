package sk.upjs.ics.application.runners;

import lombok.Getter;

@Getter
public class RunnerException extends RuntimeException {
  RunnerExceptionCodes code;
  Object[] args;

  public RunnerException(RunnerExceptionCodes code, Object... args) {
    super(code.name());
    this.code = code;
    this.args = args;
  }

  public RunnerException(RunnerExceptionCodes code, Throwable cause, Object... args) {
    super(code.name(), cause);
    this.code = code;
    this.args = args;
  }
}
