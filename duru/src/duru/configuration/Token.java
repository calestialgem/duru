package duru.configuration;

sealed interface Token {
  record OpeningBrace(int start) implements Token {
    @Override
    public int end() {
      return start + "{".length();
    }

    @Override
    public String explain() {
      return "punctuation `{`";
    }
  }

  record ClosingBrace(int start) implements Token {
    @Override
    public int end() {
      return start + "}".length();
    }

    @Override
    public String explain() {
      return "punctuation `}`";
    }
  }

  record Semicolon(int start) implements Token {
    @Override
    public int end() {
      return start + ";".length();
    }

    @Override
    public String explain() {
      return "punctuation `;`";
    }
  }

  record Dot(int start) implements Token {
    @Override
    public int end() {
      return start + ".".length();
    }

    @Override
    public String explain() {
      return "punctuation `.`";
    }
  }

  record Project(int start) implements Token {
    @Override
    public int end() {
      return start + "project".length();
    }

    @Override
    public String explain() {
      return "keyword `project`";
    }
  }

  record Executable(int start) implements Token {
    @Override
    public int end() {
      return start + "executable".length();
    }

    @Override
    public String explain() {
      return "keyword `executable`";
    }
  }

  record Identifier(int start, String word) implements Token {
    @Override
    public int end() {
      return start + word.length();
    }

    @Override
    public String explain() {
      return "identifier `%s`".formatted(word);
    }
  }

  int start();

  int end();

  String explain();

  default String text(String contents) {
    return contents.substring(start(), end());
  }
}
