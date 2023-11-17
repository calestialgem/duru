package duru;

public record Present<Value>(Value value) implements Optional<Value> {
  @Override
  public int length() {
    return 1;
  }

  @Override
  public Value get(int index) {
    return value;
  }
}
