const keythereum = require("keythereum");

// could be run from chain's VM only, see paths to keystores which this script requires
const addressArray = [
    "0x367103555b34Eb9a46D92833e7293D540bFd7143", 
    "0x1a75262751ac4E6290Ec8287d1De823F33036498",
    "0xd9df253d1f92dcba495c8b0a9616e9d817bb12b7",
    "0x46aF655eB60F0a863663Fc5D7D5fb11EC8808a51",
    "0x5136C3b985293C10A6882cE9DB4f5719c7C486a3"
];
const passwordArray = ["swapd1", "swapd2", "swapd3", "swapd4", "swapd5"];
const datadirArray = [
    "/home/gelassen/Workspace/Personal/Android/swap/blockchain/private-chain/node-bootnode",
    "/home/gelassen/Workspace/Personal/Android/swap/blockchain/private-chain/node-geth-rpc-endpoint",
    "/home/gelassen/Workspace/Personal/Android/swap/blockchain/private-chain/node-miner",
    "/home/gelassen/Workspace/Personal/Android/swap/blockchain/private-chain/node-miner",
    "/home/gelassen/Workspace/Personal/Android/swap/blockchain/private-chain/node-miner"
]

for (let idx = 0; idx < addressArray.length; idx++) {
    printKey(addressArray[idx], passwordArray[idx], datadirArray[idx]);
}

function printKey(address, password, datadir) {
    var keyObject = keythereum.importFromFile(address, datadir);
    var privateKey = keythereum.recover(password, keyObject);

    console.log(`Private key for address ${address} is ${privateKey.toString('hex')}`);
}
