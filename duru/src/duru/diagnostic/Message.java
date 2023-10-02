package duru.diagnostic;

import java.util.Optional;

record Message(
  MessageType type,
  Optional<String> subject,
  String body,
  int causes)
{}
