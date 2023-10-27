package duru.linear;

import java.nio.file.Path;

/** Representation of a source file as a string. */
public record Linear(Path path, String contents) {}
