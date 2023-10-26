package duru.configuration;

sealed interface ConfigurationToken {
    record OpeningBrace(int start) implements ConfigurationToken {
        @Override
        public int length() { return "{".length(); }

        @Override
        public String explain(String contents) { return "punctuation `{`"; }
    }

    record ClosingBrace(int start) implements ConfigurationToken {
        @Override
        public int length() { return "}".length(); }

        @Override
        public String explain(String contents) { return "punctuation `}`"; }
    }

    record Semicolon(int start) implements ConfigurationToken {
        @Override
        public int length() { return ";".length(); }

        @Override
        public String explain(String contents) { return "punctuation `;`"; }
    }

    record Dot(int start) implements ConfigurationToken {
        @Override
        public int length() { return ".".length(); }

        @Override
        public String explain(String contents) { return "punctuation `.`"; }
    }

    record Project(int start) implements ConfigurationToken {
        @Override
        public int length() { return "project".length(); }

        @Override
        public String explain(String contents) { return "keyword `project`"; }
    }

    record Executable(int start) implements ConfigurationToken {
        @Override
        public int length() { return "executable".length(); }

        @Override
        public String explain(String contents) {
            return "keyword `executable`";
        }
    }

    record Identifier(int start, int length) implements ConfigurationToken {
        @Override
        public String explain(String contents) {
            return "identifier `%s`".formatted(text(contents));
        }
    }

    int start();

    int length();

    String explain(String contents);

    default String text(String contents) {
        return contents.substring(start(), start() + length());
    }
}
