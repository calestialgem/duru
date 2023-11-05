package duru.semantics;

import duru.Name;
import duru.diagnostic.Subject;

/** Function that finds a global with the given name. */
@FunctionalInterface
interface GlobalFinder {
  /** Returns the global with the given name if it exists. Otherwise reports a
   * diagnostic to the given subject. */
  Semantic.Definition find(Subject subject, Name name);
}
