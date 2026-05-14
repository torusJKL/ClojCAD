## 1. Configuration

- [x] 1.1 Add `:nrepl {:port #shadow/env ["NREPL_PORT" 8777]}` to `shadow-cljs.edn` — returns integer 8777 as default, no env var needed
- [x] 1.2 Create `scripts/start-dev.js` — patches `shadow-cljs.edn` with integer port when env var is set, restores on exit
- [x] 1.3 Update `package.json` `dev` script to run `node scripts/start-dev.js`
- [x] 1.4 Create `shadow-cljs.edn.example` as git-tracked reference config

## 2. Documentation

- [x] 2.1 Update `README.org` to use `npm run dev` and document the `NREPL_PORT` environment variable

## 3. Verification

- [x] 3.1 Run `npm run dev` and confirm nREPL prints port 8777
- [x] 3.2 Run `NREPL_PORT=9999 npm run dev` and confirm nREPL listens on port 9999
