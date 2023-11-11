package duru;

public record List<E>(E[] elements) implements ListLike {}
