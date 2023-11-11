package duru;

final class SetBuffer<M> implements SetLike<M> {
  private final ListBuffer<M>       members;
  private final ListBuffer<Integer> buckets;

  public SetBuffer() {
    this.members = new ListBuffer<>();
    this.buckets = new ListBuffer<>();
  }

  @Override
  public int length() {}

  @Override
  public boolean contains(M member) {}
}
