// Generic data structure utilities.
//
// See [duru::Variant].
// See [duru::Result].

#pragma once

#include "duru/containers.hxx"

#include <array>
#include <cstddef>

namespace duru {
  // Returns the maximum of a single number, which is itself.
  //
  // Used for pattern matching on a template pack of numbers.
  [[nodiscard]] constexpr auto max(auto number) noexcept { return number; }

  // Returns the maximum of the given numbers using `operator<`.
  [[nodiscard]] constexpr auto max(
      auto firstNumber, auto... remainingNumbers) noexcept {
    auto maxOfRemaining = max(remainingNumbers...);
    if (firstNumber < maxOfRemaining) { return maxOfRemaining; }
    return firstNumber;
  }

  // Returns the sum of a single number, which is itself.
  //
  // Used for pattern matching on a template pack of numbers.
  [[nodiscard]] constexpr auto sum(auto number) noexcept { return number; }

  // Returns the sum of the given numbers using `operator+`.
  [[nodiscard]] constexpr auto sum(
      auto firstNumber, auto... remainingNumbers) noexcept {
    return firstNumber + sum(remainingNumbers...);
  }

  // Holds a [Tuple] member at a particular index.
  //
  // Used for keeping tract of which index holds which member in a [Tuple].
  template<std::size_t index, typename Member>
  struct TupleMemberHolder {
    Member member;
  };

  // [Tuple] and sub-[Tuple]s that hold the index of the first index.
  //
  // Used for pattern matching on the members. Has a base case and a recursive
  // case.
  template<std::size_t index, typename... Members>
  struct IndexedTuple;

  // Recursive case for the [IndexedTuple].
  template<
      std::size_t index,
      typename FirstMember,
      typename... RemainingMembers>
  struct IndexedTuple<index, FirstMember, RemainingMembers...>
      : TupleMemberHolder<index, FirstMember>,
        IndexedTuple<index + 1, RemainingMembers...> {};

  // Base case for the [IndexedTuple].
  template<std::size_t index>
  struct IndexedTuple<index> {};

  // Container of product of an ordered collection of types.
  template<typename... Members>
  struct Tuple : IndexedTuple<0, Members...> {};

  // Type with only one instance.
  //
  // Implemented as an empty [Tuple].
  struct Unit : Tuple<> {};

  // Returns the member of a [Tuple] at a compile-time known index.
  template<std::size_t index, typename Member>
  constexpr Member& get(TupleMemberHolder<index, Member>& tuple) noexcept {
    return tuple.member;
  }

  // Contains one type out of all alternative ones; a tagged union.
  template<typename... Alternatives>
  struct Variant {
    // Data of the currently active alternative of the variant.
    alignas(max(alignof(Alternatives)...))
        std::array<char, max(sizeof(Alternatives)...)> data;

    // Identifier of the currently active alternative.
    std::size_t tag;
  };

  // Contains a value or error.
  //
  // Useful for holding the outcome of a subroutine.
  template<typename Value, typename Error>
  struct [[nodiscard]] Result : Variant<Value, Error> {};
}
