package duru;

public sealed interface ConfigurationToken {
  record Semicolon(Location location) implements ConfigurationToken {
    @Override
    public String toString() {
      return "punctuation `;`";
    }
  }

  record ColonColon(Location location) implements ConfigurationToken {
    @Override
    public String toString() {
      return "punctuation `::`";
    }
  }

  record Executable(Location location) implements ConfigurationToken {
    @Override
    public String toString() {
      return "keyword `executable`";
    }
  }

  record Library(Location location) implements ConfigurationToken {
    @Override
    public String toString() {
      return "keyword `library`";
    }
  }

  record Identifier(Location location, String text)
    implements ConfigurationToken
  {
    @Override
    public String toString() {
      return "identifier `%s`".formatted(text);
    }
  }

  Location location();
}
