package duru;

public record List<E>(E[] elements) implements ListLike {
  @Override
  public int length() {
    return elements.length;
  }

  @Override
  public Object get(int index) {
    return elements[index];
  }
}
