package duru;

public record Set<Member>(List<Member> members, List<Integer> buckets)
  implements SetLike<Member>, Collection<Member>
{
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
  public String toString() {
    var string = new StringBuilder();
    string.append('{');
    if (!isEmpty()) {
      string.append(getFirst());
      for (var i = 1; i < length(); i++) {
        string.append(',');
        string.append(' ');
        string.append(get(i));
      }
    }
    string.append('}');
    return string.toString();
  }
}
