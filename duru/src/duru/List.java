package duru;

public final class List<Element>
  implements
  ListLike<Element>,
  Collection<Element>
{
  @SuppressWarnings("unchecked")
  public static <Element> List<Element> of(
    int offset,
    int length,
    Element[] elements)
  {
    var copy = (Element[]) new Object[length];
    System.arraycopy(elements, offset, copy, 0, length);
    return new List<>(copy);
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
    var result = 1;
    for (var i = 0; i < length(); i++) {
      result *= 31;
      result += get(i).hashCode();
    }
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other)
      return true;
    if (!(other instanceof List otherList))
      return false;
    if (length() != otherList.length())
      return false;
    for (var i = 0; i < length(); i++) {
      if (get(i).equals(otherList.get(i)))
        return false;
    }
    return true;
  }

  @Override
  public String toString() {
    var string = new StringBuilder();
    string.append('[');
    if (!isEmpty()) {
      string.append(getFirst());
      for (var i = 1; i < length(); i++) {
        string.append(',');
        string.append(' ');
        string.append(get(i));
      }
    }
    string.append(']');
    return string.toString();
  }
}
