package duru.launcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import duru.configuration.Configuration;
import duru.configuration.ConfigurationException;
import duru.configuration.ConfigurationParser;
import duru.lexer.Lexer;
import duru.model.Token;

final class Launcher {
    public static void main(String... arguments) {
        if (arguments.length != 1)
            throw new RuntimeException("Provide a test code!");
        switch (arguments[0]) {
            case "c" -> configurationTest();
            case "l" -> lexerTest();
            default ->
                throw new RuntimeException(
                    "Unknown test code `%s`!".formatted(arguments[0]));
        }
    }

    private static void lexerTest() {
        var    sourcePath = Path.of("test", "returns2", "src", "main.duru");
        String sourceText;
        try {
            sourceText = Files.readString(sourcePath);
        }
        catch (IOException cause) {
            throw new RuntimeException(
                "%s: failure: Could not read the source file!"
                    .formatted(sourcePath.toAbsolutePath().normalize()),
                cause);
        }
        List<Token> tokens;
        try {
            tokens = new Lexer().lex(sourceText);
        }
        catch (RuntimeException cause) {
            throw new RuntimeException(
                "%s: error: Could not lex the source file!"
                    .formatted(sourcePath.toAbsolutePath().normalize(), cause),
                cause);
        }
        var artifactDirectory = Path.of("test", "returns2", "art");
        if (!Files.exists(artifactDirectory)) {
            try {
                Files.createDirectory(artifactDirectory);
            }
            catch (IOException cause) {
                throw new RuntimeException(
                    "%s: failure: Could not create the artifact directory!"
                        .formatted(
                            artifactDirectory.toAbsolutePath().normalize()),
                    cause);
            }
        }
        var artifactPath = artifactDirectory.resolve("tokens.duru");
        try {
            Files.writeString(artifactPath, tokens.toString());
        }
        catch (IOException cause) {
            throw new RuntimeException(
                "%s: failure: Could not write the tokens artifact!"
                    .formatted(artifactPath.toAbsolutePath().normalize()),
                cause);
        }
    }

    private static void configurationTest() {
        var    configurationPath = Path.of("test", "returns2", "project.duru");
        String configurationText;
        try {
            configurationText = Files.readString(configurationPath);
        }
        catch (IOException cause) {
            throw new RuntimeException(
                "%s: failure: Could not read the configuration file!"
                    .formatted(configurationPath.toAbsolutePath().normalize()),
                cause);
        }
        Configuration configuration;
        try {
            configuration = new ConfigurationParser().parse(configurationText);
        }
        catch (ConfigurationException cause) {
            throw new RuntimeException(
                "%s:%s: error: Could not parse the configuration file!"
                    .formatted(
                        configurationPath.toAbsolutePath().normalize(),
                        cause.location()),
                cause);
        }
        var artifactDirectory = Path.of("test", "returns2", "art");
        if (!Files.exists(artifactDirectory)) {
            try {
                Files.createDirectory(artifactDirectory);
            }
            catch (IOException cause) {
                throw new RuntimeException(
                    "%s: failure: Could not create the artifact directory!"
                        .formatted(
                            artifactDirectory.toAbsolutePath().normalize()),
                    cause);
            }
        }
        var artifactPath = artifactDirectory.resolve("configuration.duru");
        try {
            Files.writeString(artifactPath, configuration.toString());
        }
        catch (IOException cause) {
            throw new RuntimeException(
                "%s: failure: Could not write the configuration artifact!"
                    .formatted(artifactPath.toAbsolutePath().normalize()),
                cause);
        }
    }
}
