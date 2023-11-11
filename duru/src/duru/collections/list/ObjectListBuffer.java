package duru.collections.list;

import java.util.Arrays;

public final class ObjectListBuffer<E>
  implements
  ListBuffer<E>,
  ObjectListLike<E>
{
  private E[] elements;
  private int length;

  @SuppressWarnings("unchecked")
  public ObjectListBuffer() {
    elements = (E[]) new Object[0];
    length   = 0;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public E get(int index) {
    return elements[index];
  }

  @Override
  public void set(int index, E element) {
    elements[index] = element;
  }

  @Override
  public void add(E element) {
    reserve(1);
    elements[length] = element;
    length++;
  }

  @Override
  public void addAll(ListLike<E> list) {
    reserve(list.length());
    switch (list) {
      case ObjectList l ->
        System.arraycopy(l.elements(), 0, elements, length, l.length());
      case ObjectListBuffer b ->
        System.arraycopy(b.elements, 0, elements, length, b.length());
      default -> {
        for (var e : list) {
          add(e);
        }
      }
    }
  }

  @Override
  public void reserve(int amount) {
    var growth = elements.length - length - amount;
    if (growth <= 0)
      return;
    if (growth < elements.length)
      growth = elements.length;
    elements = Arrays.copyOf(elements, elements.length + growth);
  }

  @Override
  public ObjectList<E> toList() {
    return new ObjectList<>(Arrays.copyOf(elements, length));
  }
}
