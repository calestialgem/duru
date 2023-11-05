package duru;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Runs a process when necessary only once for each input and makes sure the
 * process is acyclic between inputs, which means the process does not run for a
 * input when it is already running for that input. */
public final class AcyclicCache<I, O, E extends Exception> {
  /** Function that the inputs go through. */
  private final Process<I, O, E> process;

  /** Outputs that were already processed. Used for deduplicating work. */
  private final Map<I, O> alreadyProcessed;

  /** Inputs that are currently getting processed. Used for catching cycles. */
  private final Set<I> currentlyProcessed;

  /** Constructs. */
  public AcyclicCache(Process<I, O, E> process) {
    this.process       = process;
    alreadyProcessed   = new HashMap<>();
    currentlyProcessed = new HashSet<>();
  }

  /** Returns the output for the given input. Throws when the output cyclicly
   * depends on itself. */
  public O get(I input) throws E, CyclicProcessException {
    if (alreadyProcessed.containsKey(input))
      return (alreadyProcessed.get(input));
    if (currentlyProcessed.contains(input)) {
      throw new CyclicProcessException();
    }
    currentlyProcessed.add(input);
    var output = process.process(input);
    alreadyProcessed.put(input, output);
    currentlyProcessed.remove(input);
    return (output);
  }

  /** Returns all the outputs up to this point. */
  public Map<I, O> getAll() {
    return Map.copyOf(alreadyProcessed);
  }
}
