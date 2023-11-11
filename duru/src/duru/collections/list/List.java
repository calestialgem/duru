package duru.collections.list;

public sealed interface List<E> extends ListLike<E>
  permits
  ObjectList,
  IntList
{}
