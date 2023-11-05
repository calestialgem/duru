package duru;

import java.nio.file.Path;

/**
 * Provides the initialize command.
 *
 * @see #initialize(Path, String)
 */
final class Initializer {
  /**
   * Calls {@link #initialize(Path, String)} with the name of the directory as
   * the project name.
   *
   * @param directory Directory where the project will be initialized at.
   */
  public static void initialize(Path directory) {
    initialize(directory, directory.getFileName().toString());
  }

  /**
   * Initializes a project in the given directory. Initialization is setting up
   * a new project in a directory.
   *
   * @param directory Directory where the project will be initialized at.
   * @param name      Name of the initialized project.
   */
  public static void initialize(Path directory, String name) {
    createConfiguration(directory, name);
    createMainSource(directory);
  }

  private static void createConfiguration(Path directory, String name) {
    throw new MissingImplementationException();
  }

  private static void createMainSource(Path directory) {
    throw new MissingImplementationException();
  }
}
