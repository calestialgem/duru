#pragma once

#include <format>
#include <optional>
#include <string>
#include <vector>

namespace duru {
  class Diagnostic {
  public:
    static Diagnostic create();

    void begin();

    void skip();

    template <typename... Arguments>
    void failure(std::string format, Arguments&&... arguments);

    template <typename... Arguments>
    void error(std::string format, Arguments&&... arguments);

    template <typename... Arguments>
    void warning(std::string format, Arguments&&... arguments);

    template <typename... Arguments>
    void info(std::_Fmt_string<Arguments...> format, Arguments&&... arguments);

  private:
    struct MessageType {
      std::string title;
      bool is_fatal;
    };

    struct Message {
      MessageType type;
      std::optional<std::string> subject;
      std::string body;
    };

    std::vector<int> causes;
    std::vector<Message> messages;
  };
}
