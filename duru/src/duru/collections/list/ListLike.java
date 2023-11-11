package duru.collections.list;

public sealed interface ListLike<E> extends Iterable<E>
  permits
  List,
  ListBuffer,
  ObjectListLike,
  IntListLike
{
  int length();
  E get(int index);
}
