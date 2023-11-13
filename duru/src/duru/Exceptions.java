package duru;

import java.io.IOException;
import java.nio.file.Path;

public final class Exceptions {
  public static RuntimeException unimplemented() {
    return new RuntimeException("fatal error: Unimplemented!");
  }

  public static RuntimeException io(
    IOException cause,
    Path subject,
    String format,
    Object... arguments)
  {
    return new RuntimeException(
      "%s: failure: %s".formatted(subject, format.formatted(arguments)),
      cause);
  }

  private Exceptions() {}
}
