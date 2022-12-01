import { HardhatUserConfig } from "hardhat/config";
import "@nomicfoundation/hardhat-toolbox";
const configChain = require('config');

const config: HardhatUserConfig = {
  solidity: "0.8.9",
  networks: {
    hardhat: {
      allowUnlimitedContractSize: true,
    },
    goerli: {
      url: configChain.get("goerly_testnet").goerlyApi,
      accounts: [`0x${configChain.get("wallet").privateKey}`],
      chainId: 5,
    },
    private_testnet: {
      url: configChain.get("private_testnet").api,
      accounts: [`0x${configChain.get("private_testnet").privateKey}`],
      chainId: 3347
    }
  }
  
};

export default config;
