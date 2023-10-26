package duru.configuration;

import java.util.List;

public record Configuration(
    String name,
    List<ConfigurationPackage> executables)
{
    public Configuration { executables = List.copyOf(executables); }
}
