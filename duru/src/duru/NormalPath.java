package duru;

import java.nio.file.Path;

record NormalPath(Path value) {
  public static NormalPath of(Path value) {
    return new NormalPath(value.toAbsolutePath().normalize());
  }
}
