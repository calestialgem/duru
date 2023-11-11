package duru.collections.set;

public interface SetLike<M> extends Iterable<M> {
  int length();
  boolean contains(M member);
}
