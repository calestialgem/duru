package duru;

public record Subject(String name) {
  private static ListBuffer<Subject> subjects;

  static {
    subjects = ListBuffer.create();
  }

  public static Subject get() {
    return subjects.get();
  }

  public static void add(Object name) {
    add(new Subject(name.toString()));
  }

  public static void add(Subject subject) {
    subjects.add(subject);
  }

  public static Subject remove() {
    return subjects.remove();
  }

  public Diagnostic diagnose(String title, String format, Object... arguments) {
    return new Diagnostic(
      "%s: %s: %s".formatted(name, title, format.formatted(arguments)));
  }
}
