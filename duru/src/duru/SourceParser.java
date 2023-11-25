package duru;

import java.util.function.Supplier;

public final class SourceParser {
  public static List<Node.Declaration> parse(List<Token> tokens) {
    var parser = new SourceParser(tokens);
    return parser.parse();
  }

  private final List<Token> tokens;
  private int               index;

  private SourceParser(List<Token> tokens) {
    this.tokens = tokens;
  }

  private List<Node.Declaration> parse() {
    index = 0;
    var declarations = ListBuffer.<Node.Declaration>create();
    while (index != tokens.length()) {
      var declaration = expect(this::parseDeclaration, "declaration");
      declarations.add(declaration);
    }
    return declarations.toList();
  }

  private Optional<Node.Declaration> parseDeclaration() {
    var begin        = index;
    var externalName = Optional.<Token.StringConstant>absent();
    if (take(Token.Extern.class)) {
      externalName =
        Optional.present(expect(Token.StringConstant.class, "external name"));
    }
    var isPublic = take(Token.Public.class);
    if (take(Token.Using.class)) {
      var fallback = index;
      if (!take(Token.Identifier.class) || !take(Token.Equal.class)) {
        index = fallback;
        var aliased = expect(this::parseMention, "aliased symbol");
        expect(Token.Semicolon.class, "`;` of using declaration");
        return Optional
          .present(
            new Node.Using(
              location(begin),
              externalName,
              isPublic,
              Optional.absent(),
              aliased));
      }
      index = fallback;
      var newName = expect(Token.Identifier.class, "alias name");
      expect(Token.Equal.class, "`=` of using declaration");
      var aliased = expect(this::parseMention, "aliased symbol");
      expect(Token.Semicolon.class, "`;` of using declaration");
      return Optional
        .present(
          new Node.Using(
            location(begin),
            externalName,
            isPublic,
            Optional.present(newName),
            aliased));
    }
    if (take(Token.Struct.class)) {
      var name = expect(Token.Identifier.class, "struct name");
      expect(Token.OpeningBrace.class, "`{` of member list");
      var members = parseSeparated(this::parseBinding);
      expect(Token.ClosingBrace.class, "`}` of member list");
      return Optional
        .present(
          new Node.Struct(
            location(begin),
            externalName,
            isPublic,
            name,
            members));
    }
    if (take(Token.Const.class)) {
      var name = expect(Token.Identifier.class, "constant name");
      var type = expect(this::parseFormula, "constant type");
      expect(Token.Equal.class, "`=` of constant declaration");
      var initialValue = expect(this::parseExpression, "initial value");
      expect(Token.Semicolon.class, "`;` of constant declaration");
      return Optional
        .present(
          new Node.Const(
            location(begin),
            externalName,
            isPublic,
            name,
            type,
            initialValue));
    }
    if (take(Token.Var.class)) {
      var name = expect(Token.Identifier.class, "variable name");
      var type = expect(this::parseFormula, "variable type");
      expect(Token.Equal.class, "`=` of variable declaration");
      var initialValue = expect(this::parseExpression, "initial value");
      expect(Token.Semicolon.class, "`;` of variable declaration");
      return Optional
        .present(
          new Node.Var(
            location(begin),
            externalName,
            isPublic,
            name,
            type,
            initialValue));
    }
    if (take(Token.Fn.class)) {
      var name = expect(Token.Identifier.class, "function name");
      expect(Token.OpeningParenthesis.class, "`(` of parameter list");
      var parameters = parseSeparated(this::parseBinding);
      expect(Token.ClosingParenthesis.class, "`)` of parameter list");
      var returnType = expect(this::parseFormula, "return type");
      var body       = Optional.<Node.Statement>absent();
      if (!take(Token.Semicolon.class)) {
        body = Optional.present(expect(this::parseBlock, "function body"));
      }
      return Optional
        .present(
          new Node.Fn(
            location(begin),
            externalName,
            isPublic,
            name,
            parameters,
            returnType,
            body));
    }
    if (!externalName.isEmpty() || isPublic) {
      throw missing("declaration");
    }
    return Optional.absent();
  }

  private Optional<Node.Binding> parseBinding() {
    var begin = index;
    var name  = parse(Token.Identifier.class);
    if (name.isEmpty()) {
      return Optional.absent();
    }
    var type = expect(this::parseFormula, "type");
    return Optional
      .present(new Node.Binding(location(begin), name.getFirst(), type));
  }

  private Optional<Node.Statement> parseStatement() {
    return or(
      this::parseBlock,
      this::parseIf,
      this::parseFor,
      this::parseBreak,
      this::parseContinue,
      this::parseReturn,
      this::parseDeclare,
      this::parseAffect);
  }

  private Optional<Node.Block> parseBlock() {
    var begin = index;
    if (!take(Token.OpeningBrace.class)) {
      return Optional.absent();
    }
    var innerStatements = ListBuffer.<Node.Statement>create();
    while (true) {
      var innerStatement = parseStatement();
      if (innerStatement.isEmpty()) {
        break;
      }
      innerStatements.add(innerStatement.getFirst());
    }
    expect(Token.ClosingBrace.class, "`}` of block statement");
    return Optional
      .present(new Node.Block(location(begin), innerStatements.toList()));
  }

  private Optional<Node.If> parseIf() {
    var begin = index;
    if (!take(Token.If.class)) {
      return Optional.absent();
    }
    expect(Token.OpeningParenthesis.class, "`(` of if condition");
    var initializationStatements = parseRepeated(this::parseDeclare);
    var condition                =
      expect(this::parseExpression, "if condition");
    expect(Token.ClosingParenthesis.class, "`)` of if condition");
    var trueBranch  = expect(this::parseStatement, "if branch");
    var falseBranch = Optional.<Node.Statement>absent();
    if (take(Token.Else.class)) {
      falseBranch =
        Optional.present(expect(this::parseStatement, "else branch"));
    }
    return Optional
      .present(
        new Node.If(
          location(begin),
          initializationStatements,
          condition,
          trueBranch,
          falseBranch));
  }

  private Optional<Node.For> parseFor() {
    var begin = index;
    var label = parse(Token.Identifier.class);
    if (!label.isEmpty() && !take(Token.Colon.class)
      || !take(Token.For.class))
    {
      index = begin;
      return Optional.absent();
    }
    expect(Token.OpeningParenthesis.class, "`(` of for condition");
    var initializationStatements = parseRepeated(this::parseDeclare);
    var condition                =
      expect(this::parseExpression, "for condition");
    var interleavedStatement     = Optional.<Node.Affect>absent();
    if (take(Token.Semicolon.class)) {
      interleavedStatement =
        Optional
          .present(expect(this::parseLoneAffect, "interleaved statement"));
    }
    expect(Token.ClosingParenthesis.class, "`)` of for condition");
    var loopBranch  = expect(this::parseStatement, "for branch");
    var falseBranch = Optional.<Node.Statement>absent();
    if (take(Token.Else.class)) {
      falseBranch =
        Optional.present(expect(this::parseStatement, "else branch"));
    }
    return Optional
      .present(
        new Node.For(
          location(begin),
          label,
          initializationStatements,
          condition,
          interleavedStatement,
          loopBranch,
          falseBranch));
  }

  private Optional<Node.Break> parseBreak() {
    var begin = index;
    if (!take(Token.Break.class)) {
      return Optional.absent();
    }
    var label = parse(Token.Identifier.class);
    expect(Token.Semicolon.class, "`;` of break statement");
    return Optional.present(new Node.Break(location(begin), label));
  }

  private Optional<Node.Continue> parseContinue() {
    var begin = index;
    if (!take(Token.Continue.class)) {
      return Optional.absent();
    }
    var label = parse(Token.Identifier.class);
    expect(Token.Semicolon.class, "`;` of continue statement");
    return Optional.present(new Node.Continue(location(begin), label));
  }

  private Optional<Node.Return> parseReturn() {
    var begin = index;
    if (!take(Token.Return.class)) {
      return Optional.absent();
    }
    var value = parseExpression();
    expect(Token.Semicolon.class, "`;` of return statement");
    return Optional.present(new Node.Return(location(begin), value));
  }

  private Optional<Node.Declare> parseDeclare() {
    var begin = index;
    if (!take(Token.Var.class)) {
      return Optional.absent();
    }
    var name = expect(Token.Identifier.class, "variable name");
    var type = parseFormula();
    expect(Token.Equal.class, "`=` of variable declaration");
    var initialValue = expect(this::parseExpression, "initial value");
    expect(Token.Semicolon.class, "`;` of variable declaration");
    return Optional
      .present(new Node.Declare(location(begin), name, type, initialValue));
  }

  private Optional<Node.Affect> parseAffect() {
    var begin = index;
    var base  = parseExpression();
    if (base.isEmpty()) {
      return Optional.absent();
    }
    if (take(Token.PlusPlus.class)) {
      expect(Token.Semicolon.class, "`;` of increment statement");
      return Optional
        .present(new Node.Increment(location(begin), base.getFirst()));
    }
    if (take(Token.MinusMinus.class)) {
      expect(Token.Semicolon.class, "`;` of decrement statement");
      return Optional
        .present(new Node.Decrement(location(begin), base.getFirst()));
    }
    if (take(Token.Equal.class)) {
      var source = expect(this::parseExpression, "source of `=`");
      expect(Token.Semicolon.class, "`;` of assign statement");
      return Optional
        .present(new Node.Assign(location(begin), base.getFirst(), source));
    }
    if (take(Token.StarEqual.class)) {
      var source = expect(this::parseExpression, "source of `*=`");
      expect(Token.Semicolon.class, "`;` of assign statement");
      return Optional
        .present(
          new Node.MultiplyAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.SlashEqual.class)) {
      var source = expect(this::parseExpression, "source of `/=`");
      expect(Token.Semicolon.class, "`;` of assign statement");
      return Optional
        .present(
          new Node.QuotientAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.PercentEqual.class)) {
      var source = expect(this::parseExpression, "source of `%=`");
      expect(Token.Semicolon.class, "`;` of assign statement");
      return Optional
        .present(
          new Node.ReminderAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.PlusEqual.class)) {
      var source = expect(this::parseExpression, "source of `+=`");
      expect(Token.Semicolon.class, "`;` of assign statement");
      return Optional
        .present(new Node.AddAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.MinusEqual.class)) {
      var source = expect(this::parseExpression, "source of `-=`");
      expect(Token.Semicolon.class, "`;` of assign statement");
      return Optional
        .present(
          new Node.SubtractAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.LeftLeftEqual.class)) {
      var source = expect(this::parseExpression, "source of `<<=`");
      expect(Token.Semicolon.class, "`;` of assign statement");
      return Optional
        .present(
          new Node.ShiftLeftAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.RightRightEqual.class)) {
      var source = expect(this::parseExpression, "source of `>>=`");
      expect(Token.Semicolon.class, "`;` of assign statement");
      return Optional
        .present(
          new Node.ShiftRightAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.AmpersandEqual.class)) {
      var source = expect(this::parseExpression, "source of `&=`");
      expect(Token.Semicolon.class, "`;` of assign statement");
      return Optional
        .present(new Node.AndAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.CaretEqual.class)) {
      var source = expect(this::parseExpression, "source of `^=`");
      expect(Token.Semicolon.class, "`;` of assign statement");
      return Optional
        .present(new Node.XorAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.PipeEqual.class)) {
      var source = expect(this::parseExpression, "source of `|=`");
      expect(Token.Semicolon.class, "`;` of assign statement");
      return Optional
        .present(new Node.OrAssign(location(begin), base.getFirst(), source));
    }
    expect(Token.Semicolon.class, "`;` of discard statement");
    return Optional.present(new Node.Discard(location(begin), base.getFirst()));
  }

  private Optional<Node.Affect> parseLoneAffect() {
    var begin = index;
    var base  = parseExpression();
    if (base.isEmpty()) {
      return Optional.absent();
    }
    if (take(Token.PlusPlus.class)) {
      return Optional
        .present(new Node.Increment(location(begin), base.getFirst()));
    }
    if (take(Token.MinusMinus.class)) {
      return Optional
        .present(new Node.Decrement(location(begin), base.getFirst()));
    }
    if (take(Token.Equal.class)) {
      var source = expect(this::parseExpression, "source of `=`");
      return Optional
        .present(new Node.Assign(location(begin), base.getFirst(), source));
    }
    if (take(Token.StarEqual.class)) {
      var source = expect(this::parseExpression, "source of `*=`");
      return Optional
        .present(
          new Node.MultiplyAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.SlashEqual.class)) {
      var source = expect(this::parseExpression, "source of `/=`");
      return Optional
        .present(
          new Node.QuotientAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.PercentEqual.class)) {
      var source = expect(this::parseExpression, "source of `%=`");
      return Optional
        .present(
          new Node.ReminderAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.PlusEqual.class)) {
      var source = expect(this::parseExpression, "source of `+=`");
      return Optional
        .present(new Node.AddAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.MinusEqual.class)) {
      var source = expect(this::parseExpression, "source of `-=`");
      return Optional
        .present(
          new Node.SubtractAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.LeftLeftEqual.class)) {
      var source = expect(this::parseExpression, "source of `<<=`");
      return Optional
        .present(
          new Node.ShiftLeftAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.RightRightEqual.class)) {
      var source = expect(this::parseExpression, "source of `>>=`");
      return Optional
        .present(
          new Node.ShiftRightAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.AmpersandEqual.class)) {
      var source = expect(this::parseExpression, "source of `&=`");
      return Optional
        .present(new Node.AndAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.CaretEqual.class)) {
      var source = expect(this::parseExpression, "source of `^=`");
      return Optional
        .present(new Node.XorAssign(location(begin), base.getFirst(), source));
    }
    if (take(Token.PipeEqual.class)) {
      var source = expect(this::parseExpression, "source of `|=`");
      return Optional
        .present(new Node.OrAssign(location(begin), base.getFirst(), source));
    }
    return Optional.present(new Node.Discard(location(begin), base.getFirst()));
  }

  private Optional<Node.Formula> parseFormula() {
    var begin = index;
    if (take(Token.Star.class)) {
      var pointee = expect(this::parseFormula, "pointee type");
      return Optional.present(new Node.Pointer(location(begin), pointee));
    }
    var name = parseMention();
    if (name.isEmpty()) {
      return Optional.absent();
    }
    return Optional.present(new Node.Base(name.getFirst()));
  }

  private Optional<Node.Expression> parseExpression() {
    return parseLogicalOr();
  }

  private Optional<Node.Expression> parseLogicalOr() {
    var begin = index;
    var and   = parseLogicalAnd();
    if (and.isEmpty()) {
      return Optional.absent();
    }
    var or = and.getFirst();
    while (true) {
      if (take(Token.PipePipe.class)) {
        var rightOperand =
          expect(this::parseLogicalAnd, "right operand of binary `||`");
        or = new Node.LogicalOr(location(begin), or, rightOperand);
        continue;
      }
      return Optional.present(or);
    }
  }

  private Optional<Node.Expression> parseLogicalAnd() {
    var begin    = index;
    var equality = parseEquality();
    if (equality.isEmpty()) {
      return Optional.absent();
    }
    var and = equality.getFirst();
    while (true) {
      if (take(Token.AmpersandAmpersand.class)) {
        var rightOperand =
          expect(this::parseEquality, "right operand of binary `&&`");
        and = new Node.LogicalAnd(location(begin), and, rightOperand);
        continue;
      }
      return Optional.present(and);
    }
  }

  private Optional<Node.Expression> parseEquality() {
    var begin    = index;
    var relation = parseRelation();
    if (relation.isEmpty()) {
      return Optional.absent();
    }
    var equality = relation.getFirst();
    while (true) {
      if (take(Token.EqualEqual.class)) {
        var rightOperand =
          expect(this::parseRelation, "right operand of binary `==`");
        equality = new Node.EqualTo(location(begin), equality, rightOperand);
        continue;
      }
      if (take(Token.ExclamationEqual.class)) {
        var rightOperand =
          expect(this::parseRelation, "right operand of binary `!=`");
        equality = new Node.NotEqualTo(location(begin), equality, rightOperand);
        continue;
      }
      return Optional.present(equality);
    }
  }

  private Optional<Node.Expression> parseRelation() {
    var begin = index;
    var or    = parseBitwiseOr();
    if (or.isEmpty()) {
      return Optional.absent();
    }
    var relation = or.getFirst();
    while (true) {
      if (take(Token.Left.class)) {
        var rightOperand =
          expect(this::parseBitwiseOr, "right operand of binary `<`");
        relation = new Node.LessThan(location(begin), relation, rightOperand);
        continue;
      }
      if (take(Token.LeftEqual.class)) {
        var rightOperand =
          expect(this::parseBitwiseOr, "right operand of binary `<=`");
        relation =
          new Node.LessThanOrEqualTo(location(begin), relation, rightOperand);
        continue;
      }
      if (take(Token.Right.class)) {
        var rightOperand =
          expect(this::parseBitwiseOr, "right operand of binary `>`");
        relation =
          new Node.GreaterThan(location(begin), relation, rightOperand);
        continue;
      }
      if (take(Token.RightEqual.class)) {
        var rightOperand =
          expect(this::parseBitwiseOr, "right operand of binary `>=`");
        relation =
          new Node.GreaterThanOrEqualTo(
            location(begin),
            relation,
            rightOperand);
        continue;
      }
      return Optional.present(relation);
    }
  }

  private Optional<Node.Expression> parseBitwiseOr() {
    var begin = index;
    var xor   = parseBitwiseXor();
    if (xor.isEmpty()) {
      return Optional.absent();
    }
    var or = xor.getFirst();
    while (true) {
      if (take(Token.Pipe.class)) {
        var rightOperand =
          expect(this::parseBitwiseXor, "right operand of binary `|`");
        or = new Node.BitwiseOr(location(begin), or, rightOperand);
        continue;
      }
      return Optional.present(or);
    }
  }

  private Optional<Node.Expression> parseBitwiseXor() {
    var begin = index;
    var and   = parseBitwiseAnd();
    if (and.isEmpty()) {
      return Optional.absent();
    }
    var xor = and.getFirst();
    while (true) {
      if (take(Token.Caret.class)) {
        var rightOperand =
          expect(this::parseBitwiseAnd, "right operand of binary `^`");
        xor = new Node.BitwiseXor(location(begin), xor, rightOperand);
        continue;
      }
      return Optional.present(xor);
    }
  }

  private Optional<Node.Expression> parseBitwiseAnd() {
    var begin = index;
    var shift = parseShift();
    if (shift.isEmpty()) {
      return Optional.absent();
    }
    var and = shift.getFirst();
    while (true) {
      if (take(Token.Ampersand.class)) {
        var rightOperand =
          expect(this::parseShift, "right operand of binary `&`");
        and = new Node.BitwiseAnd(location(begin), and, rightOperand);
        continue;
      }
      return Optional.present(and);
    }
  }

  private Optional<Node.Expression> parseShift() {
    var begin    = index;
    var additive = parseAdditive();
    if (additive.isEmpty()) {
      return Optional.absent();
    }
    var shift = additive.getFirst();
    while (true) {
      if (take(Token.LeftLeft.class)) {
        var rightOperand =
          expect(this::parseAdditive, "right operand of binary `<<`");
        shift = new Node.LeftShift(location(begin), shift, rightOperand);
        continue;
      }
      if (take(Token.RightRight.class)) {
        var rightOperand =
          expect(this::parseAdditive, "right operand of binary `>>`");
        shift = new Node.RightShift(location(begin), shift, rightOperand);
        continue;
      }
      return Optional.present(shift);
    }
  }

  private Optional<Node.Expression> parseAdditive() {
    var begin          = index;
    var multiplicative = parseMultiplicative();
    if (multiplicative.isEmpty()) {
      return Optional.absent();
    }
    var additive = multiplicative.getFirst();
    while (true) {
      if (take(Token.Plus.class)) {
        var rightOperand =
          expect(this::parseMultiplicative, "right operand of binary `+`");
        additive = new Node.Addition(location(begin), additive, rightOperand);
        continue;
      }
      if (take(Token.Minus.class)) {
        var rightOperand =
          expect(this::parseMultiplicative, "right operand of binary `-`");
        additive =
          new Node.Subtraction(location(begin), additive, rightOperand);
        continue;
      }
      return Optional.present(additive);
    }
  }

  private Optional<Node.Expression> parseMultiplicative() {
    var begin = index;
    var unary = parseUnary();
    if (unary.isEmpty()) {
      return Optional.absent();
    }
    var multiplicative = unary.getFirst();
    while (true) {
      if (take(Token.Star.class)) {
        var rightOperand =
          expect(this::parseUnary, "right operand of binary `*`");
        multiplicative =
          new Node.Multiplication(
            location(begin),
            multiplicative,
            rightOperand);
        continue;
      }
      if (take(Token.Slash.class)) {
        var rightOperand =
          expect(this::parseUnary, "right operand of binary `/`");
        multiplicative =
          new Node.Quotient(location(begin), multiplicative, rightOperand);
        continue;
      }
      if (take(Token.Percent.class)) {
        var rightOperand =
          expect(this::parseUnary, "right operand of binary `%`");
        multiplicative =
          new Node.Reminder(location(begin), multiplicative, rightOperand);
        continue;
      }
      return Optional.present(multiplicative);
    }
  }

  private Optional<Node.Expression> parseUnary() {
    var begin = index;
    if (take(Token.Plus.class)) {
      var operand = expect(this::parseUnary, "operand of unary `+`");
      return Optional.present(new Node.Promotion(location(begin), operand));
    }
    if (take(Token.Minus.class)) {
      var operand = expect(this::parseUnary, "operand of unary `-`");
      return Optional.present(new Node.Negation(location(begin), operand));
    }
    if (take(Token.Tilde.class)) {
      var operand = expect(this::parseUnary, "operand of unary `~`");
      return Optional.present(new Node.BitwiseNot(location(begin), operand));
    }
    if (take(Token.Exclamation.class)) {
      var operand = expect(this::parseUnary, "operand of unary `!`");
      return Optional.present(new Node.LogicalNot(location(begin), operand));
    }
    return parseSuffix();
  }

  private Optional<Node.Expression> parseSuffix() {
    var begin   = index;
    var primary = parsePrimary();
    if (primary.isEmpty()) {
      return Optional.absent();
    }
    var suffix = primary.getFirst();
    while (true) {
      if (take(Token.OpeningParenthesis.class)) {
        var arguments = parseSeparated(this::parseExpression);
        expect(Token.ClosingParenthesis.class, "`)` of arguments");
        suffix = new Node.PostfixCall(location(begin), suffix, arguments);
        continue;
      }
      if (take(Token.Colon.class)) {
        var callee = expect(this::parsePrimary, "callee");
        expect(Token.OpeningParenthesis.class, "`(` of arguments");
        var remainingArguments = parseSeparated(this::parseExpression);
        expect(Token.ClosingParenthesis.class, "`)` of arguments");
        suffix =
          new Node.InfixCall(
            location(begin),
            suffix,
            callee,
            remainingArguments);
        continue;
      }
      if (take(Token.Dot.class)) {
        var member = expect(Token.Identifier.class, "member name");
        suffix = new Node.MemberAccess(location(begin), suffix, member);
        continue;
      }
      if (take(Token.As.class)) {
        var target = expect(this::parseFormula, "target type");
        suffix = new Node.Cast(location(begin), suffix, target);
        continue;
      }
      return Optional.present(suffix);
    }
  }

  private Optional<Node.Expression> parsePrimary() {
    var begin   = index;
    var mention = parseMention();
    if (mention.isEmpty()) {
      return or(
        this::parseStringConstant,
        this::parseNumberConstant,
        this::parseGrouping);
    }
    if (!take(Token.OpeningBrace.class)) {
      return Optional.present(new Node.Access(mention.getFirst()));
    }
    var memberInitializations = parseSeparated(this::parseMemberInitialization);
    expect(Token.ClosingBrace.class, "`}` of initialization");
    return Optional
      .present(
        new Node.Initialization(
          location(begin),
          mention.getFirst(),
          memberInitializations));
  }

  private Optional<Node.MemberInitialization> parseMemberInitialization() {
    var begin  = index;
    var member = parse(Token.Identifier.class);
    if (member.isEmpty()) {
      return Optional.absent();
    }
    expect(Token.Equal.class, "`=` of member initialization");
    var value = expect(this::parseExpression, "member initial value");
    return Optional
      .present(
        new Node.MemberInitialization(
          location(begin),
          member.getFirst(),
          value));
  }

  private Optional<Node.Grouping> parseGrouping() {
    var begin = index;
    if (!take(Token.OpeningParenthesis.class)) {
      return Optional.absent();
    }
    var grouped = expect(this::parseExpression, "grouped expression");
    expect(Token.ClosingParenthesis.class, "`)` of groping");
    return Optional.present(new Node.Grouping(location(begin), grouped));
  }

  private Optional<Node.NumberConstant> parseNumberConstant() {
    return parse(Token.NumberConstant.class)
      .transform(Node.NumberConstant::new);
  }

  private Optional<Node.StringConstant> parseStringConstant() {
    return parse(Token.StringConstant.class)
      .transform(Node.StringConstant::new);
  }

  private Optional<Node.Mention> parseMention() {
    var begin = index;
    var name  = parse(Token.Identifier.class);
    if (name.isEmpty()) {
      return Optional.absent();
    }
    var identifiers = ListBuffer.<Token.Identifier>create();
    identifiers.add(name.getLast());
    while (!parse(Token.ColonColon.class).isEmpty()) {
      var subspace = expect(Token.Identifier.class, "identifier");
      identifiers.add(subspace);
    }
    return Optional
      .present(new Node.Mention(location(begin), identifiers.toList()));
  }

  private Location location(int begin) {
    var beginLocation = tokens.get(begin).location();
    var endLocation   = tokens.get(index - 1).location();
    return Location
      .at(
        beginLocation.source(),
        beginLocation.beginIndex(),
        endLocation.endIndex());
  }

  private <V> List<V> parseSeparated(Supplier<Optional<V>> parserFunction) {
    var list = ListBuffer.<V>create();
    while (true) {
      var value = parserFunction.get();
      if (value.isEmpty()) {
        break;
      }
      list.add(value.getFirst());
      if (!take(Token.Comma.class)) {
        break;
      }
    }
    return list.toList();
  }

  private <V> List<V> parseRepeated(Supplier<Optional<V>> parserFunction) {
    var list = ListBuffer.<V>create();
    while (true) {
      var value = parserFunction.get();
      if (value.isEmpty()) {
        break;
      }
      list.add(value.getFirst());
    }
    return list.toList();
  }

  private <T extends Token> T expect(Class<T> tokenClass, String explanation) {
    return expect(() -> parse(tokenClass), explanation);
  }

  private <Value> Value expect(
    Supplier<Optional<Value>> parserFunction,
    String explanation)
  {
    var value = parserFunction.get();
    if (!value.isEmpty()) {
      return value.getLast();
    }
    throw missing(explanation);
  }

  private RuntimeException missing(String explanation) {
    if (index == 0) {
      return Diagnostic
        .error(
          tokens.getFirst().location(),
          "expected %s instead of %s at beginning of file",
          explanation,
          tokens.getFirst());
    }
    if (index == tokens.length()) {
      return Diagnostic
        .error(
          tokens.getLast().location(),
          "expected %s after %s at end of file",
          explanation,
          tokens.getLast());
    }
    return Diagnostic
      .error(
        tokens.get(index).location(),
        "expected %s instead of %s after %s",
        explanation,
        tokens.get(index),
        tokens.get(index - 1));
  }

  private <T extends Token> boolean take(Class<T> tokenClass) {
    return !parse(tokenClass).isEmpty();
  }

  @SuppressWarnings("unchecked")
  private <T extends Token> Optional<T> parse(Class<T> tokenClass) {
    if (index == tokens.length()) {
      return Optional.absent();
    }
    var token = tokens.get(index);
    if (!tokenClass.isInstance(token)) {
      return Optional.absent();
    }
    index++;
    return Optional.present((T) token);
  }

  @SafeVarargs
  @SuppressWarnings("unchecked")
  private <V> Optional<V> or(
    Supplier<Optional<? extends V>>... parserFunctions)
  {
    for (var parserFunction : parserFunctions) {
      var value = parserFunction.get();
      if (!value.isEmpty()) {
        return (Optional<V>) value;
      }
    }
    return Optional.absent();
  }
}
