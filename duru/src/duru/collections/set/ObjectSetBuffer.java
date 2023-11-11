package duru.collections.set;

import java.util.Iterator;

import duru.collections.list.IntListBuffer;
import duru.collections.list.ObjectListBuffer;

public final class ObjectSetBuffer<M>
  implements
  SetBuffer<M>,
  ObjectSetLike<M>
{
  private final ObjectListBuffer<M> members;
  private final IntListBuffer       mapping;

  public ObjectSetBuffer() {
    members = new ObjectListBuffer<>();
    mapping = new IntListBuffer();
  }

  @Override
  public int length() {
    return members.length();
  }

  @Override
  public boolean contains(M member) {
    var start = member.hashCode() % length();
    for (var i = 0; i != length(); i++) {
      var search = (start + i) % length();
      var index  = mapping.getAsInt(search);
      if (index == -1)
        break;
      var mapped = members.get(index);
      if (mapped.equals(member))
        return true;
    }
    return false;
  }

  @Override
  public Iterator<M> iterator() {
    return members.iterator();
  }

  @Override
  public boolean add(M member) {
    var start = member.hashCode() % length();
    for (var i = 0; i != length(); i++) {
      var search = (start + i) % length();
      var index  = mapping.getAsInt(search);
      if (index == -1) {
        var newIndex = members.length();
        mapping.setAsInt(search, newIndex);
        members.add(member);
        return true;
      }
      var mapped = members.get(index);
      if (mapped.equals(member))
        return false;
    }
    var rehashed = new ObjectSetBuffer<M>();
    rehashed.members.reserve(members.length() * 2);
    rehashed.mapping.reserve(mapping.length() * 2);
    members.add(member);
    return true;
  }

  @Override
  public Set<M> toSet() {

    return new ObjectSet<M>(members.toList(), mapping.toList());
  }
}
