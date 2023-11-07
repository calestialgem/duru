package duru;

import java.util.Arrays;

final class ListBuffer<E> {
  private E[] elements;
  private int length;

  @SuppressWarnings("unchecked")
  public ListBuffer() {
    elements = (E[]) new Object[0];
    length   = 0;
  }

  public void push(E element) {
    reserve(1);
    elements[length] = element;
    length++;
  }

  public void reserve(int amount) {
    var space  = elements.length - length;
    var growth = amount - space;
    if (growth <= 0)
      return;
    if (growth < elements.length)
      growth = elements.length;
    elements = Arrays.copyOf(elements, elements.length + growth);
  }

  public List<E> toList() {
    return new List<>(Arrays.copyOf(elements, length));
  }
}
