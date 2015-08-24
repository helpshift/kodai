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

Use META-[MOUSE LEFT] to select a node:

![selecting a node](https://cloud.githubusercontent.com/assets/1455572/9433469/3c32d67e-4a51-11e5-9675-79eb7fa60e88.png)

It can be seen that we have selected the top level node - `kodai.core/insight`. It has two adjacent nodes: `kodai.core.viewer/viewer` and `kodai.bundle/bundle`, in blue and the corresponding downstream nodes in red. We can META-[MOUSE LEFT] on the two of them, firstly the `kodai.core.viewer/viewer` node:

![viewer node](https://cloud.githubusercontent.com/assets/1455572/9433476/5205f094-4a51-11e5-8cc1-e272bfbbe747.png)

and the `kodai.bundle/bundle` node:

![bundle node](https://cloud.githubusercontent.com/assets/1455572/9433600/c022a6d4-4a52-11e5-86ea-28e9eea83dd5.png)

### select node

To focus only on the selected var, press `v`. This will only show `kodai.bundle/bundle` and it's downstream nodes:

![zoom on bundle](https://cloud.githubusercontent.com/assets/1455572/9433531/ee2f42ea-4a51-11e5-959a-5cfae07a6dec.png)

And press `v` again to go back to the global view.

### collapse node

We can also selectively collapse nodes to save screen space, press `c` to collapse the `kodai.bundle/bundle` node:

![collapse on bundle](https://cloud.githubusercontent.com/assets/1455572/9433557/2e275400-4a52-11e5-8b75-930c1e71831a.png)

It can be seen that the `kodai.bundle/bundle` goes yellow and selecting `kodai.core/insight` will save more space than before:

![insight with collapse](https://cloud.githubusercontent.com/assets/1455572/9433625/280fb6a6-4a53-11e5-924a-c3d0f9dde66f.png)

Multiple nodes can be collapsed by pressing `c`:

![multiple collapse](https://cloud.githubusercontent.com/assets/1455572/9433649/7862de26-4a53-11e5-8395-4378c7ac854a.png)

As well as revived by pressing `c` again on the collapsed node:

![reviving collapse](https://cloud.githubusercontent.com/assets/1455572/9433660/a08bfafe-4a53-11e5-8cdc-8d3aa9c64541.png)

### reverse graph

A reversal of the call graph can be toggled by pressing `r`:

![reversal](https://cloud.githubusercontent.com/assets/1455572/9433741/4da7c100-4a54-11e5-9363-f58e28e4ba80.png)

### hide nodes

Nodes can be hidden by selecting and pressing `h`, all hidden nodes can be put back by pressing `CTRL-h`. Currently there is no way to selectively put nodes back in the ui, but this feature is planned for the future.

### hide namespaces

Entire namespaces can be hidden by selecting a node and pressing `n`, all hidden namespaces can be put back by pressing `CTRL-n`. Currently there is no way to selectively put nodes back in the ui, but this feature is planned for the future.

### hide/show dynamic vars

dynamic vars in a project (ear-muffed vars) can be toggled on and off with the `d` key.

### hide/show singletons vars

singletons on the screen can be toggled on and off with the `s` key.

## License

Copyright © 2015 [Helpshift](https://www.helpshift.com/)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
