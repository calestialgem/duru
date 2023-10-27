package duru.configuration;

import java.util.List;

public record PackageName(List<String> scopes) {
    public PackageName { scopes = List.copyOf(scopes); }
}
