package duru.configuration;

import java.util.List;

public record ConfigurationPackage(List<String> scopes) {
    public ConfigurationPackage { scopes = List.copyOf(scopes); }
}
