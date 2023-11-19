package duru;

import java.util.Arrays;

public enum Control {
  FLOWS, SINKS, RETURNS, BRANCHING;

  public Control sequent(Control second) {
    if (this != FLOWS) {
      return this;
    }
    return second;
  }

  public Control branch(Control... branches) {
    if (this == FLOWS || Arrays.stream(branches).anyMatch(FLOWS::equals)) {
      return FLOWS;
    }
    if (Arrays.stream(branches).allMatch(this::equals)) {
      return this;
    }
    return BRANCHING;
  }
}
