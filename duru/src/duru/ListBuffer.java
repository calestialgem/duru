package duru;

import java.util.Arrays;
import java.util.function.Function;

public final class ListBuffer<Element> implements ListLike<Element> {
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

  @Override
  public int length() {
    return length;
  }

  @Override
  public Element get(int index) {
    return elements[index];
  }

  @Override
  @SuppressWarnings("unchecked")
  public <U> ListBuffer<U> transform(
    Function<Element, ? extends U> transformer)
  {
    var elements = (U[]) new Object[length];
    for (var i = 0; i < length; i++) {
      elements[i] = transformer.apply(get(i));
    }
    return new ListBuffer<>(elements, length);
  }

  public List<Element> toList() {
    return List.of(0, length, elements);
  }

  public ListBuffer<Element> copy() {
    return transform(Function.identity());
  }

  public void set(int index, Element element) {
    elements[index] = element;
  }

  public void add(Element element) {
    reserve(1);
    elements[length] = element;
    length++;
  }

  public void addAll(List<Element> list) {
    reserve(list.length());
    for (var i = 0; i < list.length(); i++)
      elements[length + i] = list.get(i);
    length += list.length();
  }

  public void fill(Element element, int amount) {
    reserve(amount);
    Arrays.fill(elements, length, length + amount, element);
    length += amount;
  }

  public Element removeLast() {
    return remove(length - 1);
  }

  public Element remove(int index) {
    var element = elements[index];
    length--;
    System.arraycopy(elements, index + 1, elements, index, length - index);
    return element;
  }

  public void removeDownTo(int newLength) {
    length = newLength;
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
