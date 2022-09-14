import { expect } from "chai";
import { ethers } from "hardhat";
import { BigNumber } from "ethers";
import { time, loadFixture } from "@nomicfoundation/hardhat-network-helpers";

describe("MatchDynamicArray", function () {
    async function deploy() {
        const factory = await ethers.getContractFactory("MatchDynamicArray");
        const contract = await factory.deploy();
        return { contract };
    }

    it("On call getByIndex() on empty array will revert tx with msg", async function() {
        const { contract } = await loadFixture(deploy);

        await expect(contract.getByIndex(0)).to.be.revertedWith("You can not request data when array is empty.");
    });

    it("On call getByIndex() with index out of array bounds will revert tx with msg", async function() {
        const { contract } = await loadFixture(deploy);

        const matchObj = {
            _userFirst: "0x0000000000000000000000000000000000000042", 
            _valueOfFirstUser: 0, 
            _userSecond: "0x0000000000000000000000000000000000000043", 
            _valueOfSecondUser: 1,
            _lockedUntil: 0,
            _approvedByFirstUser: false,
            _approvedBySecondUser: false
        };
        await contract.push(matchObj);

        await expect(contract.getByIndex(1)).to.be.revertedWith("You can not request data for out of array bounds index."); 
    });

    it("On push() call array length increases by one", async function() {
        const { contract } = await loadFixture(deploy);
        const lengthBeforePush = await contract.getLength();

        const matchObj = {
            _userFirst: "0x0000000000000000000000000000000000000042", 
            _valueOfFirstUser: 0, 
            _userSecond: "0x0000000000000000000000000000000000000043", 
            _valueOfSecondUser: 1,
            _lockedUntil: 0,
            _approvedByFirstUser: false,
            _approvedBySecondUser: false 
        };
        await contract.push(matchObj);

        await expect(await contract.getLength()).to.be.equal(lengthBeforePush.add(1));
    });

    it("On push() call twice array length increases by two", async function() {
        const { contract } = await loadFixture(deploy);
        const lengthBeforePush = await contract.getLength();

        const matchObj = {
            _userFirst: "0x0000000000000000000000000000000000000042", 
            _valueOfFirstUser: 0, 
            _userSecond: "0x0000000000000000000000000000000000000043", 
            _valueOfSecondUser: 1,
            _lockedUntil: 0,
            _approvedByFirstUser: false,
            _approvedBySecondUser: false
        };
        await contract.push(matchObj);
        await contract.push(matchObj);

        await expect(await contract.getLength()).to.be.equal(lengthBeforePush.add(2));
    });

    it("On call clear() with non-empty array state array length becomes zero", async function() {
        const { contract } = await loadFixture(deploy);

        await contract.clear();

        await expect(await contract.getLength()).to.be.equal(0);
    });

    it("On call clear() with empty aray state array length becomes zero", async function() {
        const { contract } = await loadFixture(deploy);
        const matchObj = {
            _userFirst: "0x0000000000000000000000000000000000000042", 
            _valueOfFirstUser: 0, 
            _userSecond: "0x0000000000000000000000000000000000000043", 
            _valueOfSecondUser: 1,
            _lockedUntil: 0,
            _approvedByFirstUser: false,
            _approvedBySecondUser: false
        };
        await contract.push(matchObj);
        await contract.push(matchObj);

        await contract.clear();

        await expect(await contract.getLength()).to.be.equal(0);
    });

    it("On call getArray() returns array", async function() {
        const { contract } = await loadFixture(deploy);
        const matchObj = {
            _userFirst: "0x0000000000000000000000000000000000000042", 
            _valueOfFirstUser: 0, 
            _userSecond: "0x0000000000000000000000000000000000000043", 
            _valueOfSecondUser: 1,
            _lockedUntil: 0,
            _approvedByFirstUser: false,
            _approvedBySecondUser: false 
        };
        await contract.push(matchObj);
        await contract.push(matchObj);

        const array = await contract.getArray();

        expect(array.length).to.be.equal(2);
    });

    it("On call clear() array copy which has been returned over getArray() call remains the same", async function() {
        const { contract } = await loadFixture(deploy);
        const matchObj = {
            _userFirst: "0x0000000000000000000000000000000000000042", 
            _valueOfFirstUser: 0, 
            _userSecond: "0x0000000000000000000000000000000000000043", 
            _valueOfSecondUser: 1,
            _lockedUntil: 0,
            _approvedByFirstUser: false,
            _approvedBySecondUser: false 
        };
        await contract.push(matchObj);
        await contract.push(matchObj);

        const array = await contract.getArray();
        await contract.clear();

        expect(array.length).to.be.equal(2);
    });
})