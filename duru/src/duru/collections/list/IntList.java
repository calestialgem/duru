package duru.collections.list;

public record IntList(int[] elements) implements List<Integer>, IntListLike {
  @Override
  public int length() {
    return elements.length;
  }

  @Override
  public int getAsInt(int index) {
    return elements[index];
  }
}
