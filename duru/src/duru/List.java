package duru;

import java.util.Arrays;

public final class List<Element> {
  public static <Element> List<Element> copyOf(Element[] elements, int length) {
    return new List<>(Arrays.copyOf(elements, length));
  }

  private final Element[] elements;

  private List(Element[] elements) {
    this.elements = elements;
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

  public Element first() {
    return elements[0];
  }

  public Element last() {
    return elements[elements.length - 1];
  }
}
