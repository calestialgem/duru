package duru;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/** Name of a package, which is a `.` separated identifier sequence. */
public record Namespace(String text) {
  /** Returns the root space, which is the project name. */
  public String root() {
    var separator = text.indexOf('.');
    if (separator == -1)
      return text;
    return text.substring(0, separator);
  }

  /** Returns the identifier sequence after the root. */
  public List<String> subspaces() {
    var separator = text.indexOf('.');
    if (separator == -1)
      return List.of();
    return List.of(text.substring(separator + 1).split(Pattern.quote(".")));
  }

  /** Resolves to a directory in the given path that corresponds to the
   * namespace given that the path corresponds to the root. */
  public Path resolveSubspaces(Path path) {
    for (var subspace : subspaces()) {
      path = path.resolve(subspace);
    }
    return path;
  }

  @Override
  public String toString() {
    return text;
  }
}
