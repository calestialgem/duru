package duru;

record Set<M>(List<M> members, int[] mapping) implements Iterable<M> {
  public int length() {
    return members.length();
  }

  public boolean contains(M member) {
    var start = member.hashCode() % length();
    for (var i = 0; i != length(); i++) {
      var search = start + i;
      var index  = mapping[search % length()];
      var mapped = members.get(index);
      if (mapped.equals(member))
        return true;
    }
    return false;
  }

  @Override
  public ListIterator<M> iterator() {
    return members.iterator();
  }
}
