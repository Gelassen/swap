import { expect } from "chai";
import { ethers } from "hardhat";
import { utils, BigNumber } from "ethers";
import { time, loadFixture } from "@nomicfoundation/hardhat-network-helpers";

describe.only("SwapChain V2 test", async function () {
    async function deploy() {
        const factoryUtils = await ethers.getContractFactory("Utils");
        const contractUtils = await factoryUtils.deploy();

        const factorySwapValue = await ethers.getContractFactory("SwapValue");
        const contractValue = await factorySwapValue.deploy();

        const factory = await ethers.getContractFactory("SwapChainV2");
        const contractSwapChain = await factory.deploy(
            contractUtils.address, 
            contractValue.address
        );

        const accounts = await ethers.getSigners();

        return { contractSwapChain, contractValue, contractUtils }
    }

    it("On register new user, get users call returns relevant count", async function() {
        const { contractSwapChain } = await loadFixture(deploy);
        const usersLengthStart = await contractSwapChain.getUsers();
        expect(usersLengthStart.length).to.be.equal(0);
        const user = "0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd";

        await contractSwapChain.registerUser(user);

        const usersLengthEnd = await contractSwapChain.getUsers();
        expect(usersLengthEnd.length).to.be.equal(1);
    });

    it("On approve swap for caller which is not presented in match object would lead to return tx", async function() {
        const { contractSwapChain } = await loadFixture(deploy);
        const accounts = await ethers.getSigners();
        const firstUser = accounts[0];
        const anotherUser = accounts[1];
        const unknownUser = accounts[2];   
        
        const matchObj = {
            _userFirst : firstUser.address,
            _valueOfFirstUser : 0,
            _userSecond : anotherUser.address,
            _valueOfSecondUser : 1,
            _approvedByFirstUser : false,
            _approvedBySecondUser : false
        }
        await expect(contractSwapChain.connect(unknownUser).approveSwap(matchObj)).to.be.revertedWith("Caller should be an one of the users defined in the passed match object.");
    });

    it("On approve swap for both users who do not exist would lead to reverted tx", async function() {
        const accounts = await ethers.getSigners();
        const firstUser = accounts[0];
        const anotherUser = accounts[1];
        const { contractSwapChain } = await loadFixture(deploy);
        const usersLengthStart = await contractSwapChain.getUsers();
        expect(usersLengthStart.length).to.be.equal(0);

        const matchObj = {
            _userFirst : firstUser.address,
            _valueOfFirstUser : 0,
            _userSecond : anotherUser.address,
            _valueOfSecondUser : 1,
            _approvedByFirstUser : false,
            _approvedBySecondUser : false
        }
        await expect(contractSwapChain.connect(firstUser).approveSwap(matchObj)).to.be.revertedWith("The first user should be registered.");
    });

    it("On approve swap when only one of the users is registered would lead to reverted tx", async function() {
        const accounts = await ethers.getSigners();
        const firstUser = accounts[0];
        const anotherUser = accounts[1];
        const { contractSwapChain } = await loadFixture(deploy);
        await contractSwapChain.registerUser(firstUser.address);

        const matchObj = {
            _userFirst : firstUser.address,
            _valueOfFirstUser : 0,
            _userSecond : anotherUser.address,
            _valueOfSecondUser : 1,
            _approvedByFirstUser : false,
            _approvedBySecondUser : false
        }
        await expect(contractSwapChain.connect(firstUser).approveSwap(matchObj)).to.be.revertedWith("The second user should be registered.");
    });

    it("On approve swap when both users are registered, but no token is valid would lead to reverted tx", async function() {
        const accounts = await ethers.getSigners();
        const firstUser = accounts[0];
        const anotherUser = accounts[1];
        const { contractSwapChain } = await loadFixture(deploy);
        await contractSwapChain.registerUser(firstUser.address);
        await contractSwapChain.registerUser(anotherUser.address);

        const matchObj = {
            _userFirst : firstUser.address,
            _valueOfFirstUser : 0,
            _userSecond : anotherUser.address,
            _valueOfSecondUser : 1,
            _approvedByFirstUser : false,
            _approvedBySecondUser : false
        }
        await expect(contractSwapChain.connect(firstUser).approveSwap(matchObj)).to.be.revertedWith("This token does not exist.");
    });

    it("On approve swap when both users are registered and has tokens, but their date is not valid would lead to reverted tx.", async function() {
        const accounts = await ethers.getSigners();
        const firstUser = accounts[0];
        const anotherUser = accounts[1];
        const { contractSwapChain, contractValue } = await loadFixture(deploy);
        await contractSwapChain.registerUser(firstUser.address);
        await contractSwapChain.registerUser(anotherUser.address);
        const firstUserCustomMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contractValue.safeMint(firstUser.address, firstUserCustomMetadata, "https://gelassen.github.io/blog/");
        const anotherUserCustomMetadata = {
            _offer : "Farmer's products.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000
        }
        await contractValue.safeMint(anotherUser.address, anotherUserCustomMetadata, "https://gelassen.github.io/blog/2021/08/20/case-study-pharmacy-and-farmer-growing-sales.html");
        const firstUserToken = (await contractValue.getTokensIdsForUser(firstUser.address))[0];
        const anotherUserToken = (await contractValue.getTokensIdsForUser(anotherUser.address))[0];

        const matchObj = {
            _userFirst : firstUser.address,
            _valueOfFirstUser : firstUserToken,
            _userSecond : anotherUser.address,
            _valueOfSecondUser : anotherUserToken,
            _approvedByFirstUser : false,
            _approvedBySecondUser : false
        }
        await expect(contractSwapChain.connect(firstUser).approveSwap(matchObj)).to.be.revertedWith("One or both tokens has been expired.");
    });

    // 1702539434000
    it("On approve swap with valid tokens call tx is not reverted", async function() {
        const accounts = await ethers.getSigners();
        const firstUser = accounts[0];
        const anotherUser = accounts[1];
        const { contractSwapChain, contractValue } = await loadFixture(deploy);
        await contractSwapChain.registerUser(firstUser.address);
        await contractSwapChain.registerUser(anotherUser.address);
        const firstUserCustomMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 1671014555000,
            _availabilityEnd: 1702539434000,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contractValue.safeMint(firstUser.address, firstUserCustomMetadata, "https://gelassen.github.io/blog/");
        const anotherUserCustomMetadata = {
            _offer : "Farmer's products.", 
            _availableSince: 1671014555000,
            _availabilityEnd: 1702539434000,
            _isConsumed: false,
            _lockedUntil: 1000
        }
        await contractValue.safeMint(anotherUser.address, anotherUserCustomMetadata, "https://gelassen.github.io/blog/2021/08/20/case-study-pharmacy-and-farmer-growing-sales.html");
        const firstUserToken = (await contractValue.getTokensIdsForUser(firstUser.address))[0];
        const anotherUserToken = (await contractValue.getTokensIdsForUser(anotherUser.address))[0];

        const matchObj = {
            _userFirst : firstUser.address,
            _valueOfFirstUser : firstUserToken,
            _userSecond : anotherUser.address,
            _valueOfSecondUser : anotherUserToken,
            _approvedByFirstUser : false,
            _approvedBySecondUser : false
        }
        expect(contractSwapChain.connect(firstUser).approveSwap(matchObj)).not.to.be.reverted;
    })

})