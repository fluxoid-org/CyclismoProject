Subtrees
--------

- Add the appropriate remote:

```
git remote add fluxutils git@github.com:fluxoid-org/FluxUtils.git
```

- pull:

```
git subtree pull --squash -P Cyclismo/libs/FluxUtils fluxutils master
```

- push:

```
git subtree push -P Cyclismo/libs/FluxUtils fluxutils remote_branch
```