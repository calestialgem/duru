package duru.collections.set;

import java.util.Iterator;

import duru.collections.list.IntList;
import duru.collections.list.ObjectList;

public record ObjectSet<M>(ObjectList<M> members, IntList mapping)
  implements Set<M>, ObjectSetLike<M>
{
  @Override
  public int length() {
    return members.length();
  }

  @Override
  public boolean contains(M member) {
    var start = member.hashCode() % length();
    for (var i = 0; i != length(); i++) {
      var search = start + i;
      var index  = mapping.getAsInt(search % length());
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
}
