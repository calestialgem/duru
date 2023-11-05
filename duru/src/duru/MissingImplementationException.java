package duru;

import java.io.Serial;

/**
 * Exception that is thrown when an implementation is missing. This is used for
 * writing an empty API and leaving it unimplemented until the changes caused by
 * the API are done. Hence, the amount of changes are limited leading to
 * incremental improvements.
 */
final class MissingImplementationException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = -7091900440604439526L;
}
