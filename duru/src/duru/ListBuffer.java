package duru;

import java.util.Arrays;

public final class ListBuffer<Element> {
  @SuppressWarnings("unchecked")
  public static <Element> ListBuffer<Element> create() {
    return new ListBuffer<>((Element[]) new Object[0], 0);
  }

  private Element[] elements;
  private int       length;

  private ListBuffer(Element[] elements, int length) {
    this.elements = elements;
    this.length   = length;
  }

  public Element get() {
    return elements[length - 1];
  }

  public void add(Element element) {
    reserve(1);
    elements[length] = element;
    length++;
  }

  public Element remove() {
    length--;
    return elements[length];
  }

  private void reserve(int amount) {
    var space  = elements.length - length;
    var growth = amount - space;
    if (growth <= 0) {
      return;
    }
    if (growth < elements.length) {
      growth = elements.length;
    }
    elements = Arrays.copyOf(elements, elements.length + growth);
  }
}
