package duru;

final class ListBuffer<E> implements ListLike<E> {
  private E[] elements;
  private int length;

  @Override
  public int length() {
    return length;
  }

  @Override
  public E get(int index) {
    return elements[index];
  }
}
