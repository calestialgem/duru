package duru;

import java.util.Arrays;

public final class List<Element> implements Collection<Element> {
  @SafeVarargs
  public static <Element> List<Element> of(Element... elements) {
    return of(elements.length, elements);
  }

  @SafeVarargs
  public static <Element> List<Element> of(int length, Element... elements) {
    return new List<>(Arrays.copyOf(elements, length));
  }

  private final Element[] elements;

  private List(Element[] elements) {
    this.elements = elements;
  }

  @Override
  public int length() {
    return elements.length;
  }

  @Override
  public Element get(int index) {
    return elements[index];
  }

  @Override
  public int hashCode() {
    return Arrays.deepHashCode(elements);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other)
      return true;
    if (!(other instanceof List otherList))
      return false;
    return Arrays.deepEquals(elements, otherList.elements);
  }

  @Override
  public String toString() {
    return Arrays.toString(elements);
  }
}
