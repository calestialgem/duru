package duru;

record Set<M>(List<M> members, List<Integer> buckets) implements SetLike<M> {
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
