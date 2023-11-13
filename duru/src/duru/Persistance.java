package duru;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Persistance {
  public static void recreate(Path directory) {
    if (Files.exists(directory))
      delete(directory);
    create(directory);
  }

  private static void delete(Path directory) {
    throw Exceptions.unimplemented();
  }

  private static void create(Path directory) {
    throw Exceptions.unimplemented();
  }

  public static void enter(Path directory) {
    throw Exceptions.unimplemented();
  }

  private Persistance() {}
}
