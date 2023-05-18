const keythereum = require("keythereum");

// could be run from chain's VM only, see paths to keystores which this script requires
const addressArray = ["0x1a75262751ac4E6290Ec8287d1De823F33036498"];
const passwordArray = ["swapd2"];
const datadirArray = [
    // "../../infrastructure/chain/accounts"
    "/home/gelassen/Workspace/Personal/Android/swap/infrastructure/chain/accounts"
    // "/home/ubuntu/Workspace/private-chain/swap-chain-2/node1/data",
    // "/home/ubuntu/Workspace/private-chain/swap-chain-2/node2/data",
    // "/home/ubuntu/Workspace/private-chain/swap-chain-2/node3/data"
]

for (let idx = 0; idx < addressArray.length; idx++) {
    printKey(addressArray[idx], passwordArray[idx], datadirArray[idx]);
}

function printKey(address, password, datadir) {
    var keyObject = keythereum.importFromFile(address, datadir);
    var privateKey = keythereum.recover(password, keyObject);

    console.log(`Private key for address ${address} is ${privateKey.toString('hex')}`);
}
