package duru;

import java.util.Arrays;

public final class List<Element> {
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
}
