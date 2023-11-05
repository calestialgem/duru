package duru;

import java.io.Serial;
import java.io.Serializable;

/**
 * Exception that is thrown when an implementation is missing. This is used for
 * writing an empty API and leaving it unimplemented until the changes caused by
 * the API are done. Hence, the amount of changes are limited leading to
 * incremental improvements.
 */
final class MissingImplementationException extends RuntimeException {
  /**
   * Integer that identifies this type's instances when they are deserialized.
   * Required for all {@link Exception}s because they implement
   * {@link Serializable}.
   */
  @Serial
  private static final long serialVersionUID = -7091900440604439526L;
}
