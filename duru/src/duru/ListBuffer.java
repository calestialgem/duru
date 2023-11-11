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

  public void set(int index, E element) {
    elements[index] = element;
  }

  public void add(E element) {
    reserve(1);
    elements[length] = element;
    length++;
  }

  public void add(E element, int amount) {
    reserve(amount);
    Arrays.fill(elements, length, length + amount, element);
    length += amount;
  }

  public void remove(int index) {
    System.arraycopy(elements, index + 1, elements, index, length - index - 1);
  }

  public void clear() {
    length = 0;
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
