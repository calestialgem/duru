package duru.collections.list;

public interface IntListLike extends ListLike<Integer> {
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
