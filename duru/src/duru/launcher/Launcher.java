package duru.launcher;

import java.io.IOException;
import java.nio.file.Path;

import duru.Configuration;
import duru.Duru;
import duru.diagnostic.Diagnostic;

final class Launcher {
  public static void main(String... arguments) throws IOException {
    var diagnostic = Diagnostic.create();
    var configuration = Configuration.create();
    configuration.set_workspace(Path.of("tests", "entrypoint"));
    if (Duru.init(diagnostic, configuration)) {
      diagnostic.append(System.err);
      System.exit(1);
    }
  }
}
