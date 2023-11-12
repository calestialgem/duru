// Text manipulation utilities.
//
// See [duru::String].

#pragma once

namespace duru {
  // String is a linear collection of zero or more [Character]s.
  struct String {};

  // A character is a linear collection of one or more [Codepoint]s.
  struct Character {};

  // A codepoint is a linear collection of one or more [Byte]s, encoded using
  // UTF-8.
  struct Codepoint {};

  // Strongly typed version of [char].
  struct Byte {};
}
