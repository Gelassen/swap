import { ethers } from "hardhat";
import { Utils } from "../utils/utils"
const configChain = require('config');
const isNodeAccountPwdRecovered = true;

async function main() {
  if (isNodeAccountPwdRecovered) {
    const factoryUtils = await ethers.getContractFactory("Utils");
    const contractUtils = await factoryUtils.deploy();
    console.log("contractUtils address: ", contractUtils.address);
  
    const factoryValue = await ethers.getContractFactory("SwapValue");
    const contractValue = await factoryValue.deploy();
    console.log("SwapValueContract adddress: ", contractValue.address);

    // estimate gas cost for deployment
    const factorySwapChain = await ethers.getContractFactory("SwapChainV2");
    const gasPrice = await factorySwapChain.signer.getGasPrice();
    const estimatedGas = await factorySwapChain.signer.estimateGas(
      factorySwapChain.getDeployTransaction(
        contractUtils.address, 
        contractValue.address
      )
    )
    const deployerBalance = await factorySwapChain.signer.getBalance();
    const deploymentPrice = estimatedGas.mul(gasPrice);
    console.log(`Deployer balance: ${ethers.utils.formatEther(deployerBalance)}`);
    console.log(`Deployment price: ${ethers.utils.formatEther(deploymentPrice)}`);

    const contract = await factorySwapChain.deploy(
        contractUtils.address, 
        contractValue.address
    );
    console.log("SwapChainV2Contract address: ", contract.address)

  } else {
    // since 18th of April 2023, a separate script is used for this aim; see scripts/keys.js
    const nodeConfig = configChain.get("private_testnet");
    const pwd = new Utils().recoverPasswordForNode(
      nodeConfig.password, 
      nodeConfig.address, 
      nodeConfig.dataDir
      );
    console.log("Provide this password to hardhat private network config: ", pwd);
  }
}

// We recommend this pattern to be able to use async/await everywhere
// and properly handle errors.
// main().catch((error) => {
//   console.error(error);
//   process.exitCode = 1;
// });
main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exitCode = 1;
  });
