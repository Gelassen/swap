# Swap
The idea behind this project is to give people an option to exchange products and services without money (barter).  

# Github Actions CI
Similar to the case of <a href="https://github.com/Gelassen/government-rus">government-rus</a> tests passes locally, but on the server one test has been failing which marks CI run as unsuccessful. The root cause of this is unclear. Build reports created at the end of the build work as 2nd source of confidence as a workaround of this issue.  

# Tech doc in user stories
As a user I open an app and write services I can offer. 

As a user I open an app and write services or products I am interested in.

As a user after finish profile with "I can offer" & "I am interested in" I can find people who offers services I am interested in. 

As an application I can chain people (more than two) who are interested in services offered by at least one in chain. 

# Installation
This is a client-server application with use of ethereum chain. The cost of deployment and operation of project work on Mainnet force us to use private own network. At time of writing development goes with help of Goerly testnet. 

1. Server is run by setting production configuration file and running commands: ```$npm install && npm run start:prod```
2. Server would require database which creation and configuration file lays in the repository
3. Android mobile client would require release build with sing keys generated by you.
4. Ethereum chain contracts, ```SwapValue.sol``` and ```SwapChain.sol``` would require deployment. Their addresses available after depoloyment should be passed to mobile client and server configs.
5. Mobile client operates with chain over java\kotlin wrappers which require binary code of the compiled contracts. Binary code is available after execution ```$npx hardhat compile```

# Deploy own self-hosted chain
1. Create folders for nodes
2. Create accounts in nodes: 

```$geth --datadir ./node1/data account new```

pwd: node1

Public address of the key:   0x06Ba36FeA25dAAc20d7d00f95a491566E80a610a
Path of the secret key file: node1/data/keystore/UTC--2022-11-07T07-00-25.976610112Z--06ba36fea25daac20d7d00f95a491566e80a610a

```$geth --datadir ./node2/data account new```

pwd: node2

Public address of the key:   0xaB781fEF949CB48a554C15Be2c36b9E1d2663dee
Path of the secret key file: node2/data/keystore/UTC--2022-11-07T07-02-51.751446401Z--ab781fef949cb48a554c15be2c36b9e1d2663dee

```$geth --datadir ./node3/data account new```

pwd: node3

Public address of the key:   0xDb4E3996071D1B0d37336b6D082ee0176395749b
Path of the secret key file: node3/data/keystore/UTC--2022-11-07T07-03-44.502277991Z--db4e3996071d1b0d37336b6d082ee0176395749b

3. Generate genesis block:

```$puppeth```

4. Export chain configuration: 

```$puppeth (reselect option 2. Manage existing genesis)```

5. Initialize all nodes with chain config: 

```
$geth --datadir ./node1/data init <chain config>.json
$geth --datadir ./node2/data init <chain config>.json
$geth --datadir ./node3/data init <chain config>.json
```

6. Start nodes: 

A.
```
$geth --datadir ./node1/data --port 2001 (default authrpc.port 8551)
$geth --datadir ./node2/data --port 2002 --authrpc.port 8552
$geth --datadir ./node3/data --port 2003 --authrpc.port 8553
```
B. (preferable)
```
$./swap/blockchain/start_nodes.sh
$<enter your root pwd>
```
The script is tested on Ubuntu 22.04, but still should be POSIX-compatible. 

The script is partly finished. There is a not complete issue with passing ```enode`` url to each node. More details regarding this issue are here:
https://unix.stackexchange.com/questions/724525/custom-scenario-for-linux-shell-script/726158#726158

This script will start nodes available over http and print ```enode``` url for each node. You still have to add them manually as peers which is described in the next step.

7. Link all nodes with a main one: 

A.
```
$geth attach ipc:node1/data/geth.ipc
$admin.nodeInfo.enode
(reply would be something similar to "enode://64dccd02d5d1166cfb4913f0d0c164dff2b9c61fd55182461010569e15319c7ff5cb4dc8b502e441c38c80ae1b42c2cc95c7e170ed973bb0353d766669c5447c@195.178.22.21:2001?discport=39805")
$geth attach ipc:node2/data/geth.ipc
$admin.addPeer("enode://64dccd02d5d1166cfb4913f0d0c164dff2b9c61fd55182461010569e15319c7ff5cb4dc8b502e441c38c80ae1b42c2cc95c7e170ed973bb0353d766669c5447c@127.0.0.1:2001")
```
Repeat for all nodes: each node should have reference in peers on all OTHERS nodes. Known issue: https://github.com/ethereum/go-ethereum/issues

B.
<complete me>

8. Set reward collector:

```
$miner.setEtherbase(<account for collecting rewards from mining>)
```

9. To solve invalid address error which appears on contract's method invocation:
```
web3.eth.defaultAccount = web3.eth.coinbase
```

10. Unlock account to make it available to either collect the reward from mining or withdraw ether on transaction execution:
```
personal.unlockAccount("0x62f8dc8a5c80db6e8fcc042f0cc54a298f8f2ffd")
```

Please note, unlock account is not avaiable by default for nodes run with http access due security breach reasons. For development purpouses nodes are run over http with flag ``` --allow-insecure-unlock```, for deployment in the production alternative way should be found. 

9. To make node miner:

```
$geth attach ipc:node3/data/geth.ipc
$miner.start()
$miner.stop()
$eth.getBalance(eth.accounts[0])
```

10. Deploy nodes: 

```
npx hardhat run --network localhost scripts/deploy.ts
```

# Inspired by
The idea to use barter with assist of the modern tech was heard by me from russian ex-oligarch Herman Sterligov during finance crisis in 2008. The idea of matching and chaining people together by their needs and offers has borrowed few things from dead Google project Schemer (https://gcemetery.co/google-schemer/) 

# Contacts
For reporting issue use 'Issues' tab, for offers -- pull requests.

Email: swap.dev.abc@gmail.com
