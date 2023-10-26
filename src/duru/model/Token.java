package duru.model;

public sealed interface Token {
    record OpeningBrace(Portion portion) implements Token {
        @Override
        public String explain() { return "punctuation `{`"; }
    }

    record ClosingBrace(Portion portion) implements Token {
        @Override
        public String explain() { return "punctuation `}`"; }
    }

    record OpeningParenthesis(Portion portion) implements Token {
        @Override
        public String explain() { return "punctuation `(`"; }
    }

    record ClosingParenthesis(Portion portion) implements Token {
        @Override
        public String explain() { return "punctuation `)`"; }
    }

    record Semicolon(Portion portion) implements Token {
        @Override
        public String explain() { return "punctuation `;`"; }
    }

    record Dot(Portion portion) implements Token {
        @Override
        public String explain() { return "punctuation `.`"; }
    }

    record Entrypoint(Portion portion) implements Token {
        @Override
        public String explain() { return "keyword `entrypoint`"; }
    }

    record Identifier(Portion portion, String text) implements Token {
        @Override
        public String explain() { return "identifier `%s`".formatted(text); }
    }

    record NumberConstant(Portion portion, long value) implements Token {
        @Override
        public String explain() {
            return "number constant `%x`".formatted(value);
        }
    }

    Portion portion();

    String explain();
}
