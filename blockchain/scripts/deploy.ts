import { ethers } from "hardhat";

async function main() {
  // const factoryMatchDynamicArray = await ethers.getContractFactory("MatchDynamicArray");
  // const contractMatchDynamicArray = await factoryMatchDynamicArray.deploy();

  // const factoryUtils = await ethers.getContractFactory("Utils");
  // const contractUtils = await factoryUtils.deploy();
  // contractUtils.deployed()

  const factoryValue = await ethers.getContractFactory("SwapValue");
  const contractValue = await factoryValue.deploy();

  // const factory = await ethers.getContractFactory("SwapChain");
  // const contract = await factory.deploy(
  //     contractUtils.address, 
  //     contractValue.address,
  //     contractMatchDynamicArray.address
  // );
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
