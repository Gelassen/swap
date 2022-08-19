import { expect } from "chai";
import { ethers } from "hardhat";
import { utils, BigNumber } from "ethers";
import { time, loadFixture } from "@nomicfoundation/hardhat-network-helpers";

/**
 * NOTE: you can receive value for functions which do not change state. 
 * For the rest you will receive tx. 
 * 
 */
describe("SwapChain test", async function() {
    async function deploy() {
        const factoryUtils = await ethers.getContractFactory("Utils");
        const contractUtils = await factoryUtils.deploy();

        const factory = await ethers.getContractFactory("SwapChain");
        const contract = await factory.deploy(contractUtils.address);

        const factoryValue = await ethers.getContractFactory("SwapValue");
        const contractValue = await factoryValue.deploy();
        
        return { contract, contractUtils, contractValue };
    }

    it("On registerUser() the next call of usersInTotal() will return increased number", async function() {
        const { contract } = await loadFixture(deploy);
        const someUser = "0x0000000000000000000000000000000000000042";
        const previousUsersAmount = await contract.usersInTotal();

        await contract.registerUser(someUser);

        expect(await contract.usersInTotal()).not.to.be.equal(previousUsersAmount);
        expect(await contract.usersInTotal()).to.be.equal(previousUsersAmount.add(1), "Call registerUser() is not successful, but it should be.");
    });

    it("On registerUser() with the same user twice would lead to revrted tx", async function() {
        const { contract } = await loadFixture(deploy);

        const someUser = "0x0000000000000000000000000000000000000042";
        await contract.registerUser(someUser);

        await expect(contract.registerUser(someUser)).to.be.revertedWith("User should not be registered yet.");
    });

    it("On users in total at the initial state return zero", async function() {
        const { contract } = await loadFixture(deploy);

        await expect(await contract.usersInTotal()).to.be.equal(0);
    });

    it("On users in total after two users have been registered", async function() {
        const { contract } = await loadFixture(deploy);
        const someUser = "0x0000000000000000000000000000000000000042";
        await contract.registerUser(someUser);
        const anotherUser = "0x0000000000000000000000000000000000000043";
        await contract.registerUser(anotherUser);

        await expect(await contract.usersInTotal()).to.be.equal(2)
    });

    it("On registerDemand() next call on getDemandsByUsers() will return increased value", async function() {
        const { contract, contractUtils } = await loadFixture(deploy);
        const someUser = "0x0000000000000000000000000000000000000042";
        const oldDemands = await contract.getDemandsByUser(someUser);
        await contract.registerUser(someUser);

        await contract.registerDemand(someUser, "Farmer products");
        const newDemands = await contract.getDemandsByUser(someUser);

        await expect(newDemands.length).to.be.equal(oldDemands.length + 1);
    });

    it("On registerDemand() twice with the same input tx is reverted", async function() {
        const { contract, contractUtils } = await loadFixture(deploy);
        const someUser = "0x0000000000000000000000000000000000000042";
        const oldDemands = await contract.getDemandsByUser(someUser);
        
        await contract.registerDemand(someUser, "Farmer products");
        
        await expect(contract.registerDemand(someUser, "Farmer products")).to.be.revertedWith("This pair <user, demand> already exist.");
    });

    it("On reigsterDemand() with several users and several items changes state correctly", async function() {
        const { contract, contractUtils } = await loadFixture(deploy);
        const someUser = "0x0000000000000000000000000000000000000042";
        const anotherUser = "0x0000000000000000000000000000000000000043";

        await contract.registerDemand(someUser, "Farmer products");
        await contract.registerDemand(anotherUser, "Software Development");
        await contract.registerDemand(anotherUser, "Gasoline");

        await expect((await contract.getDemandsByUser(someUser)).length).to.be.equal(1);
        await expect((await contract.getDemandsByUser(anotherUser)).length).to.be.equal(2);
    });

    it.skip("On mint new token receive address of just created token", async function() {
        /**
         * There is an adress of the ERC721 contract, there is no address of just minted token. 
         * We can get array of token id per address if it is necessary.  
         */
    });

    it("On mint new token balance of user increases by one", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const { contractValue } = await loadFixture(deploy);
        await expect(await contractValue.balanceOf(owner)).to.be.equal(0);

        // {value: ethers.utils.parseEther("0.123") }
        const customMetadata = { offer : "Software development for Android."};
        await contractValue.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");

        await expect(await contractValue.balanceOf(owner)).to.be.equal(1);
    });

    it("On mint new token with custom content token with new content is created and stored", async function() {

    });

    it.skip("On showMatches() for existing user and value, return result", async function() {
        const { contract, contractUtils } = await loadFixture(deploy);
        const firstUser = "0x0000000000000000000000000000000000000042";
        // const firstValue = // TODO address of minted token
        const secondUser = "0x0000000000000000000000000000000000000043";

        const matches = await contract.showMatches(firstUser);
    });
});