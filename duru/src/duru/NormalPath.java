package duru;

import java.nio.file.Path;

record NormalPath(Path value) {
  public static NormalPath of(Path value) {
    return new NormalPath(value.toAbsolutePath().normalize());
  }

  public NormalPath resolve(String filename) {
    return new NormalPath(value.resolve(filename));
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
