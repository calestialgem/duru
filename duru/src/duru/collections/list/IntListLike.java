package duru.collections.list;

public sealed interface IntListLike extends ListLike<Integer>
  permits
  IntList,
  IntListBuffer
{
  @Override
  default IntListIterator iterator() {
    return new IntListIterator(this);
  }

  @Override
  default Integer get(int index) {
    return getAsInt(index);
  }

  int getAsInt(int index);
}
