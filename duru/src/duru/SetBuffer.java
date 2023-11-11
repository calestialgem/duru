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

  public boolean add(M member) {
    {
      var expectedBucket = member.hashCode();
      for (var i = 0; i < buckets.length(); i++) {
        var bucket = (expectedBucket + i) % buckets.length();
        var index  = buckets.get(bucket);
        if (index == -1) {
          buckets.set(bucket, members.length());
          members.add(member);
          return true;
        }
        var mapped = members.get(index);
        if (mapped.equals(member))
          return false;
      }
    }
    members.add(member);
    buckets.clear();
    buckets.add(-1, members.length() * 2);
    for (var m = 0; m < members.length(); m++) {
      var expectedBucket = members.get(m).hashCode();
      for (var i = 0; i < buckets.length(); i++) {
        var bucket = (expectedBucket + i) % buckets.length();
        var index  = buckets.get(bucket);
        if (index == -1) {
          buckets.set(bucket, m);
          break;
        }
      }
    }
    return true;
  }

  public boolean remove(M member) {}
}
