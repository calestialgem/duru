package duru;

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
