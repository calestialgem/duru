package duru.collections.list;

public interface ListLike<E> extends Iterable<E> {
  int length();
  E get(int index);
}
