package duru;

public record Absent() implements Optional<Object> {
  @Override
  public int length() {
    return 0;
  }

  @Override
  public Object get(int index) {
    throw Subject.failure("value is absent");
  }
}
