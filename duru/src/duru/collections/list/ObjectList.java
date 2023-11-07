package duru.collections.list;

public record ObjectList<E>(E[] elements)
  implements List<E>, ObjectListLike<E>
{
  @Override
  public int length() {
    return elements.length;
  }

  @Override
  public E get(int index) {
    return elements[index];
  }
}
