## ADDED Requirements

### Requirement: JVM nREPL port configurable via environment variable

The shadow-cljs JVM nREPL port SHALL be configurable at startup via the `NREPL_PORT` environment variable.

The JVM nREPL port MUST default to 8777 when `NREPL_PORT` is not set or is empty.

The configuration MUST be added as a top-level `:nrepl` map in `shadow-cljs.edn`, using shadow-cljs's built-in `#shadow/env` reader tag.

#### Scenario: Default port used when env var unset

- **WHEN** shadow-cljs starts without `NREPL_PORT` set
- **THEN** the JVM nREPL server listens on port 8777

#### Scenario: Custom port via environment variable

- **WHEN** shadow-cljs starts with `NREPL_PORT=9999`
- **THEN** the JVM nREPL server listens on port 9999

#### Scenario: Invalid port value

- **WHEN** shadow-cljs starts with `NREPL_PORT=abc`
- **THEN** shadow-cljs MUST fail to start or log a configuration error

#### Scenario: Port already in use

- **WHEN** shadow-cljs starts with an `NREPL_PORT` value that is already in use
- **THEN** shadow-cljs MUST fail to start with a port-in-use error
