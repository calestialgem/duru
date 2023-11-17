package duru;

public interface SetLike<Member> extends CollectionLike<Member> {
  boolean contains(Member member);
}
