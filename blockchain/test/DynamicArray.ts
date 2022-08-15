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

        await expect((await deployResult.asPlainArray(false)).length).to.be.equal(20);
    });

    it("Call drop empty slots", async function() {
        const { deployResult } = await loadFixture(deploy);

        await expect((await deployResult.dropEmptySlots())).not.to.be.reverted;
    });

    it("Call asPlainArray() with drop empty slots flag return zero length array", async function() {
        const { deployResult } = await loadFixture(deploy);

        await expect((await deployResult.asPlainArray(true))).not.to.be.reverted;
        await expect((await deployResult.asPlainArray(true)).length).to.be.equal(0)
    });
});