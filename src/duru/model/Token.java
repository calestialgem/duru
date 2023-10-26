package duru.model;

public sealed interface Token {
    record OpeningBrace(int start) implements Token {
        @Override
        public int length() { return "{".length(); }

        @Override
        public String explain(String contents) { return "punctuation `{`"; }
    }

    record ClosingBrace(int start) implements Token {
        @Override
        public int length() { return "}".length(); }

        @Override
        public String explain(String contents) { return "punctuation `}`"; }
    }

    record OpeningParenthesis(int start) implements Token {
        @Override
        public int length() { return "(".length(); }

        @Override
        public String explain(String contents) { return "punctuation `(`"; }
    }

    record ClosingParenthesis(int start) implements Token {
        @Override
        public int length() { return ")".length(); }

        @Override
        public String explain(String contents) { return "punctuation `)`"; }
    }

    record Semicolon(int start) implements Token {
        @Override
        public int length() { return ";".length(); }

        @Override
        public String explain(String contents) { return "punctuation `;`"; }
    }

    record Dot(int start) implements Token {
        @Override
        public int length() { return ".".length(); }

        @Override
        public String explain(String contents) { return "punctuation `.`"; }
    }

    record Entrypoint(int start) implements Token {
        @Override
        public int length() { return "entrypoint".length(); }

        @Override
        public String explain(String contents) {
            return "keyword `entrypoint`";
        }
    }

    record Identifier(int start, String text) implements Token {
        @Override
        public int length() { return text.length(); }

        @Override
        public String explain(String contents) {
            return "identifier `%s`".formatted(text);
        }

        @Override
        public String text(String contents) { return text; }
    }

    record NumberConstant(int start, int length, long value) implements Token {
        @Override
        public String explain(String contents) {
            return "number constant `%x`".formatted(value);
        }
    }

    int start();

    int length();

    String explain(String contents);

    default String text(String contents) {
        return contents.substring(start(), start() + length());
    }
}
