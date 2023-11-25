package duru;

import java.math.BigDecimal;

public sealed interface Token {
  record Extern(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `extern`";
    }
  }

  record Public(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `public`";
    }
  }

  record Using(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `using`";
    }
  }

  record Struct(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `struct`";
    }
  }

  record Const(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `const`";
    }
  }

  record Var(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `var`";
    }
  }

  record Fn(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `fn`";
    }
  }

  record If(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `if`";
    }
  }

  record Else(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `else`";
    }
  }

  record For(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `for`";
    }
  }

  record Break(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `break`";
    }
  }

  record Continue(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `continue`";
    }
  }

  record Return(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `return`";
    }
  }

  record As(Location location) implements Token {
    @Override
    public String toString() {
      return "keyword `as`";
    }
  }

  record OpeningBrace(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `{`";
    }
  }

  record ClosingBrace(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `}`";
    }
  }

  record OpeningParenthesis(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `(`";
    }
  }

  record ClosingParenthesis(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `)`";
    }
  }

  record Semicolon(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `;`";
    }
  }

  record Dot(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `.`";
    }
  }

  record Comma(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `,`";
    }
  }

  record Tilde(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `~`";
    }
  }

  record Colon(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `:`";
    }
  }

  record ColonColon(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `::`";
    }
  }

  record Equal(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `=`";
    }
  }

  record EqualEqual(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `==`";
    }
  }

  record Star(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `*`";
    }
  }

  record StarEqual(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `*=`";
    }
  }

  record Slash(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `/`";
    }
  }

  record SlashEqual(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `/=`";
    }
  }

  record Percent(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `%`";
    }
  }

  record PercentEqual(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `%=`";
    }
  }

  record Caret(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `^`";
    }
  }

  record CaretEqual(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `^=`";
    }
  }

  record Exclamation(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `!`";
    }
  }

  record ExclamationEqual(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `!=`";
    }
  }

  record Plus(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `+`";
    }
  }

  record PlusPlus(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `++`";
    }
  }

  record PlusEqual(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `+=`";
    }
  }

  record Minus(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `-`";
    }
  }

  record MinusMinus(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `--`";
    }
  }

  record MinusEqual(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `-=`";
    }
  }

  record Ampersand(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `&`";
    }
  }

  record AmpersandAmpersand(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `&&`";
    }
  }

  record AmpersandEqual(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `&=`";
    }
  }

  record Pipe(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `|`";
    }
  }

  record PipePipe(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `||`";
    }
  }

  record PipeEqual(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `|=`";
    }
  }

  record Left(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `<`";
    }
  }

  record LeftLeft(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `<<`";
    }
  }

  record LeftEqual(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `<=`";
    }
  }

  record LeftLeftEqual(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `<<=`";
    }
  }

  record Right(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `>`";
    }
  }

  record RightRight(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `>>`";
    }
  }

  record RightEqual(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `>=`";
    }
  }

  record RightRightEqual(Location location) implements Token {
    @Override
    public String toString() {
      return "punctuation `>>=`";
    }
  }

  record Identifier(Location location, String text) implements Token {
    @Override
    public String toString() {
      return "identifier `%s`".formatted(text);
    }
  }

  record NumberConstant(Location location, BigDecimal value) implements Token {
    @Override
    public String toString() {
      return "number `%s`".formatted(Text.format(value));
    }
  }

  record StringConstant(Location location, String value) implements Token {
    @Override
    public String toString() {
      return "string `%s`".formatted(Text.quote(value));
    }
  }

  Location location();
}
