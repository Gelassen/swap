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
        this.signerInstance = this.getWalletWithProvider(privateKey, nodeUrl);
        this.swapTokenContract = new ethers.Contract(
            contractAddress, 
            swapTokenAbiJson, 
            this.signerInstance
        );
    }

    async balanceOf(address) {
        return await this.swapTokenContract.balanceOf(address);
    }
}

class SwapChainV2 extends Contract {

    constructor(privateKey, nodeUrl, contractAddress) {
        super();
        let swapChainAbiJson = [
            "function registerUser(address user) public override",
            "function getUsers() public view override returns (address[] memory)",
            "function getMatches(address userFirst, address userSecond) external view override returns ((address,uint,address,uint,bool,bool)[])"
        ];
        this.signerInstance = this.getWalletWithProvider(privateKey, nodeUrl);
        this.swapChainContract = new ethers.Contract(
            contractAddress, 
            swapChainAbiJson, 
            this.signerInstance
        );
        
    }

    async getMatches(userFirstAddress, userSecondAddress) {
        return await this.swapChainContract.getMatches(userFirstAddress, userSecondAddress);
    }

    async getUsers() {
        return await this.swapChainContract.getUsers();
    }
}

module.exports = { SwapToken, SwapChainV2 }