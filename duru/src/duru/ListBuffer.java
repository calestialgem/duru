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

  public List<Element> toList() {
    return new List<>(Arrays.copyOf(elements, length));
  }

  public int length() {
    return length;
  }

  public Element get() {
    return get(length - 1);
  }

  public Element get(int index) {
    return elements[index];
  }

  public void set(int index, Element element) {
    elements[index] = element;
  }

  public void add(Element element) {
    reserve(1);
    elements[length] = element;
    length++;
  }

  public void fill(Element element, int amount) {
    reserve(amount);
    Arrays.fill(elements, length, length + amount, element);
    length += amount;
  }

  public Element remove() {
    return remove(length - 1);
  }

  public Element remove(int index) {
    var element = elements[index];
    length--;
    System.arraycopy(elements, index + 1, elements, index, length - index);
    return element;
  }

  public void clear() {
    length = 0;
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
