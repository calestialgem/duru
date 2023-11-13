package duru;

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
    return List.copyOf(elements, length);
  }
}
