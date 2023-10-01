#pragma once

#include <ostream>
#include <string>

namespace duru {
  class Any {
  public:
    Any() = default;
    Any(const Any&) = delete;
    Any(Any&&) = delete;
    virtual ~Any() = 0;
    Any& operator=(const Any&) = delete;
    Any& operator=(Any&&) = delete;
  };

  class Subject : public Any {
  public:
    virtual void append_to(std::ostream& target) const = 0;
  };

  class NominalSubject final : public Subject {
  public:
    explicit NominalSubject(std::string name);
    void append_to(std::ostream& target) const override;

  private:
    std::string name;
  };

  void launch(Subject const& subject);
}
