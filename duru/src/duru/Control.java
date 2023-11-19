package duru;

public enum Control {
  FLOWS, SINKS, RETURNS, BRANCHING;

  public static Control combineSequents(Control first, Control second) {
    if (first != Control.FLOWS)
      return first;
    return second;
  }

  public static Control combineBranches(Control... branches) {
    if (branches.length == 0) {
      return Control.BRANCHING;
    }
    for (var branch : branches) {
      if (branch == Control.FLOWS)
        return Control.FLOWS;
    }
    for (var branch : branches) {
      if (branch != branches[0])
        return Control.BRANCHING;
    }
    return branches[0];
  }
}
