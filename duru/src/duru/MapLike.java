package duru;

sealed interface MapLike<K, V> permits Map, MapBuffer {}
