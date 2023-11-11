package duru;

public record List<E>(E[] elements) implements ListLike<E> {
  @Override
  public int length() {
    return elements.length;
  }

  @Override
  public E get(int index) {
    return elements[index];
  }
}
