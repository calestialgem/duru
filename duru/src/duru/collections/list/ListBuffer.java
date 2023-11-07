package duru.collections.list;

public interface ListBuffer<E> extends ListLike<E> {
  void push(E element);
  E pop();
  void reserve(int amount);
  List<E> toList();
}
