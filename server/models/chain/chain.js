const { ethers } = require("ethers");

class Contract {
    constructor() {}

    getWalletWithProvider(privateKey, nodeUrl) {
        let provider = new ethers.providers.JsonRpcProvider(nodeUrl)
        let walletWithProvider = new ethers.Wallet(privateKey, provider);
        return walletWithProvider;
    }
}

class SwapToken extends Contract {
    constructor(privateKey, nodeUrl, contractAddress) {
        super();
        console.log(`${JSON.stringify(privateKey)}, ${JSON.stringify(nodeUrl)}, ${JSON.stringify(contractAddress)}`)
        let swapTokenAbiJson = [
            "function balanceOf(address owner) public view virtual override returns (uint256)",
        ];
        console.log("[debug] 1.")
        this.signerInstance = this.getWalletWithProvider(privateKey, nodeUrl);
        console.log("[debug] 2.")
        this.swapTokenContract = new ethers.Contract(
            contractAddress, 
            swapTokenAbiJson, 
            this.signerInstance
        );
        console.log("[debug] 3.")
    }

    async balanceOf(address) {
        console.log("[debug] 4.")
        return await this.swapTokenContract.balanceOf(address);
    }
}

class SwapChainV2 extends Contract {

    constructor(privateKey, nodeUrl, contractAddress) {
        let swapChainAbiJson = [
            "function registerUser(address user) public override",
        ];
        this.signerInstance = getWalletWithProvider(privateKey, nodeUrl);
        this.swapChainContract = new ethers.Contract(
            contractAddress, 
            swapChainAbiJson, 
            this.signerInstance
        );
        
    }
}

module.exports = { SwapToken, SwapChainV2 }