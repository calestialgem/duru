package duru.configuration;

import java.util.List;

public record Configuration(String name, List<PackageName> executables) {
    public Configuration { executables = List.copyOf(executables); }

    public static Configuration parse(String contents)
        throws ConfigurationParseException
    {
        var lexer         = Lexer.create();
        var tokens        = lexer.lex(contents);
        var parser        = Parser.create();
        var configuration = parser.parse(contents, tokens);
        return configuration;
    }
}
