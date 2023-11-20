package duru;

import java.util.function.Function;

public final class SetBuffer<Member> implements SetLike<Member> {
  public static <Member> SetBuffer<Member> create() {
    return new SetBuffer<>(ListBuffer.create(), ListBuffer.create());
  }

  private final ListBuffer<Member>  members;
  private final ListBuffer<Integer> buckets;

  private SetBuffer(ListBuffer<Member> members, ListBuffer<Integer> buckets) {
    this.members = members;
    this.buckets = buckets;
  }

  @Override
  public int length() {
    return members.length();
  }

  @Override
  public Member get(int index) {
    return members.get(index);
  }

  @Override
  public boolean contains(Member member) {
    var hash = member.hashCode();
    for (var i = 0; i < buckets.length(); i++) {
      var bucket = Math.floorMod(hash + i, buckets.length());
      var index  = buckets.get(bucket);
      if (index == -1) {
        return false;
      }
      if (member.equals(members.get(index))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public <U> ListBuffer<U> transform(
    Function<Member, ? extends U> transformer)
  {
    return members.transform(transformer);
  }

  public Set<Member> toSet() {
    rehash();
    return new Set<>(members.toList(), buckets.toList());
  }

  public SetBuffer<Member> copy() {
    return new SetBuffer<>(members.copy(), buckets.copy());
  }

  public boolean add(Member member) {
    var hash = member.hashCode();
    for (var i = 0; i < buckets.length(); i++) {
      var bucket = Math.floorMod(hash + i, buckets.length());
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
      var bucket = Math.floorMod(hash + i, buckets.length());
      var index  = buckets.get(bucket);
      if (index == -1) {
        return false;
      }
      if (member.equals(members.get(index))) {
        members.remove(index);
        rehash();
        return true;
      }
    }
    return false;
  }

  public void clear() {
    members.clear();
    buckets.clear();
  }

  private void rehash() {
    buckets.clear();
    buckets.fill(-1, members.length() * 2);
    for (var m = 0; m < members.length(); m++) {
      var member = members.get(m);
      var hash   = member.hashCode();
      for (var i = 0; i < buckets.length(); i++) {
        var bucket = Math.floorMod(hash + i, buckets.length());
        var index  = buckets.get(bucket);
        if (index == -1) {
          buckets.set(bucket, m);
          break;
        }
      }
    }
  }
}
