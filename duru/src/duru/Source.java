package duru;

import java.nio.file.Path;

public record Source(Path path, String contents) {
  @Override
  public String toString() {
    return path.toString();
  }
}
