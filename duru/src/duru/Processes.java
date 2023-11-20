package duru;

import java.io.IOException;
import java.util.Arrays;

public final class Processes {
  public static int execute(
    Object subject,
    boolean showOutput,
    Object... command)
  {
    var arguments = Arrays.stream(command).map(String::valueOf).toList();
    var builder   = new ProcessBuilder(arguments);
    if (showOutput) {
      builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
      builder.redirectError(ProcessBuilder.Redirect.INHERIT);
    }
    try {
      var process = builder.start();
      return process.waitFor();
    }
    catch (IOException | InterruptedException cause) {
      throw Diagnostic
        .failure(
          subject,
          cause,
          "could not execute `%s`",
          String.join(" ", arguments));
    }
  }

  private Processes() {}
}
