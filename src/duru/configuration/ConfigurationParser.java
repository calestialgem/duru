package duru.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ConfigurationParser {
    private final ConfigurationLexer lexer;

    private String contents;

    private List<ConfigurationToken> tokens;

    private String name;

    private final List<ConfigurationPackage> executables;

    private int index;

    public ConfigurationParser() {
        lexer       = new ConfigurationLexer();
        executables = new ArrayList<>();
    }

    public Configuration parse(String contents) throws ConfigurationException {
        this.contents = contents;
        this.tokens   = lexer.lex(contents);
        executables.clear();
        index = 0;
        parse();
        return new Configuration(name, executables);
    }

    private void parse() throws ConfigurationException {
        expectToken(ConfigurationToken.Project.class, "project definition");
        name =
            expectToken(
                ConfigurationToken.Identifier.class,
                "name of the project definition").text(contents);
        expectToken(
            ConfigurationToken.OpeningBrace.class,
            "opening `{` of the project definition");
        while (true) {
            if (parseToken(ConfigurationToken.Executable.class).isPresent()) {
                executables
                    .add(
                        expect(
                            this::parsePackageName,
                            "package of the executable directive"));
                expectToken(
                    ConfigurationToken.Semicolon.class,
                    "terminating `;` of the executable directive");
            }
            expectToken(
                ConfigurationToken.ClosingBrace.class,
                "closing `}` of the project definition");
            break;
        }
    }

    private Optional<ConfigurationPackage> parsePackageName()
        throws ConfigurationException
    {
        var name = parseToken(ConfigurationToken.Identifier.class);
        if (name.isEmpty())
            return Optional.empty();
        var scopes = new ArrayList<String>();
        scopes.add(name.get().text(contents));
        while (parseToken(ConfigurationToken.Dot.class).isPresent()) {
            scopes
                .add(
                    expectToken(
                        ConfigurationToken.Identifier.class,
                        "name of the package").text(contents));
        }
        return Optional.of(new ConfigurationPackage(scopes));
    }

    private <T extends ConfigurationToken> T expectToken(
        Class<T> klass,
        String explanation)
        throws ConfigurationException
    {
        return expect(() -> parseToken(klass), explanation);
    }

    @SuppressWarnings("unchecked")
    private <T extends ConfigurationToken> Optional<T> parseToken(
        Class<T> klass)
    {
        if (index == tokens.size()) { return Optional.empty(); }
        var token = tokens.get(index);
        if (!klass.isInstance(token)) { return Optional.empty(); }
        index++;
        return Optional.of((T) token);
    }

    private <T> T expect(
        ConfigurationParseFunction<T> parseFunction,
        String explanation)
        throws ConfigurationException
    {
        var result = parseFunction.parse();
        if (result.isPresent())
            return result.get();
        if (index == tokens.size()) {
            if (index != 0) {
                var previous = tokens.getLast();
                throw new ConfigurationException(
                    contents,
                    previous.start(),
                    previous.length(),
                    "Expected %s at the end of the file, after %s!",
                    explanation,
                    previous.explain(contents));
            }
            throw new ConfigurationException(
                contents,
                0,
                0,
                "Expected %s instead of an empty file!",
                explanation);
        }
        var current = tokens.get(index);
        throw new ConfigurationException(
            contents,
            current.start(),
            current.length(),
            "Expected %s instead of %s!",
            explanation,
            current.explain(contents));
    }
}
