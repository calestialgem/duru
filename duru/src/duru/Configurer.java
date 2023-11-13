package duru;

import java.nio.file.Path;
import java.util.Optional;

public final class Configurer {
  public static Configuration.Project configure(Path file) {
    var configurer = new Configurer(file);
    return configurer.configure();
  }

  private final Path                           file;
  private Optional<Configuration.Identifier>   name;
  private ListBuffer<Configuration.Executable> executables;
  private String                               text;
  private int                                  index;

  private Configurer(Path file) {
    this.file = file;
  }

  private Configuration.Project configure() {
    text        = Persistance.read(file);
    name        = Optional.empty();
    executables = ListBuffer.create();
    index       = 0;
    return new Configuration.Project(
      -1,
      index,
      name.get(),
      executables.toList());
  }
}
