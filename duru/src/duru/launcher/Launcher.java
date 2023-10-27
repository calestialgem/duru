package duru.launcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import duru.configuration.Configuration;
import duru.configuration.ConfigurationParseException;

final class Launcher {
    public static void main(String... arguments) {
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
            configuration = Configuration.parse(configurationText);
        }
        catch (ConfigurationParseException cause) {
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
