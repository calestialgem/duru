package duru.configuration;

import java.util.Optional;

@FunctionalInterface
interface ParseFunction<T> {
  Optional<T> parse() throws ConfigurationParseException;
}
