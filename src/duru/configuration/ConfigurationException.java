package duru.configuration;

public final class ConfigurationException extends Exception {
    private final String location;

    public ConfigurationException(
        String contents,
        int start,
        int length,
        String format,
        Object... arguments)
    {
        super(format.formatted(arguments));
        var line   = 1;
        var column = 1;
        for (var i = 0; i < start; i++) {
            if (contents.codePointAt(i) == '\n') {
                line++;
                column = 1;
            }
            else {
                column++;
            }
        }
        var buffer = new StringBuilder();
        buffer.append(line);
        buffer.append(':');
        buffer.append(column);
        if (length > 1) {
            buffer.append(line);
            buffer.append(':');
            buffer.append(column + length);
        }
        location = buffer.toString();
    }

    public String location() { return location; }
}
