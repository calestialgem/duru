package duru.configuration;

import duru.Namespace;
import duru.diagnostic.Subject;

/** Reference to a package in the configuration file. */
public record Reference(Subject subject, Namespace namespace) {}
