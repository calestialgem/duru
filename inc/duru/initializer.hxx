// Holds initialization command.
//
// See [duru::initialize(duru::String, duru::String)].

#pragma once

#include <duru/text.hxx>

namespace duru {
  // Initializes a new project in the given directory with the given name.
  //
  // Returns the error code. Fails if there is a configuration file in the given
  // directory or its parents. Additionally propagates any IO failure.
  //
  // Initialization is creating the necessary configuration to setup a project
  // in the given directory. Also, creates a simple source file for debugging
  // the setup.
  int initialize(String directory, String name);
}
