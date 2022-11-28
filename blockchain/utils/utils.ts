import { ethers } from "hardhat"

// import { keythereum } from "keythereum"
var keythereum = require("keythereum")

class Utils {

    recoverPasswordForNode(password: string, address: string, dataDir: string) {
        var keyObject = keythereum.importFromFile(address, dataDir)
        var privateKey = keythereum.recover(password, keyObject)
        return privateKey
    }

}

export { Utils } 