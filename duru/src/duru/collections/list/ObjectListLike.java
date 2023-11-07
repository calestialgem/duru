package duru.collections.list;

public interface ObjectListLike<E> extends ListLike<E> {
  @Override
  default ObjectListIterator<E> iterator() {
    return new ObjectListIterator<>(this);
  }
}
