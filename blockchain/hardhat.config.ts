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
    private_network: {
      url: configChain.get("private_network").api,
      accounts: [`0x${configChain.get("private_network").privateKey}`],
      chainId: 50101
    },
    private_testnet: {
      url: configChain.get("private_testnet").api,
      accounts: [`0x${configChain.get("private_testnet").privateKey}`],
      chainId: 50102
    },
  }
  
};

export default config;
