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
  }
  
};

export default config;
