package duru;

public final class Set<Member> implements Collection<Member> {
  private final List<Member>  members;
  private final List<Integer> buckets;

  private Set(List<Member> members, List<Integer> buckets) {
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
}
