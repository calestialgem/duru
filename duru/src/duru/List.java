package duru;

import java.util.Arrays;

public final class List<Element> {
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
}
