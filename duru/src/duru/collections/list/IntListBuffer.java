package duru.collections.list;

import java.util.Arrays;

public final class IntListBuffer implements ListBuffer<Integer>, IntListLike {
  private int[] elements;
  private int   length;

  public IntListBuffer() {
    elements = new int[0];
    length   = 0;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int getAsInt(int index) {
    return elements[index];
  }

  @Override
  public void set(int index, Integer element) {
    setAsInt(index, element);
  }

  public void setAsInt(int index, int element) {
    elements[index] = element;
  }

  @Override
  public void add(Integer element) {
    addAsInt(element);
  }

  public void addAsInt(int element) {
    reserve(1);
    elements[length] = element;
    length++;
  }

  @Override
  public void addAll(ListLike<Integer> list) {
    switch (list) {
      case IntListLike i -> addAllAsInt(i);
      default -> {
        reserve(list.length());
        for (var e : list) {
          add(e);
        }
      }
    }
  }

  public void addAllAsInt(IntListLike list) {
    reserve(list.length());
    switch (list) {
      case IntList l ->
        System.arraycopy(l.elements(), 0, elements, length, l.length());
      case IntListBuffer b ->
        System.arraycopy(b.elements, 0, elements, length, b.length);
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
  public IntList toList() {
    return new IntList(Arrays.copyOf(elements, length));
  }
}
