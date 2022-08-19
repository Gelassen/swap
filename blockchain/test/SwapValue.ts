import { ethers } from "hardhat";
// import "../contracts/SwapValue.sol";
import { expect } from "chai";
import { contracts } from "../typechain-types";
import { loadFixture } from "@nomicfoundation/hardhat-network-helpers";

describe.only("Swap value suite", async function() {

    async function deploy() {
        const factory = await ethers.getContractFactory("SwapValue");
        const contract = await factory.deploy();
        return { contract };
    }

    it("On ethers.getSigners() receives accounts", async function() {
        const accounts = await ethers.getSigners();

        expect(accounts[0]).to.be.equal(0);
        expect(accounts[1]).to.be.equal(1);
        expect(accounts[2]).to.be.equal(2);
    });

    it("On emit new tokens balance of user changes", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const user1 = accounts[1].address;
        const { contract } = await loadFixture(deploy);
        await expect(await contract.balanceOf(owner)).to.be.equal(0);

        // {value: ethers.utils.parseEther("0.123") }
        await contract.safeMint(owner, "https://gelassen.github.io/blog/");

        await expect(await contract.balanceOf(owner)).to.be.equal(1);
    });

    it("On mint new token with custom content token with new content is created and stored", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const user1 = accounts[1].address;
        const { contract } = await loadFixture(deploy);
        await expect(await contract.balanceOf(owner)).to.be.equal(0);
    })

})