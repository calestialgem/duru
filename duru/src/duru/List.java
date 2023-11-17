package duru;

import java.util.Arrays;

public record List<Element>(Element[] elements) implements Collection<Element> {
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
