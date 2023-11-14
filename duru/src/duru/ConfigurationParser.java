package duru;

public final class ConfigurationParser {
  public static Syntactics parse(Lectics lectics) {
    var parser = new ConfigurationParser(lectics);
    return parser.parse();
  }

  private final Lectics   lectics;
  private ListBuffer<Tag> nodes;
  private int             index;

  private ConfigurationParser(Lectics lectics) {
    this.lectics = lectics;
  }

  private Syntactics parse() {
    nodes = ListBuffer.create();
    index = 0;
    throw Diagnostic.error("unimplemented");
  }
}
