package duru;

import java.nio.file.Path;

/**
 * Provides the initialize command.
 *
 * @see #initialize(Path, String)
 */
final class Initializer {
  /**
   * Initializes a project in the given directory. Initialization is setting up
   * a new project in a directory.
   *
   * @param directory Directory where the project will be initialized at.
   * @param name      Name of the initialized project.
   */
  public static void initialize(Path directory, String name) {
    throw new MissingImplementationException();
  }
}
