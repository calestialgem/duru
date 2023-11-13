package duru;

public sealed interface Configuration {
  record Project(
    int begin,
    int end,
    Identifier name,
    List<Executable> executables) implements Configuration
  {}

  record Executable(int begin, int end, Namespace namespace)
    implements Configuration
  {}

  record Namespace(List<Identifier> parts) implements Configuration {
    @Override
    public int begin() {
      return parts.first().begin();
    }

    @Override
    public int end() {
      return parts.last().end();
    }
  }

  record Identifier(int begin, String text) implements Configuration {
    @Override
    public int end() {
      return begin + text.length();
    }
  }

  String name = "project.duru";

  int begin();
  int end();
}
