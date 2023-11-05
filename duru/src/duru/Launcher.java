package duru;

import java.nio.file.Path;

/**
 * Holds the main function of the tool.
 */
final class Launcher {
  /**
   * Entrypoint of the tool.
   *
   * @param arguments Arguments that were passed from the command line.
   */
  public static void main(String[] arguments) {
    Initializer.initialize(Path.of("initTest"), "initTest");
  }

  /**
   * Hidden constructor.
   */
  private Launcher() {}
}
