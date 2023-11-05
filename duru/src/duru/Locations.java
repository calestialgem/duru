package duru;

import java.nio.file.Path;

/** Tool that finds the portion of a source file. */
public final class Locations {
  /** Returns the location of the given portion with the path prefix. */
  public static String locate(Path file, String contents, int start, int end) {
    var buffer = new StringBuilder();
    buffer.append(file.toAbsolutePath().normalize());
    buffer.append(':');
    appendLocation(contents, start, end, buffer);
    return buffer.toString();
  }

  /** Appends the given location to the given buffer. */
  private static void appendLocation(
    String contents,
    int start,
    int end,
    StringBuilder buffer)
  {
    var line   = 1;
    var column = 1;
    var index  = 0;
    for (; index < start; index = contents.offsetByCodePoints(index, 1)) {
      if (contents.codePointAt(index) == '\n') {
        line++;
        column = 1;
      }
      else {
        column++;
      }
    }
    buffer.append(line);
    buffer.append(':');
    buffer.append(column);
    if (end - start > 1) {
      for (; index < end; index = contents.offsetByCodePoints(index, 1)) {
        if (contents.codePointAt(index) == '\n') {
          line++;
          column = 1;
        }
        else {
          column++;
        }
      }
      buffer.append(':');
      buffer.append(line);
      buffer.append(':');
      buffer.append(column);
    }
  }

  /** Constructor. */
  private Locations() {}
}
