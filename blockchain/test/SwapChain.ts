import { expect } from "chai";
import { ethers } from "hardhat";
import { utils, BigNumber } from "ethers";
import { time, loadFixture } from "@nomicfoundation/hardhat-network-helpers";

/**
 * NOTE: you can receive value for functions which do not change state. 
 * For the rest you will receive tx. 
 * 
 */
describe.only("SwapChain test", async function() {
    async function deploy() {
        const factoryUtils = await ethers.getContractFactory("Utils");
        const contractUtils = await factoryUtils.deploy();

        const factory = await ethers.getContractFactory("SwapChain");
        const contract = await factory.deploy(contractUtils.address);
        
        return { contract, contractUtils };
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

});