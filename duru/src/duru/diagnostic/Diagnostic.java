package duru.diagnostic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Diagnostic {
  public static Diagnostic create() {
    var diagnostic = new Diagnostic(new ArrayList<>(), new ArrayList<>());
    return diagnostic;
  }

  private final List<Integer> causes;

  private final List<Message> messages;

  private Diagnostic(List<Integer> causes, List<Message> messages) {
    this.causes = causes;
    this.messages = messages;
  }

  public void exception(Throwable throwable) {
    begin();
    if (throwable.getCause() != null) { exception(throwable.getCause()); }
    failure(throwable.getMessage());
  }

  public void begin() { causes.add(0); }

  public void skip() {
    int added_causes = causes.removeLast();
    if (!causes.isEmpty()) { causes.add(causes.removeLast() + added_causes); }
  }

  public void failure(String subject, String format, Object... arguments) {
    failure(Optional.of(subject), format, arguments);
  }

  public void failure(String format, Object... arguments) {
    failure(Optional.empty(), format, arguments);
  }

  private void failure(
    Optional<String> subject,
    String format,
    Object... arguments)
  {
    var type = new MessageType("failure", true);
    add_message(type, subject, format, arguments);
  }

  public void error(String subject, String format, Object... arguments) {
    error(Optional.of(subject), format, arguments);
  }

  public void error(String format, Object... arguments) {
    failure(Optional.empty(), format, arguments);
  }

  private void error(
    Optional<String> subject,
    String format,
    Object... arguments)
  {
    var type = new MessageType("error", true);
    add_message(type, subject, format, arguments);
  }

  public void warning(String subject, String format, Object... arguments) {
    warning(Optional.of(subject), format, arguments);
  }

  public void warning(String format, Object... arguments) {
    failure(Optional.empty(), format, arguments);
  }

  private void warning(
    Optional<String> subject,
    String format,
    Object... arguments)
  {
    var type = new MessageType("warning", false);
    add_message(type, subject, format, arguments);
  }

  public void info(String subject, String format, Object... arguments) {
    info(Optional.of(subject), format, arguments);
  }

  public void info(String format, Object... arguments) {
    failure(Optional.empty(), format, arguments);
  }

  private void info(
    Optional<String> subject,
    String format,
    Object... arguments)
  {
    var type = new MessageType("info", false);
    add_message(type, subject, format, arguments);
  }

  private void add_message(
    MessageType type,
    Optional<String> subject,
    String format,
    Object... arguments)
  {
    var message =
      new Message(
        type,
        subject,
        format.formatted(arguments),
        causes.removeLast());
    if (!causes.isEmpty()) { causes.add(causes.removeLast() + 1); }
    messages.add(message);
  }

  public void append(Appendable appendable) throws IOException {
    var remaining_causes = new ArrayList<Integer>();
    for (var message : messages.reversed()) {
      appendable.append("  ".repeat(remaining_causes.size()));
      if (message.subject().isPresent()) {
        appendable.append(message.subject().get());
        appendable.append(": ");
      }
      appendable.append(message.type().name());
      appendable.append(": ");
      appendable.append(message.body());
      appendable.append(System.lineSeparator());
      if (message.causes() != 0) {
        remaining_causes.add(message.causes());
        continue;
      }
      if (!remaining_causes.isEmpty()) {
        int remaining = remaining_causes.removeLast() - 1;
        if (remaining != 0) { remaining_causes.add(remaining); }
      }
    }
  }

  public boolean is_fatal() {
    for (var message : messages) {
      if (message.type().is_fatal()) { return true; }
    }
    return false;
  }
}
