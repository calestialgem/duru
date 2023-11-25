package duru;

import java.util.Arrays;

public enum Control {
  FLOWS, SINKS, RETURNS, BREAKS, CONTINUES, BRANCHING;

  public static Control sequence(List<Control> sequence) {
    var control = FLOWS;
    for (var sequent : sequence)
      control = control.sequent(sequent);
    return control;
  }

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

  public Control unloop(boolean unconditional) {
    var result = this;
    if (this == BREAKS || this == CONTINUES)
      result = FLOWS;
    if (unconditional && result == FLOWS)
      result = SINKS;
    return result;
  }
}
