package duru.configuration;

import java.util.Optional;

@FunctionalInterface
interface ConfigurationParseFunction<T> {
    Optional<T> parse() throws ConfigurationException;
}
