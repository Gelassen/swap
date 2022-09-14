import { ethers } from "hardhat";
import { expect } from "chai";
import { contracts } from "../typechain-types";
import { loadFixture } from "@nomicfoundation/hardhat-network-helpers";

describe("Swap value suite", async function() {

    async function deploy() {
        const factory = await ethers.getContractFactory("SwapValue");
        const contract = await factory.deploy();

        const utilsFactory = await ethers.getContractFactory("Utils");
        const contractUtils = await utilsFactory.deploy();
        return { contract, contractUtils };
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
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");

        await expect(await contract.balanceOf(owner)).to.be.equal(1);
    });

    it("On mint new token with custom content token with new content is created and stored", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const user1 = accounts[1].address;
        const { contract } = await loadFixture(deploy);
        await expect(await contract.balanceOf(owner)).to.be.equal(0);
        
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");
    
        console.log(JSON.stringify(await contract.offer(0)));
        console.log(JSON.stringify(customMetadata));
        await expect((await contract.offer(0)).at(0)).to.be.equal(customMetadata._offer);
    });

    it("On queryFilter() when two tokens have been minted receives token ids", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const user1 = accounts[1].address;
        const { contract } = await loadFixture(deploy);
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
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

    it("On mint new token save tokenId per user", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const { contract } = await loadFixture(deploy);
        await expect((await contract.getTokensIdsForUser(owner)).length).to.be.equal(0);
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");
        
        await expect((await contract.getTokensIdsForUser(owner)).length).to.be.equal(1);
    });

    it("On transferFrom(from, to, tokenId) _tokensIdsPerUser changes address of the owner", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const anotherUser = accounts[1].address;
        const { contract } = await loadFixture(deploy);
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");
        const balanceOfAnotherUserBeforeTransfer = await contract.balanceOf(anotherUser); 

        const tokenId = 0; // we have control over scope execution and can use it as const, but the right way is find out this value over events
        await contract.transferFrom(owner, anotherUser, tokenId);

        await expect(await contract.balanceOf(owner)).to.be.equal(0);
        await expect(await contract.balanceOf(anotherUser)).to.be.equal(1);
        expect(balanceOfAnotherUserBeforeTransfer).to.be.equal(0);
        await expect((await contract.getTokensIdsForUser(owner)).length).to.be.equal(0);
        await expect((await contract.getTokensIdsForUser(anotherUser))[0]).to.be.equal(tokenId);
    });

    it("On burn() tokenId with binded metadata should be deleted", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const { contract } = await loadFixture(deploy);
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");
    
        const tokenId = 0;
        await contract.burn(owner, tokenId);

        await expect(await contract.balanceOf(owner)).to.be.equal(0);
        await expect(contract.offer(tokenId)).to.be.revertedWith("This token does not exist.");
        await expect((await contract.getTokensIdsForUser(owner)).length).to.be.equal(0);
    });

    it.skip("Get offer for non existing token id", async function() {
        // WOULDN'T IMPLEMENT: because of require(isExist(tokenId)) has been added, this test case become redundant
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const { contract, contractUtils } = await loadFixture(deploy);

        const result = await contract.offer(0);

        expect(await contractUtils.stringsEquals(JSON.stringify(result), "[\"\"]")).to.be.equal(true);
    });

    it.skip("On safeTransferFrom() change tokensIdsPerUser according to new owner", async function() {
        // WOULDN'T IMPLEMENT: safeTransferFrom() is defined as virtual and not implemented
    });

    it("On mint() new token consumed flag is false", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const { contract } = await loadFixture(deploy);
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");
    
        const offer = await contract.offer(0);

        expect(offer.length).to.be.equal(5); // check size of data stucture
        expect(offer[3]).to.be.equal(false);

    });

    it("On transfer() token's consumed flag is false", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const anotherUser = accounts[1].address;
        const { contract } = await loadFixture(deploy);
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");
        const tokenId = 0;

        await contract.transferFrom(owner, anotherUser, tokenId);
        const offer = await contract.offer(tokenId);

        expect(offer[3]).to.be.equal(true);
    });

    it("On burn() token's consumed flag is not available", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const { contract } = await loadFixture(deploy);
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");

        const tokenId = 0;
        await contract.burn(owner, tokenId);

        await expect(contract.offer(tokenId)).to.be.revertedWith("This token does not exist.");
    });

    it("On makeTokenConsumed(tokenId) call updated token's value to isConsumed = true", async function() { 
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const { contract } = await loadFixture(deploy);
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");

        const tokenId = 0;
        await contract.makeTokenConsumed(tokenId);

        const offer = await contract.offer(tokenId);
        expect(offer[3]).to.be.equal(true);
    });

    it("On makeTokenConsumed(tokenId) call with non-owner or non-approved address leads to reverted tx", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const { contract } = await loadFixture(deploy);
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");

        const tokenId = 0;
        const tx = contract.connect(accounts[1]).makeTokenConsumed(tokenId);
        await expect(tx).to.be.revertedWith("Only token owner or actor approved by owner is allowed to call this function.");
    });

    it("On lockToken(tokenId) call update token's value to isLockedUntil", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const { contract } = await loadFixture(deploy);
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");

        const tokenId = 0;
        const timeOfLockEnds = 42;
        await contract.lockToken(tokenId, timeOfLockEnds);

        const offer = await contract.offer(tokenId);
        expect(offer[4]).to.be.equal(timeOfLockEnds);
    });

    it("On lockToken(tokenId) call from address which is not an owner and is not from an approve list leads to reverted tx", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const anotherUser = accounts[1].address;
        const { contract } = await loadFixture(deploy);
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contract.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/", {from: owner});

        const tokenId = 0;
        const timeOfLockEnds = 42;
        const tx = contract.connect(accounts[1]).lockToken(tokenId, timeOfLockEnds);
        await expect(tx).to.be.revertedWith("Only token owner or actor approved by owner is allowed to call this function.");
    });

    it("On lockToken(tokenId) call from address which is in the approve list executes successfully", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0];
        const anotherUser = accounts[1];
        const arbitrUser = accounts[12];
        const { contract } = await loadFixture(deploy);
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contract.connect(owner).safeMint(owner.address, customMetadata, "https://gelassen.github.io/blog/");

        const tokenId = 0;
        const timeOfLockEnds = 42;
        await contract.approve(arbitrUser.address, tokenId);
        const txSecond = contract.connect(arbitrUser).lockToken(tokenId, timeOfLockEnds);
        await expect(txSecond).not.to.be.revertedWith("Only token owner or actor approved by owner is allowed to call lockToken().");
    });

    it("On transferFrom() call twice by the same address which has been approved before 1st tx leads to tx is reverted as this address neither owner or within approve list", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0];
        const anotherUser = accounts[1];
        const thirdUser = accounts[2];
        const arbitrUser = accounts[12];
        const { contract } = await loadFixture(deploy);
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contract.connect(owner).safeMint(owner.address, customMetadata, "https://gelassen.github.io/blog/");
        const tokenId = 0; // we know it as we control the flow, but it is better to request it from contract
        await contract.connect(owner).approve(arbitrUser.address, tokenId);

        await contract.connect(arbitrUser).transferFrom(owner.address, anotherUser.address, tokenId);
        
        await expect(contract.connect(arbitrUser).transferFrom(owner.address, thirdUser.address, tokenId)).to.be.revertedWith("ERC721: caller is not token owner nor approved");
        await expect(await contract.ownerOf(tokenId)).not.to.be.equal(thirdUser.address);
    });

})