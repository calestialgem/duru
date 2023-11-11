package duru;

import java.util.Arrays;

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

  public void add(E element) {
    reserve(1);
    elements[length] = element;
    length++;
  }

  public void set(int index, E element) {
    elements[index] = element;
  }

  private void reserve(int amount) {
    int space  = elements.length - length;
    int growth = amount - space;
    if (growth <= 0)
      return;
    if (growth < elements.length)
      growth = elements.length;
    elements = Arrays.copyOf(elements, elements.length + growth);
  }
}
