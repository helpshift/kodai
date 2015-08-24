# kodai

[![Build Status](https://travis-ci.org/helpshift/hydrox.svg?branch=master)](https://travis-ci.org/helpshift/hydrox)

visualisations of function connectivity

![kodai logo](https://raw.githubusercontent.com/helpshift/kodai/master/template/assets/img/kodai.png)

## Installation


In your `project.clj`, add hydrox to the `[:profiles :dev :dependencies]` entry:  

```clojure
(defproject ...
  ...
  :profiles {:dev {:dependencies [...
                                  [helpshift/kodai "0.1.0"]
                                  ...]}}
  ...)
```

## Usage

[kodai](https://www.github.com/helpshift/kodai) (pronounced "code-eye") is a visualization tool for function connectivity within a codebase. To create a visualization of your codebase, run as follows:

```clojure
(use 'kodai.core)

(insight #"src/<directory>")
```

## Quickstart

Running `insight` on kodai itself yields:

```clojure
(insight #"src/kodai")
```

![initial](https://cloud.githubusercontent.com/assets/1455572/9433452/f421e9b0-4a50-11e5-8744-ebea8cc4b695.png)

Use META-<MOUSE LEFT> to select a node:

![selecting a node](https://cloud.githubusercontent.com/assets/1455572/9433469/3c32d67e-4a51-11e5-9675-79eb7fa60e88.png)

It can be seen that we have selected the top level node - `kodai.core/insight`. It has two adjacent nodes: `kodai.core.viewer/viewer` and `kodai.bundle/bundle`, we can META-<MOUSE LEFT> on the two of them, firstly the `kodai.core.viewer/viewer` node:

![viewer node](https://cloud.githubusercontent.com/assets/1455572/9433476/5205f094-4a51-11e5-8cc1-e272bfbbe747.png)

and the `kodai.bundle/bundle` node:

![bundle node](https://cloud.githubusercontent.com/assets/1455572/9433476/5205f094-4a51-11e5-8cc1-e272bfbbe747.png)

### select node

To focus only on the selected var, press `v`:

![zoom on bundle](https://cloud.githubusercontent.com/assets/1455572/9433531/ee2f42ea-4a51-11e5-959a-5cfae07a6dec.png)

And press `v` again to go back to the global view.

### collapse node

We can also selectively collapse nodes to save screen space, press `c` to collapse the `kodai.bundle/bundle` node:

![collapse on bundle](https://cloud.githubusercontent.com/assets/1455572/9433557/2e275400-4a52-11e5-8b75-930c1e71831a.png)




## License

Copyright © 2015 [Helpshift](https://www.helpshift.com/)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
