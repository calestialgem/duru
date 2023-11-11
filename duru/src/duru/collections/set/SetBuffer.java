package duru.collections.set;

public interface SetBuffer<M> extends SetLike<M> {
  boolean add(M member);
  Set<M> toSet();
}
