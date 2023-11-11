package duru.collections.list;

public sealed interface ObjectListLike<E> extends ListLike<E>
  permits
  ObjectList,
  ObjectListBuffer
{
  @Override
  default ObjectListIterator<E> iterator() {
    return new ObjectListIterator<>(this);
  }
}
