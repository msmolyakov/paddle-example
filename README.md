MVP:
- [x] alice.exchanges()
- [x] correctly calc all fees
- [x] wrap wavesJ IOExceptions in my own
- [x] `alice.invokes(i -> i.dApp(dApp).func(""))`
- [ ] `node.send(invokeScript(alice).dApp("dApp").func(""))`
- [x] fix: REST API key
- [ ] save test logs in target/datetime subdir + waves.log + test.log; verbose mode
- [x] AssertJ
- [ ] as setScript: truffle way (deploy, test/main values, etc)
- [ ] tutorial article to Habr (ru, en), Medium + Travis howto

Release:
- [ ] Node extends wavesJ Node
- [ ] centralized error handling
- [ ] api `account.scriptInfo()`
- [ ] api `account.transactions(limit, after)` and `node.transactions(account, limit, after)`
- [ ] `.withProofs(...)` and don't sign if `[0]` is specified

IDEAS:
* assert cause of UTX error from node log
* support micro forks (change miner settings)
* ${var} in contracts. Access from Env instance with specified profile
* imports in contracts + imports hub
* support mainnet version only?
* unit testing
* integration tests itself
* is volume needed?
* alice.placesOrder(); alice.cancelsOrder(); add matcher + matcher.log into target. Fix: remove deprecated matcher settings from waves.conf
* create maven archetype
* status badges for Github
* support for master branch
* support in IntelliJ plugin
* what will be on release new version of node or WavesJ
* is local.conf works and needed?
* multi-stage build
* Scala-friendly API
* beautify Docker Hub page + autobuild from github
* run tests on another non-Docker node
