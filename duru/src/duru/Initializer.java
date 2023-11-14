package duru;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Initializer {
  public static void initialize(Path directory) {
    var initializer = new Initializer(directory);
    initializer.initialize();
  }

  private final Path directory;
  private String     name;

  private Initializer(Path directory) {
    this.directory = directory;
  }

  private void initialize() {
    checkAvailability();
    name = directory.getFileName().toString();
    checkName();
    createConfiguration();
    createMainSource();
  }

  private void checkAvailability() {
    for (var i = directory; i != null; i = i.getParent()) {
      var configuration = i.resolve("module.duru");
      if (Files.exists(configuration))
        throw Subject
          .get()
          .diagnose(
            "error",
            "initializing in module defined by `%s`",
            configuration)
          .exception();
    }
  }

  private void checkName() {
    throw Subject.get().diagnose("failure", "unimplemented").exception();
  }

  private void createConfiguration() {
    throw Subject.get().diagnose("failure", "unimplemented").exception();
  }

  private void createMainSource() {
    throw Subject.get().diagnose("failure", "unimplemented").exception();
  }
}
