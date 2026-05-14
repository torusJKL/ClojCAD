## Why

The JVM-level nREPL port for shadow-cljs is currently unconfigured, so shadow-cljs assigns a random port at each startup (e.g., 32947, 37833, 39517). This makes it inconvenient to connect from editors like Emacs/CIDER that expect a predictable port. Adding explicit configuration with an env-var override solves this while keeping the port adjustable per-session.

## What Changes

- Add a top-level `:nrepl` config to `shadow-cljs.edn` that sets a fixed JVM nREPL port (8777 by default)
- Make the port overridable via the `NREPL_PORT` environment variable at startup
- No change to the HTTP dev server port (8700)
- Keep existing `:devtools :nrepl-port 8777` as-is (build-level ClojureScript nREPL, separate from the JVM nREPL)

## Capabilities

### New Capabilities

- `nrepl-port-config`: Support configuring the JVM nREPL port via environment variable when starting shadow-cljs from the command line

### Modified Capabilities

*(None — no existing specs are changing)*

## Impact

- **`shadow-cljs.edn`**: Add `:nrepl {:port #shadow/env ["NREPL_PORT" 8777]}` — uses integer default from vector, patched by script when env var is set
- **`scripts/start-dev.js`**: Patches `shadow-cljs.edn` with integer port when `NREPL_PORT` is set, restores on exit
- **`package.json`**: `npm run dev` runs the start script (or raw `npx shadow-cljs watch dev` also works)
- **`README.org`**: Document how to set `NREPL_PORT` and update connection instructions from random port to predictable 8777
- **No new dependencies** — uses shadow-cljs's built-in `#shadow/env` reader tag
