const keythereum = require("keythereum");

// could be run from chain's VM only, see paths to keystores which this script requires
const addressArray = ["0x0A8b295B4266d8fEB55d46a96B31936FE265C01F", "0xED337a9841aa5349ACe99931460E0443a199E746", "0xbd7E8349308ab58AE3BFe18a057240a06B133b31"];
const passwordArray = ["node1", "node2", "node3"];
const datadirArray = [
    "/home/ubuntu/Workspace/private-chain/swap-chain-2/node1/data",
    "/home/ubuntu/Workspace/private-chain/swap-chain-2/node2/data",
    "/home/ubuntu/Workspace/private-chain/swap-chain-2/node3/data"
]

for (let idx = 0; idx < addressArray.length; idx++) {
    printKey(addressArray[idx], passwordArray[idx], datadirArray[idx]);
}

function printKey(address, password, datadir) {
    var keyObject = keythereum.importFromFile(address, datadir);
    var privateKey = keythereum.recover(password, keyObject);

    console.log(`Private key for address ${address} is ${privateKey.toString('hex')}`);
}
