package duru;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Initializer {
  public static void initialize(Path directory) {
    for (var i = directory; i != null; i = i.getParent()) {
      var config = i.resolve("project.duru");
      if (Files.exists(config)) {
        throw new RuntimeException(
          "%s: error: Could not initialize inside another project, which is configured at `%s`!"
            .formatted(directory, config));
      }
    }
    throw Exceptions.unimplemented();
  }

  private Initializer() {}
}
