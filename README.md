# Aero World

A persistent world and with economy focus for flight simulators. Currently, Aero World is in a super early release and should not be "seriously" used.

## Prerequisites

You will need [Clojure CLI](https://clojure.org/guides/getting_started).

## Running Web Server in REPL

```bash
clj
```

Inside the REPL you may run these commands to start the webserver

```clojure
(require '[aero-world.core :refer :all])
(def server (-dev-main))
```

## License

Copyright Rafael Coelho © 2020
