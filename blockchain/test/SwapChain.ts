import { expect } from "chai";
import { ethers } from "hardhat";
import { utils, BigNumber } from "ethers";
import { time, loadFixture } from "@nomicfoundation/hardhat-network-helpers";
// import { User, Match } from "../contracts/StructDeclaration.sol";

/**
 * NOTE: you can receive value for functions which do not change state. 
 * For the rest you will receive tx. 
 * 
 */
describe.skip("SwapChain test", async function() {
    async function deploy() {
        const factoryMatchDynamicArray = await ethers.getContractFactory("MatchDynamicArray");
        const contractMatchDynamicArray = await factoryMatchDynamicArray.deploy();

        const factoryUtils = await ethers.getContractFactory("Utils");
        const contractUtils = await factoryUtils.deploy();

        const factoryValue = await ethers.getContractFactory("SwapValue");
        const contractValue = await factoryValue.deploy();

        const factory = await ethers.getContractFactory("SwapChain");
        const contract = await factory.deploy(
            contractUtils.address, 
            contractValue.address,
            contractMatchDynamicArray.address
        );

        const accounts = await ethers.getSigners();
        
        return { contract, contractUtils, contractValue };
    }

    // TODO do search for matches on server off-chain
    // TODO do search for matches by address on server off-chain
    // TODO pass only match object or array of match objects for swap

    it("Integration test of a whole process from register user to exchange of value in positive scenario", async function() {
        const { contract, contractValue } = await loadFixture(deploy);
        const accounts = await ethers.getSigners();
        const jackAccount = accounts[0];
        const aliceAccount = accounts[1];
        const nonRegisteredUser = accounts[2];
        console.log("Jack account: ", jackAccount.address);
        console.log("Alice account: ", aliceAccount.address);
        // mint new offers
        const jackValue = {
            _offer: "Software Engineering",
            _availableSince: 0,
            _availabilityEnd: 1000,
            _isConsumed: false,
            _lockedUntil: 1000
        }
        const jackUri = "https://gelassen.github.io/blog";
        const aliceValue = {
            _offer: "Farmer Products",
            _availableSince: 0,
            _availabilityEnd: 1000,
            _isConsumed: false,
            _lockedUntil: 1000
        }
        const aliceUri = jackUri;
        await contractValue.safeMint(jackAccount.address, jackValue, jackUri);
        await contractValue.safeMint(aliceAccount.address, aliceValue, aliceUri);
        const tokenIdMintedyJack = 0;
        const tokenIdMintedyAlice = 1;
        // register users (order is important: known issue adds restriction tokens has been minted after user registration are out of index scope)
        await contract.registerUser(jackAccount.address);
        await contract.registerUser(aliceAccount.address);
        // approve the platform contract to operate with user's Swap tokens
        await contractValue.connect(jackAccount).setApprovalForAll(contract.address, true);
        await contractValue.connect(aliceAccount).setApprovalForAll(contract.address, true);
        // register demands
        const jackDemand = "Farmer Products";
        await contract.connect(jackAccount).registerDemand(jackAccount.address, jackDemand);
        const aliceDemand = "Software Engineering";
        await contract.connect(aliceAccount).registerDemand(aliceAccount.address, aliceDemand);
        console.log("Demands are registered");
        // emulate swap() call by schedule
        await contract.swapNoParams(jackAccount.address);
        // emulate call getMatchEvents() by schedule
        // TODO complete me getMatchEvents()
        // confirm swap by both parties
        const matchObj = {
            _userFirst: jackAccount.address,
            _valueOfFirstUser: tokenIdMintedyJack, // tokenId of SwapValue.sol owned by _userFirst
            _userSecond: aliceAccount.address,
            _valueOfSecondUser: tokenIdMintedyAlice, // tokenId of SwapValue.sol owned by _userSecond
            _approvedByFirstUser: false,
            _approvedBySecondUser: false
        }
        await contract.connect(jackAccount).approveSwap(matchObj);
        await contract.connect(aliceAccount).approveSwap(matchObj);
        
        await expect(await contractValue.ownerOf(tokenIdMintedyJack)).to.be.equal(jackAccount.address);
        await expect(await contractValue.ownerOf(tokenIdMintedyAlice)).to.be.equal(aliceAccount.address);
        await expect((await contractValue.offer(tokenIdMintedyJack))._isConsumed).to.be.equal(false);
        await expect((await contractValue.offer(tokenIdMintedyAlice))._isConsumed).to.be.equal(false);
        // emulate swap for all pending matches by schedule
        await contract.connect(nonRegisteredUser).approveSwapNoParams();
        // check result by expect
        await expect(await contractValue.ownerOf(tokenIdMintedyJack)).to.be.equal(aliceAccount.address);
        await expect(await contractValue.ownerOf(tokenIdMintedyAlice)).to.be.equal(jackAccount.address);
        await expect((await contractValue.offer(tokenIdMintedyJack))._isConsumed).to.be.equal(true);
        await expect((await contractValue.offer(tokenIdMintedyAlice))._isConsumed).to.be.equal(true);
        // TODO we have to cleanup data structures: pendingMatches, demandsByUsers, indexNfts;
        // how to check positive or negative case? Query events might do not work as there are 
        // possible duplicates matches and offers with demands 
    });

    it("On users in total at the initial state return zero", async function() {
        const { contract } = await loadFixture(deploy);

        await expect(await contract.usersInTotal()).to.be.equal(0);
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
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contractValue.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");

        await expect(await contractValue.balanceOf(owner)).to.be.equal(1);
    });

    it("On mint new token with custom content token with new content is created and stored", async function() {
        const accounts = await ethers.getSigners();
        const owner = accounts[0].address;
        const { contractValue } = await loadFixture(deploy);

        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contractValue.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");
        const transferEvents = await contractValue.queryFilter(contractValue.filters.Transfer());
        const tokenId = BigNumber.from(transferEvents[0].args[2]._hex);
        const ownerOfNewNft = await contractValue.ownerOf(tokenId);

        expect(ownerOfNewNft).to.be.equals(owner);
    });

    // the requirement becomes redundant by decoupling registerDemand() and swap() logic
    it.skip("On showMatches() for existing user and value, return result", async function() {
        const { contract, contractUtils, contractValue } = await loadFixture(deploy);
        const accounts = await ethers.getSigners();
        const owner = accounts[0];
        const anotherUser = accounts[1];
        const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contractValue.safeMint(owner.address, customMetadata, "https://gelassen.github.io/blog/");
        await contract.registerUser(owner.address);
        const customMetadataOfAnotherUser = { 
            _offer : "Farmer products.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
        await contractValue.safeMint(anotherUser.address, customMetadataOfAnotherUser, "https://dikoed.ru/");
        await contract.registerUser(anotherUser.address);
        // approve swap market as operator on users tokens
        // we know tokens ids because we control the flow, otherwise this values should be obtain some other way
        const ownerTokenId = 0;
        const anotherUserTokenId = 1;
        await contractValue.connect(owner).setApprovalForAll(contract.address, true);
        await contractValue.connect(anotherUser).setApprovalForAll(contract.address, true);
        // register demands
        await contract.connect(owner).registerDemand(owner.address, "Farmer products.");
        await contract.connect(anotherUser).registerDemand(anotherUser.address, "Software development for Android.");

        await contract.showMatches(owner.address);

        // Please keep in mind the difference between on-chain vs off-chain calls
        // off-chain calls would work with tx receipts instead of immediate values
        // except cases when chain state is not changed and function is marked as 'view'
        const transferEvents = await contract.queryFilter(contract.filters.Matches());
        console.log(JSON.stringify(transferEvents));
        const args = transferEvents[2].args;
        const matches = args[0]; 
        console.log("Owner address: " + JSON.stringify(owner));
        console.log("Another user address: " + JSON.stringify(anotherUser));
        expect(matches[0][0]).to.be.equal(anotherUser.address);
        expect(BigNumber.from(matches[0][1]._hex)).to.be.equal(anotherUserTokenId);
        expect(matches[0][2]).to.be.equal(owner.address);
        expect(BigNumber.from(matches[0][3]._hex)).to.be.equal(ownerTokenId);
    });

    it.skip("On swap() lock token and wait when both users confirms", async function() {
         const { contract, contractUtils, contractValue } = await loadFixture(deploy);
         const accounts = await ethers.getSigners();
         const owner = accounts[0].address;
         const anotherUser = accounts[1].address;
         const customMetadata = { 
            _offer : "Software development for Android.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
         await contractValue.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");
         await contract.registerUser(owner);
         await contract.registerDemand(owner, "Farmer products.");
         console.log("safeMint() for owner is finished");
         const customMetadataOfAnotherUser = { 
            _offer : "Farmer products.", 
            _availableSince: 0,
            _availabilityEnd: 0,
            _isConsumed: false,
            _lockedUntil: 1000 
        };
         await contractValue.safeMint(anotherUser, customMetadataOfAnotherUser, "https://dikoed.ru/");
         await contract.registerUser(anotherUser);
         await contract.registerDemand(anotherUser, "Software development for Android.");
         await contract.showMatches(owner);
         const transferEvents = await contract.queryFilter(contract.filters.Matches());
         console.log(JSON.stringify(transferEvents));
         const args = transferEvents[0].args;
         const matches = args[0];
         const match = {
             _userFirst: matches[0][0],
             _valueOfFirstUser: BigNumber.from(matches[0][1]._hex),
             _userSecond: matches[0][2],
             _valueOfSecondUser: BigNumber.from(matches[0][3]._hex),
             _lockedUntil: 0,
             _approvedByFirstUser: false,
             _approvedBySecondUser: false
         }

         await contract.swap(match);  

         // TODO update lock status
         // TODO complete me 
    });

    it.skip("Cancel lock on resources by timeout when there is no confirmation from one or many users", async function() { /* TODO complete me */});

    describe("Register offer suite", async function() {

        it("On registerUser() the next call of usersInTotal() will return increased number", async function() {
            const { contract } = await loadFixture(deploy);
            const someUser = "0x0000000000000000000000000000000000000042";
            const previousUsersAmount = await contract.usersInTotal();
    
            await contract.registerUser(someUser);
    
            expect(await contract.usersInTotal()).not.to.be.equal(previousUsersAmount);
            expect(await contract.usersInTotal()).to.be.equal(previousUsersAmount.add(1), "Call registerUser() is not successful, but it should be.");
        });
    
        it("On registerUser() with the same user twice would lead to reverted tx", async function() {
            const { contract } = await loadFixture(deploy);
    
            const someUser = "0x0000000000000000000000000000000000000042";
            await contract.registerUser(someUser);
    
            await expect(contract.registerUser(someUser)).to.be.revertedWith("User should not be registered yet.");
        });

        it("On register new user when he has a few tokens they should be indexed", async function() {
            const { contract, contractUtils, contractValue } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const owner = accounts[0].address;
            const customMetadata = { 
                _offer : "Software development for Android.", 
                _availableSince: 0,
                _availabilityEnd: 0,
                _isConsumed: false,
                _lockedUntil: 1000 
            };
            await contractValue.safeMint(owner, customMetadata, "https://gelassen.github.io/blog/");
            const anotherCustomMetadata = { 
                _offer : "Software development for Android.", 
                _availableSince: 0,
                _availabilityEnd: 0,
                _isConsumed: false,
                _lockedUntil: 1000 
            };
            await contractValue.safeMint(owner, anotherCustomMetadata, "https://gelassen.github.io/blog/");
            const oldTokensByIndex = await contract.getTokensByIndex(customMetadata._offer);
    
            await contract.registerUser(owner);
    
            const newTokensByIndex = await contract.getTokensByIndex(customMetadata._offer);
            expect(oldTokensByIndex.length).to.be.equal(0);
            expect(newTokensByIndex.length).to.be.equal(2);
            expect(BigNumber.from(newTokensByIndex[0]._hex)).to.be.equal(0);
            expect(BigNumber.from(newTokensByIndex[1]._hex)).to.be.equal(1);
        });

        it("On users in total after two users have been registered returns two", async function() {
            const { contract } = await loadFixture(deploy);
            const someUser = "0x0000000000000000000000000000000000000042";
            await contract.registerUser(someUser);
            const anotherUser = "0x0000000000000000000000000000000000000043";
            await contract.registerUser(anotherUser);
    
            await expect(await contract.usersInTotal()).to.be.equal(2)
        });

        it.skip("On registerUser() call showMatches() has been called with automatically lock resource in positive scenario.", async function() {
            // WOULDN'T IMPLEMENT: under current requirements match works only when user 
            // has both offers and demands. When registerUser() is called there is no 
            // yet registered demands -- in this case showMatches() call is redundant  
        });

        it.skip("On registerUser() call showMatches() has been called without automatically lock resource in no-match scenario.", async function() {
            // WOULDN'T IMPLEMENT: under current requirements match works only when user 
            // has both offers and demands. When registerUser() is called there is no 
            // yet registered demands -- in this case showMatches() call is redundant
        });
    })

    describe("Register demand suite", async function() {

        it("On registerDemand() next call on getDemandsByUsers() will return increased value", async function() {
            const { contract, contractUtils } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const someUser = accounts[0];
            const oldDemands = await contract.getDemandsByUser(someUser.address);
            await contract.registerUser(someUser.address);
    
            await contract.registerDemand(someUser.address, "Farmer products");
            const newDemands = await contract.getDemandsByUser(someUser.address);
    
            await expect(newDemands.length).to.be.equal(oldDemands.length + 1);
        });
    
        it("On registerDemand() twice with the same input tx is reverted", async function() {
            const { contract, contractUtils } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const someUser = accounts[0];
            const oldDemands = await contract.getDemandsByUser(someUser.address);
            await contract.registerUser(someUser.address);

            await contract.registerDemand(someUser.address, "Farmer products");
            
            await expect(contract.registerDemand(someUser.address, "Farmer products")).to.be.revertedWith("This pair <user, demand> already exist.");
        });
    
        it("On reigsterDemand() with several users and several items changes state correctly", async function() {
            const { contract, contractUtils } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const someUser = accounts[0];
            const anotherUser = accounts[1];
            await contract.registerUser(someUser.address);
            await contract.registerUser(anotherUser.address);
    
            await contract.connect(someUser).registerDemand(someUser.address, "Farmer products");
            await contract.connect(anotherUser).registerDemand(anotherUser.address, "Software Development");
            await contract.connect(anotherUser).registerDemand(anotherUser.address, "Gasoline");
    
            await expect((await contract.getDemandsByUser(someUser.address)).length).to.be.equal(1);
            await expect((await contract.getDemandsByUser(anotherUser.address)).length).to.be.equal(2);
        });

        it("On registerDemand() call where user is not registered leads to reverted tx", async function() {
            const { contract } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const someUser = accounts[0];
    
            const tx = contract.connect(someUser).registerDemand(someUser.address, "Farmer products");

            await expect(tx).to.be.revertedWith("This user hasn't been registered yet or does not meet with caller.");
        });

        it("On registerDemand() call where user is registered, but the caller is a different address leads to reverted tx", async function() {
            const { contract } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const someUser = accounts[0];
            const anotherUser = accounts[1];
            await contract.registerUser(someUser.address);

            const tx = contract.connect(anotherUser).registerDemand(someUser.address, "Farmer products");

            await expect(tx).to.be.revertedWith("This user hasn't been registered yet or does not meet with caller.");
        });

        // the requirement becomes redundanded by decoupling registerDemand() and swap() logic
        it.skip("On registerDemand() call showMatches() has been called with automatically lock resource in positive scenario.", async function() {
            const defaultLockTime = 24 * 60 * 60;
            const { contract, contractUtils, contractValue } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const customMetadata = { 
                _offer : "Software development for Android.", 
                _availableSince: 0,
                _availabilityEnd: 0,
                _isConsumed: false,
                _lockedUntil: defaultLockTime 
            };
            await contractValue.safeMint(owner.address, customMetadata, "https://gelassen.github.io/blog/");
            await contract.registerUser(owner.address);
            const customMetadataOfAnotherUser = { 
                _offer : "Farmer products.", 
                _availableSince: 0,
                _availabilityEnd: 0,
                _isConsumed: false,
                _lockedUntil: defaultLockTime 
            };
            await contractValue.safeMint(anotherUser.address, customMetadataOfAnotherUser, "https://dikoed.ru/");
            await contract.registerUser(anotherUser.address);
            const ownerTokenId = 0;
            const anotherUserTokenId = 1;
            await contractValue.connect(owner).setApprovalForAll(contract.address, true);
            await contractValue.connect(anotherUser).setApprovalForAll(contract.address, true);
            
            // register demands
            await contract.connect(owner).registerDemand(owner.address, "Farmer products.");
            await contract.connect(anotherUser).registerDemand(anotherUser.address, "Software development for Android.");
    
            const blockTimestamp = await getBlockTimeStamp();
            console.log("Block timesamp", blockTimestamp);
            await expect((await contractValue.offer(ownerTokenId))._lockedUntil).not.to.be.equal(BigNumber.from(defaultLockTime));
            await expect((await contractValue.offer(ownerTokenId))._offer).to.be.equal("Software development for Android.");
            await expect((await contractValue.offer(ownerTokenId))._lockedUntil).to.be.above(BigNumber.from(blockTimestamp));
            await expect((await contractValue.offer(ownerTokenId))._lockedUntil).to.be.lessThanOrEqual(blockTimestamp + defaultLockTime);
            await expect((await contractValue.offer(anotherUserTokenId))._lockedUntil).not.to.be.equal(defaultLockTime);
            await expect((await contractValue.offer(anotherUserTokenId))._offer).to.be.equal("Farmer products.");
        
        });

        it("On registerDemand() call showMatches() has been called without automatically lock resource in no-match scenario.", async function() {
            const defaultLockTime = 24 * 60 * 60;
            const { contract, contractUtils, contractValue } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const customMetadata = { 
                _offer : "Software development for Android.", 
                _availableSince: 0,
                _availabilityEnd: 0,
                _isConsumed: false,
                _lockedUntil: defaultLockTime 
            };
            await contractValue.safeMint(owner.address, customMetadata, "https://gelassen.github.io/blog/");
            await contract.registerUser(owner.address);
            const customMetadataOfAnotherUser = { 
                _offer : "Farmer products.", 
                _availableSince: 0,
                _availabilityEnd: 0,
                _isConsumed: false,
                _lockedUntil: defaultLockTime 
            };
            await contractValue.safeMint(anotherUser.address, customMetadataOfAnotherUser, "https://dikoed.ru/");
            await contract.registerUser(anotherUser.address);
            const ownerTokenId = 0;
            const anotherUserTokenId = 1;
            await contractValue.connect(owner).setApprovalForAll(contract.address, true);
            await contractValue.connect(anotherUser).setApprovalForAll(contract.address, true);
            
            // register demands
            await contract.connect(owner).registerDemand(owner.address, "Automatic weapons.");
            await contract.connect(anotherUser).registerDemand(anotherUser.address, "Nitrogen fertilizers.");

            await expect((await contractValue.offer(ownerTokenId))._lockedUntil).to.be.equal(defaultLockTime);
            await expect((await contractValue.offer(ownerTokenId))._offer).to.be.equal("Software development for Android.");
            await expect((await contractValue.offer(anotherUserTokenId))._lockedUntil).to.be.equal(defaultLockTime);
            await expect((await contractValue.offer(anotherUserTokenId))._offer).to.be.equal("Farmer products.");
        });

        /**
         * Get timestamp of previous block in seconds
         * https://stackoverflow.com/questions/70489025/hardhat-how-to-get-the-block-in-timestamp-in-deployment-file
         * 
         * @returns previous block's timestamp
         */
        async function getBlockTimeStamp() {
            const blockNumBefore = await ethers.provider.getBlockNumber();
            const blockBefore = await ethers.provider.getBlock(blockNumBefore);
            const timestampBefore = blockBefore.timestamp;
            return timestampBefore;
        }
    })

    describe("Approve & revoke approve for match", async function() {
        // TODO revoke approve for match, exclude locked resources from mathcing 
        // function call

        async function prepareApproveSwapConditions() {
            const defaultLockTime = 24 * 60 * 60;
            const { contract, contractValue } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const arbitr = accounts[12];
            const customMetadata = { 
                _offer : "Software development for Android.", 
                _availableSince: 0,
                _availabilityEnd: 0,
                _isConsumed: false,
                _lockedUntil: defaultLockTime 
            };
            await contractValue.safeMint(owner.address, customMetadata, "https://gelassen.github.io/blog/");
            await contract.registerUser(owner.address);

            const anotherUser = accounts[1];
            const customMetadataOfAnotherUser = { 
                _offer : "Farmer products.", 
                _availableSince: 0,
                _availabilityEnd: 0,
                _isConsumed: false,
                _lockedUntil: defaultLockTime 
            };
            await contractValue.safeMint(anotherUser.address, customMetadataOfAnotherUser, "https://dikoed.ru/");
            await contract.registerUser(anotherUser.address);

            const ownerTokenId = 0;
            const anotherUserTokenId = 1;
            await contractValue.connect(owner).setApprovalForAll(contract.address, true);
            await contractValue.connect(anotherUser).setApprovalForAll(contract.address, true);
        
            await contract.connect(owner).registerDemand(owner.address, "Farmer products.");
            await contract.connect(anotherUser).registerDemand(anotherUser.address, "Software development for Android.");

        }

        it("On approveMatch() call in positive scenario selected match object has approved flag as true by both users", async function() {
            const { contract, contractValue } = await loadFixture(deploy);
            await prepareApproveSwapConditions();
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const notRegisteredUser = accounts[2];
            const match = {
                _userFirst: owner.address,
                _valueOfFirstUser: 0,
                _userSecond: anotherUser.address,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.connect(owner).swapNoParams(owner.address);

            // await expect(contract.connect(owner).approveMatch(match)).not.to.be.reverted
            // await expect(contract.connect(anotherUser).approveMatch(match)).not.to.be.reverted
            // TODO On September 2022 I do not see a way to check changes in contract state 
        });

        it("On approveSwap() call when passed Match does not exist call ends with reverted tx", async function() {
            const { contract } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const match = {
                _userFirst: owner.address,
                _valueOfFirstUser: 0,
                _userSecond: anotherUser.address,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }

            await expect(contract.approveSwap(match)).to.be.revertedWith("There is no Match object which is equal to the passed input parameter.");
        });

        it("On approveSwap() call when caller does not match any address in passed Match object call ends with reverted tx", async function() {
            const { contract, contractValue } = await loadFixture(deploy);
            console.log("Contract address ", contract.address);
            await prepareApproveSwapConditions();
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const notRegisteredUser = accounts[2];
            const arbitrUser = accounts[12];
            const tokenIdMintedByFirstUser = 0;
            const tokenIdMinedBySecondUser = 1;
            const match = {
                _userFirst: owner.address,
                _valueOfFirstUser: tokenIdMintedByFirstUser,
                _userSecond: anotherUser.address,
                _valueOfSecondUser: tokenIdMinedBySecondUser,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            
            expect(await contractValue.isApprovedForAll(owner.address, contract.address)).to.be.equal(true);
            expect(await contractValue.isApprovedForAll(anotherUser.address, contract.address)).to.be.equal(true); 
            await contract.connect(notRegisteredUser).swap(match);

            await expect(contract.connect(notRegisteredUser).approveSwap(match)).to.be.revertedWith("Caller should be an one of the users defined in the passed match object.");
        });

        it.skip("On approveSwap() call when current time exceeds time in match._lockedUntil leads to reverted tx", async function() {
            // FIXME currently disabled as at this moment there is no way to pass lock time
            const { contract } = await loadFixture(deploy);
            await prepareApproveSwapConditions();
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const notRegisteredUser = accounts[2];
            const match = {
                _userFirst: owner.address,
                _valueOfFirstUser: 0,
                _userSecond: anotherUser.address,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }

            await expect(contract.connect(owner).approveSwap(match)).to.be.revertedWith("Current time should not exceed time of value's has been locked.");
        });

        it.skip("On approveSwap() call when SwapChain contract has not been approved by both token's owners to transfer tokens leads to reverted tx", async function() {
            // FIXME can not test this case because mutually exclusive pieces of logic should be meet here
            const { contract } = await loadFixture(deploy);
            await prepareApproveSwapConditions();
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const notRegisteredUser = accounts[2];
            const match = {
                _userFirst: owner.address,
                _valueOfFirstUser: 0,
                _userSecond: anotherUser.address,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.connect(owner).swapNoParams(owner.address);

            await expect(contract.connect(owner).approveSwap(match)).to.be.revertedWith("SwapChain should be allowed to transfer tokens by BOTH users. Please confirm both users in match has call SwapValue.approve(swapChainContractAddress).");
        });

        it("On approveSwap() call in positive scenario SwapChain transfers tokens and makes them consumed", async function() {
            const { contract, contractValue } = await loadFixture(deploy);
            await prepareApproveSwapConditions();
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const notRegisteredUser = accounts[2];
            const match = {
                _userFirst: owner.address,
                _valueOfFirstUser: 0,
                _userSecond: anotherUser.address,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.connect(notRegisteredUser).swapNoParams(owner.address);
            // await contract.connect(owner).approveMatch(match);
            // await contract.connect(anotherUser).approveMatch(match);

            await contract.connect(owner).approveSwap(match);
            await contract.connect(anotherUser).approveSwap(match)
            await contract.connect(notRegisteredUser).approveSwapNoParams();

            await expect(await contractValue.ownerOf(match._valueOfFirstUser)).to.be.equal(match._userSecond);
            await expect(await contractValue.ownerOf(match._valueOfSecondUser)).to.be.equal(match._userFirst);
            await expect((await contractValue.offer(match._valueOfFirstUser))._isConsumed).to.be.equal(true);
            await expect((await contractValue.offer(match._valueOfSecondUser))._isConsumed).to.be.equal(true);
            await expect((await contractValue.offer(match._valueOfFirstUser))._lockedUntil).to.be.equal(0);
            await expect((await contractValue.offer(match._valueOfSecondUser))._lockedUntil).to.be.equal(0);
        });

        it.skip("On approveSwap() call by non-contract address leads to reverted tx", async function() {
            // WOULDN'T IMPLEMENT: the caller is non-contract, but user address; however all call of another contracts
            // are considered as 'call by contract address' and contract address prudently has been approved by both token owners
            // this requirement became redundant
        });

        // TODO complete me 
        it.skip("On approveSwap() when of one of users has not approved swap leads to reverted tx", async function() {
            const { contract, contractValue } = await loadFixture(deploy);
            await prepareApproveSwapConditions();
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const notRegisteredUser = accounts[2];
            const match = {
                _userFirst: owner.address,
                _valueOfFirstUser: 0,
                _userSecond: anotherUser.address,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.connect(notRegisteredUser).swapNoParams(owner.address);
            // await contract.connect(owner).approveMatch(match);
            // await contract.connect(anotherUser).approveMatch(match);
            
            await contract.connect(owner).approveSwap(match);

            await expect(contract.connect(notRegisteredUser).approveSwapNoParams()).to.be.revertedWith("Match has not been approved by second user");
        });

    });

    describe.skip("SwapChain utils suite", async function() {

        async function printSigners() {
            const accounts = await ethers.getSigners();
            console.log(accounts[0].address);
            console.log(accounts[1].address);
            console.log(accounts[12].address);
        }

        it("Check ethers.getSigners() returns different signers for each call", async function() {
            console.log("Signers external function call");
            await printSigners();
            console.log("Signers inner test function call");
            const accounts = await ethers.getSigners();
            console.log(accounts[0].address);
            console.log(accounts[1].address);
            console.log(accounts[12].address);
        });

        /* 
        it("On deletePendingMatchItem() call with zero length tx is not reverted", async function() {
            const { contract, contractUtils, contractValue } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const match = {
                _userFirst: owner.address,
                _userSecond: anotherUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }

            await expect(contract._deletePendingMatchItem(match)).not.to.be.reverted;
        });
               
        it("On deletePendingMatchItem() call with single length item is successfully deleted", async function() {
            const { contract } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const match = {
                _userFirst: owner.address,
                _userSecond: anotherUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(match);
            await expect(BigNumber.from((await contract.pendingMatchesSize())._hex)).to.be.equal(1);

            expect(contract._deletePendingMatchItem(match)).not.to.be.reverted;
            await expect(BigNumber.from((await contract.pendingMatchesSize())._hex)).to.be.equal(0);
        });

        it("On deletePendingMatchItem() call with length of two item is successfully deleted and length is one", async function() {
            const { contract } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const extraUser = accounts[2];
            const match = {
                _userFirst: owner.address,
                _userSecond: anotherUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(match);
            const matchTwo = {
                _userFirst: owner.address,
                _userSecond: extraUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(matchTwo);
            await expect(BigNumber.from((await contract.pendingMatchesSize())._hex)).to.be.equal(2);

            expect(contract._deletePendingMatchItem(match)).not.to.be.reverted;
            await expect(BigNumber.from((await contract.pendingMatchesSize())._hex)).to.be.equal(1);
        });

        it("On deletePendingMatchItem() call with length of three item is successfully deleted and length is two", async function() {
            const { contract } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const extraUser = accounts[2];
            const match = {
                _userFirst: owner.address,
                _userSecond: anotherUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(match);
            const matchTwo = {
                _userFirst: owner.address,
                _userSecond: extraUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(matchTwo);
            const matchThree = {
                _userFirst: anotherUser.address,
                _userSecond: extraUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(matchThree);
            await expect(BigNumber.from((await contract.pendingMatchesSize())._hex)).to.be.equal(3);

            expect(contract._deletePendingMatchItem(matchTwo)).not.to.be.reverted;
            await expect(BigNumber.from((await contract.pendingMatchesSize())._hex)).to.be.equal(2);
        });

        it("On deletePendingMatchItem() call with length of four item is successfully deleted and length is three", async function() {
            const { contract } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const extraUser = accounts[2];
            const fourthUser = accounts[3];
            const match = {
                _userFirst: owner.address,
                _userSecond: anotherUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(match);
            const matchTwo = {
                _userFirst: owner.address,
                _userSecond: extraUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(matchTwo);
            const matchThree = {
                _userFirst: anotherUser.address,
                _userSecond: extraUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(matchThree);
            const matchFour = {
                _userFirst: owner.address,
                _userSecond: fourthUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(matchFour);
            await expect(BigNumber.from((await contract.pendingMatchesSize())._hex)).to.be.equal(4);

            expect(contract._deletePendingMatchItem(matchTwo)).not.to.be.reverted;
            await expect(BigNumber.from((await contract.pendingMatchesSize())._hex)).to.be.equal(3);
        });

        it("On deletePendingMatchItem() call with length of four first item is successfully deleted and length is three", async function() {
            const { contract } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const extraUser = accounts[2];
            const fourthUser = accounts[3];
            const match = {
                _userFirst: owner.address,
                _userSecond: anotherUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(match);
            const matchTwo = {
                _userFirst: owner.address,
                _userSecond: extraUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(matchTwo);
            const matchThree = {
                _userFirst: anotherUser.address,
                _userSecond: extraUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(matchThree);
            const matchFour = {
                _userFirst: owner.address,
                _userSecond: fourthUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(matchFour);
            await expect(BigNumber.from((await contract.pendingMatchesSize())._hex)).to.be.equal(4);

            expect(contract._deletePendingMatchItem(match)).not.to.be.reverted;
            await expect(BigNumber.from((await contract.pendingMatchesSize())._hex)).to.be.equal(3);
        });

        it("On deletePendingMatchItem() call with length of four last item is successfully deleted and length is three", async function() {
            const { contract } = await loadFixture(deploy);
            const accounts = await ethers.getSigners();
            const owner = accounts[0];
            const anotherUser = accounts[1];
            const extraUser = accounts[2];
            const fourthUser = accounts[3];
            const match = {
                _userFirst: owner.address,
                _userSecond: anotherUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(match);
            const matchTwo = {
                _userFirst: owner.address,
                _userSecond: extraUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(matchTwo);
            const matchThree = {
                _userFirst: anotherUser.address,
                _userSecond: extraUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(matchThree);
            const matchFour = {
                _userFirst: owner.address,
                _userSecond: fourthUser.address,
                _valueOfFirstUser: 0,
                _valueOfSecondUser: 1,
                _approvedByFirstUser: false,
                _approvedBySecondUser: false
            }
            await contract.pushMatch(matchFour);
            await expect(BigNumber.from((await contract.pendingMatchesSize())._hex)).to.be.equal(4);

            expect(contract._deletePendingMatchItem(matchFour)).not.to.be.reverted;
            await expect(BigNumber.from((await contract.pendingMatchesSize())._hex)).to.be.equal(3);
        }); 
        */
    });
});