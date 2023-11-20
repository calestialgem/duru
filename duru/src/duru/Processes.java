package duru;

import java.io.IOException;

public final class Processes {
  public static int execute(
    Object subject,
    boolean showOutput,
    Object... arguments)
  {
    var combinedCommand = ListBuffer.<String>create();
    for (var argument : arguments)
      combinedCommand.add(argument.toString());
    return execute(subject, showOutput, combinedCommand.toList());
  }

  public static int execute(
    Object subject,
    boolean showOutput,
    Object command,
    List<String> arguments)
  {
    var combinedCommand = ListBuffer.<String>create();
    combinedCommand.add(command.toString());
    combinedCommand.addAll(arguments);
    return execute(subject, showOutput, combinedCommand.toList());
  }

  public static int execute(
    Object subject,
    boolean showOutput,
    List<String> arguments)
  {
    var builder = new ProcessBuilder(arguments.toArray(String[]::new));
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
