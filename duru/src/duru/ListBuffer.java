package duru;

final class ListBuffer<E> implements ListLike<E> {
  private E[] elements;
  private int length;

  @SuppressWarnings("unchecked")
  public ListBuffer() {
    elements = (E[]) new Object[0];
    length   = 0;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public E get(int index) {
    return elements[index];
  }
}
