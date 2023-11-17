package duru;

public final class SetBuffer<Member> {
  public static <Member> SetBuffer<Member> create() {
    return new SetBuffer<>(ListBuffer.create(), ListBuffer.create());
  }

  private final ListBuffer<Member>  members;
  private final ListBuffer<Integer> buckets;

  private SetBuffer(ListBuffer<Member> members, ListBuffer<Integer> buckets) {
    this.members = members;
    this.buckets = buckets;
  }

  public Set<Member> toSet() {
    rehash();
    return new Set<>(members.toList(), buckets.toList());
  }

  public boolean contains(Member member) {
    var hash = member.hashCode();
    for (var i = 0; i < buckets.length(); i++) {
      var bucket = (hash + i) % buckets.length();
      var index  = buckets.get(bucket);
      if (index == -1)
        return false;
      if (member.equals(members.get(index)))
        return true;
    }
    return false;
  }

  public boolean add(Member member) {
    var hash = member.hashCode();
    for (var i = 0; i < buckets.length(); i++) {
      var bucket = (hash + i) % buckets.length();
      var index  = buckets.get(bucket);
      if (index == -1) {
        buckets.set(bucket, members.length());
        members.add(member);
        return true;
      }
      if (member.equals(members.get(index))) {
        return false;
      }
    }
    members.add(member);
    rehash();
    return true;
  }

  public boolean remove(Member member) {
    var hash = member.hashCode();
    for (var i = 0; i < buckets.length(); i++) {
      var bucket = (hash + i) % buckets.length();
      var index  = buckets.get(bucket);
      if (index == -1)
        return false;
      if (member.equals(members.get(index))) {
        members.remove(index);
        rehash();
        return true;
      }
    }
    return false;
  }

  private void rehash() {
    buckets.clear();
    buckets.fill(-1, members.length() * 2);
    for (var m = 0; m < members.length(); m++) {
      var member = members.get(m);
      var hash   = member.hashCode();
      for (var i = 0; i < buckets.length(); i++) {
        var bucket = (hash + i) % buckets.length();
        var index  = buckets.get(bucket);
        if (index == -1) {
          buckets.set(bucket, m);
          break;
        }
      }
    }
  }
}
