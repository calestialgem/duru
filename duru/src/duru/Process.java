package duru;

/** Function that calculates an output from an input and maybe throws an
 * exception. */
@FunctionalInterface
public interface Process<I, O, E extends Exception> {
  O process(I input) throws E;
}
