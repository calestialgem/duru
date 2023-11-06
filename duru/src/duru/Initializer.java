package duru;

import java.nio.file.Files;

final class Initializer {
  private static final String CONFIGURATION_NAME = "project.duru";
  private static final String SOURCES_NAME       = "src";
  private static final String MAIN_FILE_NAME     = "main.duru";

  public static Result<Void> initialize(NormalPath directory) {
    return Result
      .perform(() -> Identifier.of(directory.value().getFileName().toString()))
      .perform(name -> initialize(directory, name));
  }

  public static Result<Void> initialize(NormalPath directory, Identifier name) {
    for (var i = directory.value(); i != null; i = i.getParent()) {
      var configuration = i.resolve(CONFIGURATION_NAME);
      if (Files.exists(configuration)) {
        return Result
          .failure(
            "Cannot initialize inside another project, which is defined by `%s`!",
            configuration);
      }
    }
    var sources = directory.resolve(SOURCES_NAME);
    return Result
      .perform(() -> Persistance.store(directory.resolve(CONFIGURATION_NAME), """
project %s {
  executable %s;
}
""", name, name))
      .perform(v -> Persistance.create(sources))
      .perform(v -> Persistance.store(sources.resolve(MAIN_FILE_NAME), """
entrypoint {
  duru.print("Hello, World!\\n");
}
"""));
  }
}
