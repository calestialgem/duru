package duru.collections.list;

public sealed interface ListBuffer<E> extends ListLike<E>
  permits
  ObjectListBuffer,
  IntListBuffer
{
  void set(int index, E element);
  void add(E element);
  void addAll(ListLike<E> list);
  void reserve(int amount);
  List<E> toList();
}
