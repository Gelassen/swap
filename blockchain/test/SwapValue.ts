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

        console.log(JSON.stringify(accounts));
        expect(accounts[0].address).to.be.equal("0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266");
        expect(accounts[1].address).to.be.equal("0x70997970C51812dc3A010C7d01b50e0d17dc79C8");
        expect(accounts[2].address).to.be.equal("0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC");
    });

    it("On emit new tokens balance of user changes", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const user1 = accounts[1].address;
        const { contract } = await loadFixture(deploy);
        await expect(await contract.balanceOf(owner)).to.be.equal(0);

        // {value: ethers.utils.parseEther("0.123") }
        const customMetadata = { offer : "Software development for Android."};
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");

        await expect(await contract.balanceOf(owner)).to.be.equal(1);
    });

    it("On mint new token with custom content token with new content is created and stored", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const user1 = accounts[1].address;
        const { contract } = await loadFixture(deploy);
        await expect(await contract.balanceOf(owner)).to.be.equal(0);
        
        const customMetadata = { "offer" : "Software development for Android."};
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");
    
        console.log(JSON.stringify(await contract.offer(0)));
        console.log(JSON.stringify(customMetadata));
        await expect((await contract.offer(0)).at(0)).to.be.equal(customMetadata.offer);
    });

    it("On queryFilter() when two tokens have been minted receives token ids", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const user1 = accounts[1].address;
        const { contract } = await loadFixture(deploy);
        const customMetadata = { "offer" : "Software development for Android."};
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");

        let eventFilter = contract.filters.Transfer();
        let events = await contract.queryFilter(eventFilter)

        console.log("Events: ", JSON.stringify(events));
        expect(events[0].args[2]._hex).to.be.equal('0x00');
        expect(events[1].args[2]._hex).to.be.equal('0x01');
        expect(ethers.BigNumber.from(events[0].args[2]._hex)).eq(0);
        expect(ethers.BigNumber.from(events[1].args[2]._hex)).eq(1);
    });

})