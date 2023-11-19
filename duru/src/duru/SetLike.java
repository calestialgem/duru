package duru;

import java.util.function.Function;

public interface SetLike<Member> extends CollectionLike<Member> {
  boolean contains(Member member);
  @Override
  <U> ListLike<U> transform(Function<Member, U> transformer);
}
