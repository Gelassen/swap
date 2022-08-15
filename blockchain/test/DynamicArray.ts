import { expect } from "chai";
import { ethers } from "hardhat";
import { time, loadFixture } from "@nomicfoundation/hardhat-network-helpers";

describe("DynamicArray", function () {
    async function deploy() {
        const contract = await ethers.getContractFactory("DynamicArray");
        const deployResult = await contract.deploy();
        return { deployResult };
    }

    it.skip("Just test a result from contract deployment", async function() {
        const { deployResult } = await loadFixture(deploy);
        console.log("Test message");
        console.log(`deployResult ${JSON.stringify(deployResult)}`);
        return expect(deployResult).to.equal("Test message");
    });

    it("Call asPlainArray() without drop empty slots flag return default length array", async function() {
        const { deployResult } = await loadFixture(deploy);

        await expect((await deployResult.asPlainArray(false)).length).to.be.equal(0);
    });

    it.skip("Call drop empty slots", async function() {
        const { deployResult } = await loadFixture(deploy);

        await expect((await deployResult.dropEmptySlots())).not.to.be.reverted;
    });

    it("Call asPlainArray() with drop empty slots flag return zero length array", async function() {
        const { deployResult } = await loadFixture(deploy);

        await expect((await deployResult.asPlainArray(true))).not.to.be.reverted;
        await expect((await deployResult.asPlainArray(true)).length).to.be.equal(0);
    });

    it("Call push() when array is empty leads to one item in array", async function() {
        const { deployResult } = await loadFixture(deploy);
        const someValue = "0x0000000000000000000000000000000000000042";

        await deployResult.push(someValue);
    
        await expect((await deployResult.asPlainArray(true)).length).to.be.equal(1);
        await expect((await deployResult.asPlainArray(true))[0]).to.be.equal(someValue);
    });

    it("Call push() when array reach default size will add extra element", async function() {
        const { deployResult } = await loadFixture(deploy);
        const someValue = "0x0000000000000000000000000000000000000042";
        await expect((await deployResult.asPlainArray(true)).length).to.be.equal(0);

        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
        await deployResult.push(someValue);
    
        await expect((await deployResult.asPlainArray(true)).length).to.be.equal(21);
    })

    it.skip("Call push() when array is empty leads to 21 item in array if empty slots hasn't been droped", async function() {
        const { deployResult } = await loadFixture(deploy);
        const someValue = "0x0000000000000000000000000000000000000042";

        await deployResult.push(someValue);
    
        await expect((await deployResult.asPlainArray(false)).length).to.be.equal(21);
        await expect((await deployResult.asPlainArray(false))[20]).to.be.equal(someValue);
    });

    it("Call removeByIndex() when array is empty leads to reverted tx", async function() {
        const { deployResult } = await loadFixture(deploy);

        await expect(deployResult.removeByIndex(0)).to.be.revertedWith("You can not remove any value from the empty array.");
    });

    it("Call removeByIndex() for array with single item", async function() {
        const { deployResult } = await loadFixture(deploy);
        const someValue = "0x0000000000000000000000000000000000000042";
        await deployResult.push(someValue);

        await deployResult.removeByIndex(0);

        await expect((await deployResult.asPlainArray(false)).length).to.be.equal(0);
    });

    it("Call removeByIndex() for index that doesn't exist leads to reverted tx", async function() {
        const { deployResult } = await loadFixture(deploy);
        const someValue = "0x0000000000000000000000000000000000000042";
        await deployResult.push(someValue);

        await expect(deployResult.removeByIndex(10)).to.be.revertedWith("You can not remove item with index out of this array range.");
    });

    it("Call removeByIndex() for existing data leads to removing this item drom array and array length becreases by one", async function() {
        const { deployResult } = await loadFixture(deploy);
        const someValue42 = "0x0000000000000000000000000000000000000042";
        const someValue43 = "0x0000000000000000000000000000000000000043";
        const someValue44 = "0x0000000000000000000000000000000000000044";
        await deployResult.push(someValue42);
        await deployResult.push(someValue43);
        await deployResult.push(someValue44);
        await expect((await deployResult.asPlainArray(false)).length).to.be.equal(3);

        await deployResult.removeByIndex(1);

        await expect((await deployResult.asPlainArray(false)).length).to.be.equal(2);
        await expect((await deployResult.asPlainArray(false))[0]).to.be.equal(someValue42);
        await expect((await deployResult.asPlainArray(false))[1]).to.be.equal(someValue44);
        await expect(deployResult.removeByIndex(2)).to.be.revertedWith("You can not remove item with index out of this array range.");
    });

    it("Call get() on empty array lead to reverted tx.", async function() {
        const { deployResult } = await loadFixture(deploy);

        await expect(deployResult.get(0)).to.be.revertedWith("Index is out of array range.");
    });

    it("Call get() on non empty array with not relevant index lead to reverted tx.", async function() {
        const { deployResult } = await loadFixture(deploy);
        const someValue = "0x0000000000000000000000000000000000000042";
        await deployResult.push(someValue);

        await expect(deployResult.get(1)).to.be.revertedWith("Index is out of array range.");
    });

    it("Call get() on non empty array with relevant index returns correct value", async function() {
        const { deployResult } = await loadFixture(deploy);
        const someValue42 = "0x0000000000000000000000000000000000000042";
        const someValue43 = "0x0000000000000000000000000000000000000043";
        const someValue44 = "0x0000000000000000000000000000000000000044";
        const someValue45 = "0x0000000000000000000000000000000000000045";
        await deployResult.push(someValue42);
        await deployResult.push(someValue43);
        await deployResult.push(someValue44);
        await deployResult.push(someValue45);

        await expect(await deployResult.get(2)).to.be.equal(someValue44);     
    })

});