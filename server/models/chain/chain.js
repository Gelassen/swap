const { ethers } = require("ethers");

class SwapToken {
    constructor() {
        let privateKey = "0x96c1d1d177c07b55c04a12d5e6a7c0b029d23ed5619c081f312ac5fb86c919f9";
        let url = "http://192.168.1.243:2001"
        let provider = new ethers.providers.JsonRpcProvider(url)
        let walletWithProvider = new ethers.Wallet(privateKey, provider);
        
        this.signerInstance = walletWithProvider;
        this.swapTokenAddress = "0xEb1aA6da9c4598AE28b793331a9B711c01670A7e";
        this.swapTokenAbiJson = [
            "function balanceOf(address owner) public view virtual override returns (uint256)",
        ];
        this.swapTokenContract = new ethers.Contract(
            this.swapTokenAddress, 
            this.swapTokenAbiJson, 
            this.signerInstance
        );
    }

    async balanceOf(address) {
        return await this.swapTokenContract.balanceOf(address);
    }
}

class SwapChainV2 {
    constructor() {
        this.swapChainAddress = "0xF8C91Ac48e437d3e56BFBFFECe2d0D4663f37e6F";
        this.swapChainAbiJson = [
            "function registerUser(address user) public override",
        ];
        this.swapChainContract = new ethers.Contract(
            this.swapTokenAddress, 
            this.swapTokenAbiJson, 
            this.signerInstance
        );
    }
}

module.exports = { SwapToken, SwapChainV2 }