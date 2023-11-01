package duru.lectics;

/** Builds a {@link Number} from digits as the whole, fraction and exponent
 * parts. */
final class NumberBuilder {
  /** Type that signifies how to handle input given to the builder. The building
   * process is separated to the whole part, fraction after a fraction
   * separator, and the exponent after an exponent separator with an optional
   * exponent sign. */
  private enum Stage {
    WHOLE, FRACTION, EXPONENT;
  }

  /** Number of bits in a {@code double}s mantissa part. */
  private static final int DOUBLE_MANTISSA_WIDTH = 52;

  /** Number of bits that can be represented by a {@code double}s
   * significand. */
  private static final int DOUBLE_SIGNIFICAND_WIDTH = DOUBLE_MANTISSA_WIDTH + 1;

  /** {@code long} that can be ANDed with a {@code double}s bit pattern to
   * extract the mantissa part out. Also can be ANDed with a significand to get
   * the mantissa. */
  private static final long DOUBLE_MANTISSA_MASK =
    (1L << DOUBLE_MANTISSA_WIDTH) - 1;

  /** Number of bits in a {@code double}s exponent part. */
  private static final int DOUBLE_EXPONENT_WIDTH =
    Double.SIZE - DOUBLE_MANTISSA_WIDTH - 1;

  /** Number that can be added to a {@code double}s extracted exponent pattern
   * to get the exponent's value. */
  private static final int DOUBLE_EXPONENT_BIAS =
    (1 << DOUBLE_EXPONENT_WIDTH - 1) - 1;

  /** Maximum exponent pattern that a {@code double} can hold. Exponent patterns
   * above this are rounded to infinity. */
  private static final int DOUBLE_EXPONENT_LIMIT =
    (1 << DOUBLE_EXPONENT_WIDTH) - 1;

  /** Returns a clean builder for the given base. */
  static NumberBuilder create(NumberBase base) {
    return new NumberBuilder(base, 0, 0, 0, false, Stage.WHOLE);
  }

  /** Base of the number's scale. For non-powers of 2, it is also the base of
   * the number's exponent. For powers of 2, number's exponent is precision;
   * thus, is base 2. */
  private final NumberBase base;

  /** Combination of whole part and the fraction of the number. */
  private long digits;

  /** Number of digits in the fraction multiplied by -1. */
  private int scale;

  /** Magnitude of the exponent without its sign. */
  private int exponentMagnitude;

  /** Whether the exponent is negative. */
  private boolean exponentSign;

  /** Current stage the builder is in. */
  private Stage stage;

  /** Significand of the number in {@code significand*2^exponent}. */
  private long significand;

  /** Exponent of the number in {@code significand*2^exponent}. */
  private int exponent;

  /** Constructs. */
  private NumberBuilder(
    NumberBase base,
    long digits,
    int scale,
    int exponentMagnitude,
    boolean exponentSign,
    Stage stage)
  {
    this.base              = base;
    this.digits            = digits;
    this.scale             = scale;
    this.exponentMagnitude = exponentMagnitude;
    this.exponentSign      = exponentSign;
    this.stage             = stage;
  }

  /** Inserts a digit to the right of the number. Depending on the stage, the
   * digit would go the the whole part, the fraction or the exponent. Exponent
   * digits are always in decimal regardless of the base! Throws
   * {@link ArithmeticException} if the digit makes the number overflow! */
  void insert(int digit) {
    if (stage == Stage.EXPONENT) {
      if (exponentMagnitude > Integer.MAX_VALUE / 10) {
        throw onExponentOverflow();
      }
      exponentMagnitude *= 10;
      if (exponentMagnitude > Integer.MAX_VALUE - digit) {
        throw onExponentOverflow();
      }
      exponentMagnitude += digit;
      return;
    }
    if (stage == Stage.FRACTION) {
      if (scale == Integer.MIN_VALUE) {
        throw onOverflow();
      }
      scale--;
    }
    switch (base) {
      case NumberBase.PowerOfTwo b -> {
        if (Long.numberOfLeadingZeros(digits) < b.power()) {
          throw onOverflow();
        }
        digits <<= b.power();
      }
      case NumberBase.Arbitrary b -> {
        if (Long.compareUnsigned(digits, Long.divideUnsigned(-1, b.radix()))
          > 0)
        {
          throw onOverflow();
        }
        digits *= b.radix();
      }
    }
    if (Long.compareUnsigned(digits, -1 - digit) > 0) {
      throw onOverflow();
    }
    digits += digit;
  }

  /** Marks the end of the whole part and start of the fraction after the
   * fraction separator. Digits inserted after this will go to the fraction
   * part. There may be no digits provided after this call. */
  void fractionSeparator() {
    stage = Stage.FRACTION;
  }

  /** Marks the end of the whole part or the fraction and start of the exponent
   * with an optional sign to turn the exponent negative. Digits inserted after
   * this will go to the exponent part. There may be no digits provided after
   * this call. */
  void exponentSeparator(boolean isNegative) {
    stage        = Stage.EXPONENT;
    exponentSign = isNegative;
  }

  /** Builds a {@code double} using the currently inserted digits. Does not
   * invalidate or affect the builder. */
  double buildDouble() {
    startBuild();
    rescale(DOUBLE_SIGNIFICAND_WIDTH);
    if (significand == 0) {
      return 0;
    }
    exponent += DOUBLE_MANTISSA_WIDTH + DOUBLE_EXPONENT_BIAS;
    if (exponent >= DOUBLE_EXPONENT_LIMIT) {
      return Double.POSITIVE_INFINITY;
    }
    if (exponent > 0) {
      significand &= DOUBLE_MANTISSA_MASK;
      return Double
        .longBitsToDouble(
          (long) exponent << DOUBLE_MANTISSA_WIDTH | significand);
    }
    if (-exponent > DOUBLE_SIGNIFICAND_WIDTH) {
      return 0;
    }
    significand >>>= 1 - exponent;
    return Double.longBitsToDouble(significand);
  }

  /** Builds an {@code unsigned int} using the currently inserted digits. Does
   * not invalidate or affect the builder. Throws {@link ArithmeticException} if
   * the built number does not fit. */
  int buildInt() {
    var asLong = buildLong();
    if (Long.compareUnsigned(asLong, Integer.toUnsignedLong(-1)) > 0) {
      throw new ArithmeticException(
        "Number does not fit to an 32-bit unsigned integral!");
    }
    return (int) asLong;
  }

  /** Builds an {@code unsigned long} using the currently inserted digits. Does
   * not invalidate or affect the builder. Throws {@link ArithmeticException} if
   * the built number does not fit. */
  long buildLong() {
    startBuild();
    if (exponent > 0) {
      var consumableExponent =
        Integer.min(exponent, Long.numberOfLeadingZeros(significand));
      significand <<= consumableExponent;
      exponent     -= consumableExponent;
    }
    if (exponent < 0) {
      var consumableExponent =
        Integer.min(-exponent, Long.numberOfTrailingZeros(significand));
      significand >>>= consumableExponent;
      exponent      += consumableExponent;
    }
    if (exponent != 0) {
      throw new ArithmeticException("Number is not integral!");
    }
    return significand;
  }

  /** Defines significand from digits, and exponent from the scale and the
   * trailing exponent. */
  private void startBuild() {
    significand = digits;
    switch (base) {
      case NumberBase.PowerOfTwo b -> {
        var power = b.power();
        if (scale < Integer.MIN_VALUE / power) {
          throw onOverflow();
        }
        exponent = scale * power;
        if (!exponentSign) {
          exponent += exponentMagnitude;
          break;
        }
        if (exponent < Integer.MIN_VALUE + exponentMagnitude) {
          throw onExponentOverflow();
        }
        exponent -= exponentMagnitude;
      }
      case NumberBase.Arbitrary b -> {
        if (!exponentSign) {
          convertToBinaryExponent(b.radix(), scale + exponentMagnitude);
          break;
        }
        if (scale < Integer.MIN_VALUE + exponentMagnitude) {
          throw onExponentOverflow();
        }
        convertToBinaryExponent(b.radix(), scale - exponentMagnitude);
      }
    }
  }

  /** Converts the built number's exponent to base 2. */
  private void convertToBinaryExponent(int radix, int arbitraryExponent) {
    exponent = 0;
    var radixWidth       = Integer.SIZE - Integer.numberOfLeadingZeros(radix);
    var safeWidth        = Long.SIZE - radixWidth;
    var overflowingWidth = safeWidth + 1;
    for (var i = 0; i < arbitraryExponent; i++) {
      var oldSignificand = significand;
      var oldExponent    = exponent;
      rescale(overflowingWidth);
      var overflows =
        Long.compareUnsigned(significand, Long.divideUnsigned(-1, radix)) > 0;
      if (overflows) {
        significand = oldSignificand;
        exponent    = oldExponent;
        rescale(safeWidth);
      }
      significand *= radix;
    }
    for (var i = 0; i < -arbitraryExponent; i++) {
      rescale(Long.SIZE);
      var truncated   = Long.divideUnsigned(significand, radix);
      var middlePoint = truncated * radix + radix / 2;
      round(middlePoint, truncated, Long.SIZE);
    }
  }

  /** Rescales a number. Rounds to even when precision is lost. Throws
   * {@link ArithmeticException} if the exponent overflows. */
  private void rescale(int targetWidth) {
    var width = Long.SIZE - Long.numberOfLeadingZeros(significand);
    if (width == 0) {
      exponent = 0;
      return;
    }
    var change = targetWidth - width;
    if (change == 0) {
      return;
    }
    if (change > 0) {
      if (exponent < Integer.MIN_VALUE + change) {
        throw onExponentOverflow();
      }
      significand <<= change;
      exponent     -= change;
      return;
    }
    if (exponent > Integer.MAX_VALUE + change) {
      throw onExponentOverflow();
    }
    exponent -= change;
    var truncated   = significand >>> -change;
    var middlePoint = (truncated << 1) + 1 << -change - 1;
    round(middlePoint, truncated, targetWidth);
  }

  /** Rounds the built number if necessary. Throws {@link ArithmeticException}
   * if the exponent overflows. */
  private void round(long middlePoint, long truncated, int targetWidth) {
    var shouldRoundDown =
      Long.compareUnsigned(significand, middlePoint) < 0
        || significand == middlePoint && (truncated & 1) == 0;
    significand = truncated;
    if (shouldRoundDown) {
      return;
    }
    var maxSignificand = (1L << targetWidth + 1) - 1;
    if (Long.compareUnsigned(significand, maxSignificand) != 0) {
      significand++;
      return;
    }
    if (exponent == Integer.MAX_VALUE) {
      throw onExponentOverflow();
    }
    var roundedUpMaxSignificand = 1L << targetWidth - 1;
    significand = roundedUpMaxSignificand;
    exponent++;
  }

  /** Returns an exception to be thrown when the number's exponent overflows. */
  private ArithmeticException onExponentOverflow() {
    return new ArithmeticException(
      "Exponent is too %s!".formatted(exponentSign ? "small" : "big"));
  }

  /** Returns an exception to be thrown when the number's significand
   * overflows. */
  private ArithmeticException onOverflow() {
    return new ArithmeticException(
      "Number is too %s!".formatted(stage == Stage.WHOLE ? "big" : "precise"));
  }
}
