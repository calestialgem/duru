package duru;

import java.util.Iterator;

import duru.collections.list.IntList;
import duru.collections.list.List;

record Set<M>(List<M> members, IntList mapping) implements Iterable<M> {
  public int length() {
    return members.length();
  }

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
