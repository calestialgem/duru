package duru;

public record Set<Member>(List<Member> members, List<Integer> buckets)
  implements Collection<Member>
{
  @Override
  public int length() {
    return members.length();
  }

  @Override
  public Member get(int index) {
    return members.get(index);
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
}
