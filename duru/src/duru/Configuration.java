package duru;

import java.util.List;

public sealed interface Configuration {
  record Project(String name, List<Package> executables)
    implements Configuration
  {}

  record Package(String name) implements Configuration {}

  String name = "project.duru";
}
