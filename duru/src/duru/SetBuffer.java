package duru;

final class SetBuffer<M> implements SetLike<M> {
  private final ListBuffer<M>       members;
  private final ListBuffer<Integer> buckets;

  public SetBuffer() {
    this.members = new ListBuffer<>();
    this.buckets = new ListBuffer<>();
  }

  @Override
  public int length() {
    return members.length();
  }

  @Override
  public boolean contains(M member) {
    var expectedBucket = member.hashCode();
    for (var i = 0; i < buckets.length(); i++) {
      var bucket = (expectedBucket + i) % buckets.length();
      var index  = buckets.get(bucket);
      if (index == -1)
        break;
      var mapped = members.get(index);
      if (mapped.equals(member))
        return true;
    }
    return false;
  }
}
