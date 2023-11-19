package duru;

public sealed interface Token {
  record OpeningBrace(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `{`";
    }
  }

  record ClosingBrace(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `}`";
    }
  }

  record OpeningParenthesis(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `(`";
    }
  }

  record ClosingParenthesis(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `)`";
    }
  }

  record Semicolon(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `;`";
    }
  }

  record Dot(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `.`";
    }
  }

  record Comma(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `,`";
    }
  }

  record Equal(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `=`";
    }
  }

  record Star(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `*`";
    }
  }

  record Left(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `<`";
    }
  }

  record Public(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `public`";
    }
  }

  record Proc(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `proc`";
    }
  }

  record Struct(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `struct`";
    }
  }

  record Var(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `var`";
    }
  }

  record If(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `if`";
    }
  }

  record Else(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `else`";
    }
  }

  record Return(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `return`";
    }
  }

  record Identifier(Location location, String text) implements Token {
    @Override
    public String toString() {
      return "identifier `%s`".formatted(text);
    }
  }

  record NaturalConstant(Location location, long value) implements Token {
    @Override
    public String toString() {
      return "number `%s`".formatted(Long.toUnsignedString(value));
    }
  }

  record StringConstant(Location location, String value) implements Token {
    @Override
    public String toString() {
      return "string `\"%s\"`".formatted(Text.quote(value));
    }
  }

  Location location();
}
