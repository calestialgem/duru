package duru.source;

import java.nio.file.Path;

/** Representation of a source file as its contents. */
public record Source(Path path, String contents) {
  /** Constructs. */
  public Source {
    path = path.toAbsolutePath().normalize();
  }
}
