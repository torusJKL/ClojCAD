## Context

The JVM-level nREPL port in shadow-cljs is currently unconfigured. shadow-cljs starts a JVM nREPL on a random port by default (the user sees ports like 32947, 37833 in startup output). There is no mechanism to set a fixed port or override it per-session. The config file already has `:nrepl-port 8777` under `:devtools`, but that controls the build-level ClojureScript nREPL — a separate server from the JVM nREPL that editors connect to.

## Goals / Non-Goals

**Goals:**
- Add a top-level `:nrepl` config to shadow-cljs.edn with a fixed port (8777)
- Allow `NREPL_PORT` env var to override the port at startup
- Zero new dependencies
- No changes to the HTTP dev server port (8700) or the build-level `:devtools :nrepl-port`

**Non-Goals:**
- Not changing the build-level ClojureScript nREPL config (`:devtools :nrepl-port 8777` stays as-is)
- Not adding a standalone shell script or Makefile wrapper
- Not supporting `.env` files or other config file formats
- Not changing how users start shadow-cljs (`npx shadow-cljs watch dev` remains the same)

## Decisions

1. **Add top-level `:nrepl {:port ...}` config** — shadow-cljs uses this to set the JVM nREPL port. This is the port printed at startup and the one editors connect to.
2. **Use `#shadow/env` for default, script for override** — `#shadow/env ["NREPL_PORT" 8777]` returns the integer 8777 when the env var is unset (the default from the vector). When `NREPL_PORT` IS set, `#shadow/env` returns a string which would fail for the integer port. So `scripts/start-dev.js` patches the config with the integer port via regex, spawns shadow-cljs, and restores the original on exit.
3. **`shadow-cljs.edn` stays committed** — not gitignored. Works out of the box with `npx shadow-cljs watch dev` or `npm run dev`. The script is only needed when overriding the port.
3. **Env var name `NREPL_PORT`** — follows the convention from Leiningen, Clojure tools.deps, and CIDER. Familiar to Clojure developers.
4. **Default to 8777** — matches the existing `:devtools :nrepl-port` value for consistency, and is the port the user already expects from their README.

## Risks / Trade-offs

- Shadow-cljs reader tags only work in `.edn` config files — if the project ever moves to programmatic config, this approach would need revisiting. Low risk — no such migration is planned.
- `#shadow/env` resolves at config load time, not per-rebuild — the env var must be set before starting shadow-cljs. Acceptable — this matches how most env-var config works.
