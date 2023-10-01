#include "duru.hxx"

#include <iostream>
#include <string>

duru::NominalSubject::NominalSubject(std::string name)
    : name{std::move(name)} {}

void duru::NominalSubject::append_to(std::ostream& target) const {
  target << name;
}

void duru::launch(duru::Subject const& subject) {
  subject.append_to(std::cout);
  std::cout << "Hello, Duru!" << std::endl;
}
