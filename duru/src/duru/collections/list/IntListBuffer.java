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
  public void push(Integer element) {
    pushAsInt(element);
  }

  public void pushAsInt(int element) {
    reserve(1);
    elements[length] = element;
    length++;
  }

  @Override
  public Integer pop() {
    return popAsInt();
  }

  public int popAsInt() {
    return elements[--length];
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
