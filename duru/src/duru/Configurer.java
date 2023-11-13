package duru;

import java.nio.file.Path;

public final class Configurer {
  public static Configuration.Project configure(Path file) {
    var text = Persistance.read(file);
    throw Diagnostic.error("unimplemented");
  }

  private Configurer() {}
}
