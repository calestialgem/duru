package duru.configuration;

public final class ConfigurationParseException extends Exception {
    private final String location;

    private ConfigurationParseException(
        String message,
        Throwable cause,
        String location)
    {
        super(message, cause);
        this.location = location;
    }

    static ConfigurationParseException create(
        String contents,
        int start,
        int length,
        String format,
        Object... arguments)
    {
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
        return new ConfigurationParseException(
            format.formatted(arguments),
            null,
            buffer.toString());
    }

    public String location() { return location; }
}
