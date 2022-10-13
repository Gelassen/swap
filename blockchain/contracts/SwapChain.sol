// SPDX-License-Identifier: MIT

pragma solidity ^0.8.4;

import { User, Match } from "../contracts/structs/StructDeclaration.sol";
import "../contracts/data_structures/MatchDynamicArray.sol";
import "../contracts/ISwapChain.sol";
import "../contracts/SwapValue.sol";
import { Utils } from "../contracts/utils/Utils.sol";
// import "hardhat/console.sol";

/**
 * ERC721 and ERC20 standarts already has links on tokens owned by address. 
 * Take a look on balanceOf(owner) and tokenOfOwnerByIndex(address owner, uint256 index)
 * https://docs.openzeppelin.com/contracts/3.x/api/token/erc721#IERC721Enumerable-tokenOfOwnerByIndex-address-uint256-
 * https://docs.openzeppelin.com/contracts/3.x/api/token/erc721#IERC721-balanceOf-address-
 * Please note: balanceOf(owner) for IERC721Enumerable is very gas costly and vector of 
 * attacks.
 * 
 * Alternative to IERC721Enumerable api is contract.getPastEvents():
 * https://stackoverflow.com/questions/69302924/erc-721-how-to-get-all-token-ids 
 * 
 * For things like instanceOf() and typeOf() use check by interface implementation
 * like described in this blog post https://kushgoyal.com/solidity-how-to-check-type-of-the-contract-erc-165/
 * 
 * 
 */
contract SwapChain is ISwapChain {

    event Matches(
        Match[] matches
    );

    uint256 private LOCK_TIME = 24 * 60 * 60; // 24 hours

    address private EMPTY_ADDRESS = 0x0000000000000000000000000000000000000000;

    Utils _utils;
    SwapValue _swapValueContract;
    address[] _users;
    mapping(string => uint256[]) _indexNfts;
    mapping(address => string[]) _demandsByUsers;
    Match[] _pendingMatches;

    // data structures
    MatchDynamicArray _matches;

    constructor(
        address utilsAddr, 
        address valueToken, 
        address matchDynamicArray) {
        _utils = Utils(utilsAddr);
        _swapValueContract = SwapValue(valueToken);
        _matches = MatchDynamicArray(matchDynamicArray);
    }

    modifier existingMatch(Match memory obj) {
        bool hasMatch = false;
        // console.log("[start] modifier");
        for (uint256 idx = 0; idx < _pendingMatches.length; idx++) {
            Match memory pendingMatch = _pendingMatches[0];
            // console.log("[check match object]");
            // console.log(pendingMatch._userFirst);
            // console.log(pendingMatch._userSecond);
            // console.log(obj._userFirst);
            if (pendingMatch._userFirst == obj._userFirst
                && pendingMatch._userSecond == obj._userSecond
                && pendingMatch._valueOfFirstUser == obj._valueOfFirstUser
                && pendingMatch._valueOfSecondUser == obj._valueOfSecondUser
                ||
                (
                    pendingMatch._userFirst == obj._userSecond
                    && pendingMatch._userSecond == obj._userFirst
                    && pendingMatch._valueOfFirstUser == obj._valueOfSecondUser
                    && pendingMatch._valueOfSecondUser == obj._valueOfFirstUser
                )
            ) {
                hasMatch = true;
            }
        }
        // console.log("[end] modifier, before require");
        require(hasMatch, "There is no Match object which is equal to the passed input parameter.");
        _;
    }

    modifier hasBeenAproved(Match memory subj) {
        // console.log("[start] hasBeenAproved()");
        address approveFirst = _swapValueContract.getApproved(subj._valueOfFirstUser);
        address approveSecond = _swapValueContract.getApproved(subj._valueOfSecondUser);
        bool approvedForAllFirst = _swapValueContract.isApprovedForAll(subj._userFirst, address(this));
        bool approvedForAllSecond = _swapValueContract.isApprovedForAll(subj._userSecond, address(this));
        // console.log(approveFirst);
        // console.log(approveSecond);
        // console.log(address(this));
        require(
            (address(this) == approveFirst && address(this) == approveSecond)
                || (approvedForAllFirst && approvedForAllSecond), 
                    "SwapChain should be allowed to transfer tokens by BOTH users. Please confirm both users in match has call SwapValue.approve(swapChainContractAddress).");
        // console.log("[end] hasBeenAproved()");
        _;
    }

    function registerUser(address user) public override {
        require(_isUserExist(user) != true, "User should not be registered yet.");
        _users.push(user);
        _updateNftsIndex(user);
    }

    function usersInTotal() external view override returns (uint256) {
        return _users.length;
    }

    function registerDemand(address user, string memory demand) external {
        // console.log("[start] registerDemand()");
        require(msg.sender == user && _isUserExist(user), "This user hasn't been registered yet or does not meet with caller.");

        string[] memory demands = _demandsByUsers[user];
        bool isExist = false;
        for (uint idx = 0; idx < demands.length; idx++) {
            // console.log(demands[idx], demand);
            if (_utils.stringsEquals(demands[idx], demand)) {
                isExist = true;
                break;
            }            
        }
        if (isExist) {
            revert("This pair <user, demand> already exist.");
        } else {
            _demandsByUsers[user].push(demand);
        }
        // console.log("[end] registerDemand()");
    }

    function getDemandsByUser(address user) external view override returns (string[] memory) {
        return _demandsByUsers[user];
    }

    function showMatches(address user) external override returns(Match[] memory) {
        // console.log("[start] showMatchDEV");
        require(_isUserExist(user) == true, "User should be registered first to view matches.");
        require(_demandsByUsers[user].length != 0, "User should have at least one registered demand.");

        // FIXME
        // TODO customize dynamic array to use with Match struct and make it fully dynamically resizeable
        // Match[] memory result = new Match[](20);
        // console.log("_demandsByUsers[user].length", _demandsByUsers[user].length);
        for (uint idx = 0; idx < _demandsByUsers[user].length; idx++) {
            // console.log("Within 1st loop");
            string memory jackDemand = _demandsByUsers[user][idx];
            uint256[] memory jackOffers = _swapValueContract.getTokensIdsForUser(user);
            uint256[] memory allAvailableOffers = _indexNfts[jackDemand];
            // console.log("jack demand", jackDemand);
            // console.log("allAvailableOffers.length", allAvailableOffers.length);
            if (allAvailableOffers.length == 0) {
                continue;
            } 
            for (uint offerIdx = 0; offerIdx < allAvailableOffers.length; offerIdx++) {
                uint256 someAliceOffer = allAvailableOffers[offerIdx];
                address someAliceAddress = _swapValueContract.ownerOf(someAliceOffer);
                string[] memory aliceDemands = _demandsByUsers[someAliceAddress];
                Match memory potentialMatch = _isMatch(jackDemand, jackOffers, someAliceOffer, aliceDemands);
                if (_isStructureNonEmpty(potentialMatch) 
                        && _notLocked(potentialMatch)
                            && _notConsumed(potentialMatch)) {
                    // console.log("[end] showMatchDEV - before push");
                    _matches.push(potentialMatch);
                }
            }
        }
        emit Matches(_matches.getArray());        
        // console.log("[end] showMatchDEV");
        return _matches.getArray();
    }

    function swap(Match memory subj) external override {
        uint256 lockUntil = block.timestamp + LOCK_TIME;
        // console.log("[start] swap call");
        // console.log(msg.sender);
        // console.log(_swapValueContract.getApproved(subj._valueOfFirstUser));
        // console.log(subj._valueOfFirstUser);
        // console.log("[in progress] call in progress");
        _swapValueContract.lockToken(subj._valueOfFirstUser, lockUntil);
        _swapValueContract.lockToken(subj._valueOfSecondUser, lockUntil);
        _pendingMatches.push(subj);

        require(_swapValueContract.offer(subj._valueOfFirstUser)._lockedUntil == lockUntil, "One or both values are not locked after swap() call.");
        require(_swapValueContract.offer(subj._valueOfSecondUser)._lockedUntil == lockUntil, "One or both values are not locked after swap() call.");
        // console.log("[end] swap call");
    }

    function swapNoParams(address userWhoLooksForMatch) external override {
        Match[] memory matches = this.showMatches(userWhoLooksForMatch);
        if (matches.length > 0) {
            this.swap(matches[0]);
            // console.log("[before swap] There is a match");
        } else {
            // console.log("[before swap] There is NO match");
        }
    }

    function approveSwap(Match memory subj) external override 
        existingMatch(subj)
        hasBeenAproved(subj) {
        require(msg.sender == subj._userFirst || msg.sender == subj._userSecond, "Caller should be an one of the users defined in the passed match object.");
        (bool hasMatch, Match memory pendingMatch) = _getPendingMatch(subj);
        require(block.timestamp < _swapValueContract.offer(pendingMatch._valueOfFirstUser)._lockedUntil 
                    && block.timestamp < _swapValueContract.offer(pendingMatch._valueOfSecondUser)._lockedUntil,
                    "Current time should not exceed time of value's has been locked.");
        _approveMatch(subj);
    }

    function approveSwapNoParams() external override {
        for (uint idx = 0; idx < _pendingMatches.length; idx++) {
            Match memory pendingMatch = _pendingMatches[idx];
            require(_pendingMatches[idx]._approvedByFirstUser = true, "Match has not been approved by first user");
            require(_pendingMatches[idx]._approvedBySecondUser = true, "Match has not been approved by second user");
            // the order is important: after safeTransferFrom() the token owner has changed which also cleanup approve() rights
            // which has been granted at begining and makes unavailable any operations from approved address 
            _swapValueContract.makeTokenConsumed(pendingMatch._valueOfFirstUser);
            _swapValueContract.makeTokenConsumed(pendingMatch._valueOfSecondUser);
            
            _swapValueContract.safeTransferFrom(
                pendingMatch._userFirst, 
                pendingMatch._userSecond, 
                pendingMatch._valueOfFirstUser);
            _swapValueContract.safeTransferFrom(
                pendingMatch._userSecond, 
                pendingMatch._userFirst, 
                pendingMatch._valueOfSecondUser);
            
            _cleanUp(pendingMatch);
        }
    }

    /** 
     * Tests only.
    */
    function getTokensByIndex(string memory demand) public view returns(uint256[] memory) {
        return _indexNfts[demand];
    }

    function _isStructureNonEmpty(Match memory subj) private view returns(bool) {
        return subj._userFirst != EMPTY_ADDRESS
                    && subj._userSecond != EMPTY_ADDRESS;
    }

    /**
     * @param jackDemand A demand of an user who runs a system to find matches
     * @param jackOffers Offers of an user who runs a system to find matches
     * @param aliceOffer An offer of an user who passivly waits for exchange
     * @param aliceDemands Demands of an user who passivly waits for exchange
     */
    function _isMatch(
        string memory jackDemand, 
        uint256[] memory jackOffers,
        uint256 aliceOffer, 
        string[] memory aliceDemands) private view returns(Match memory) {
        require(_utils.stringsEquals(jackDemand, _swapValueContract.offer(aliceOffer)._offer), "Caller demand should matches offer which is obtained over _indexNft.");
        // console.log("[start] isMatch()");

        bool isThereMutualMatch = false;
        uint256 matchedJackOffer;
        for (uint idx = 0; idx < jackOffers.length; idx++) {
            Value memory offer = _swapValueContract.offer(jackOffers[idx]);
            for (uint aliceIdx = 0; aliceIdx < aliceDemands.length; aliceIdx++) {
                if (_utils.stringsEquals(offer._offer, aliceDemands[aliceIdx])) {
                    // aliceOfferWhichMatches = aliceDemands[idx];
                    // obtain a ALice's match tokenId
                    // obtain Jack;s match tokenId
                    //  however for demands there is an owner, but no tokenId...
                    // -- but it doesn't matter - we have value for user1 demands and
                    // we have value for user2 demand => new Match()
                    matchedJackOffer = jackOffers[idx];
                    isThereMutualMatch = true;
                    break;
                }
            }
        }

        Match memory result;
        if (isThereMutualMatch) {
            result._userFirst = _swapValueContract.ownerOf(matchedJackOffer);
            result._valueOfFirstUser = matchedJackOffer;
            result._userSecond = _swapValueContract.ownerOf(aliceOffer);
            result._valueOfSecondUser = aliceOffer;
        }
        // console.log("[end] isMatch()"); 
        return result;
    }

    function _cleanUp(Match memory pendingMatch) private {
        _deletePendingMatchItem(pendingMatch);
        _deleteDemands(pendingMatch);
        _deleteIndexedTokens(pendingMatch);
    }

    function _addToCollection(address[] storage result, address[] memory addrs) private returns (address[] memory) {
        for (uint idx = 0; idx < addrs.length; idx++) {
            result.push(addrs[idx]);
        }
        return result;
    }

    function _deletePendingMatchItem(Match memory obj) private {
        if (_pendingMatches.length == 0) return;
        Match[] memory result = new Match[](_pendingMatches.length - 1); 
        uint resultIdx = 0;
        for (uint idx = 0; idx < _pendingMatches.length - 1; idx++){
            Match memory pendingItem = _pendingMatches[idx];
            if (pendingItem._userFirst == obj._userFirst
                && pendingItem._userSecond == obj._userSecond
                && pendingItem._valueOfFirstUser == obj._valueOfFirstUser
                && pendingItem._valueOfSecondUser == obj._valueOfSecondUser
                ||
                (
                    pendingItem._userFirst == obj._userSecond
                    && pendingItem._userSecond == obj._userFirst
                    && pendingItem._valueOfFirstUser == obj._valueOfSecondUser
                    && pendingItem._valueOfSecondUser == obj._valueOfFirstUser
                )
            ) {
                // skip item and since this index we will take item under [idx + 1]
                ++resultIdx;
            } 
            if (resultIdx > _pendingMatches.length - 1) { 
                continue;
            }
            _pendingMatches[idx] = _pendingMatches[resultIdx];
        }
        _pendingMatches.pop();
    }

    function _deleteDemands(Match memory pendingMatch) private {
        string[] memory userFirstDemands = _demandsByUsers[pendingMatch._userFirst];
        string memory demandOfFirstUser = _swapValueContract.offer(pendingMatch._valueOfSecondUser)._offer;
        for (uint idx = 0; idx < userFirstDemands.length; idx++) {
            if (_stringsEquals(userFirstDemands[idx], demandOfFirstUser)) {
                delete _demandsByUsers[pendingMatch._userFirst][idx];
                break;
            }
        }
        string[] memory userSecondDemands = _demandsByUsers[pendingMatch._userSecond];
        string memory demandOfSecondUser = _swapValueContract.offer(pendingMatch._valueOfFirstUser)._offer;
        for (uint idx = 0; idx < userSecondDemands.length; idx++) {
            if (_stringsEquals(userSecondDemands[idx], demandOfSecondUser)) {
                delete _demandsByUsers[pendingMatch._userSecond][idx];
                break;
            }
        }
    }

    function _deleteIndexedTokens(Match memory pendingMatch) private {
        string memory demandOfSecondUser = _swapValueContract.offer(pendingMatch._valueOfFirstUser)._offer;
        uint256[] memory tokensByDemandOfSecondUser = _indexNfts[demandOfSecondUser];
        for (uint idx = 0; idx < tokensByDemandOfSecondUser.length; idx++) {
            if (tokensByDemandOfSecondUser[idx] == pendingMatch._valueOfFirstUser) {
                delete _indexNfts[demandOfSecondUser][idx];
                break;
            }
        }
        string memory demandOfFirstUser = _swapValueContract.offer(pendingMatch._valueOfSecondUser)._offer;
        uint256[] memory tokensByDemandOfFirstUser = _indexNfts[demandOfFirstUser];
        for (uint idx = 0; idx < tokensByDemandOfFirstUser.length; idx++) {
            if (tokensByDemandOfFirstUser[idx] == pendingMatch._valueOfSecondUser) {
                delete _indexNfts[demandOfFirstUser][idx];
                break;
            }
        }
    }

    function _notLocked(Match memory matchObj) private view returns(bool) {
        uint256 firstValueLock = _swapValueContract.offer(matchObj._valueOfFirstUser)._lockedUntil;
        uint256 secondValueLock = _swapValueContract.offer(matchObj._valueOfSecondUser)._lockedUntil;
        return block.timestamp > firstValueLock 
                &&  block.timestamp > secondValueLock;
    }

    function _notConsumed(Match memory matchObj) private view returns(bool) {
        uint256 firstValueLock = _swapValueContract.offer(matchObj._valueOfFirstUser)._lockedUntil;
        uint256 secondValueLock = _swapValueContract.offer(matchObj._valueOfSecondUser)._lockedUntil;
        return block.timestamp > firstValueLock 
                &&  block.timestamp > secondValueLock;
    }

    function _isUserExist(address userAddr) private view returns (bool) {
        for (uint idx = 0; idx < _users.length; idx++) {
            if (_users[idx] == userAddr) {
                return true;
            }
        }
        return false;
    }

    function _updateNftsIndex(address user) private {
        // console.log("Update index for user", user);
        uint256[] memory tokensIds = _swapValueContract.getTokensIdsForUser(user);
        for (uint idx = 0; idx < tokensIds.length; idx++) {
            uint256 tokenId = tokensIds[idx];
            string memory offer = _swapValueContract.offer(tokenId)._offer;
            _indexNfts[offer].push(tokenId);
            // console.log("Register new index", offer);
        }
    }

    function _getPendingMatch(Match memory obj) private returns(bool, Match memory) {
        Match memory tmp;
        for (uint256 idx = 0; idx < _pendingMatches.length; idx++) {
            Match memory pendingMatch = _pendingMatches[idx];
            // console.log(pendingMatch._userFirst);
            // console.log(obj._userFirst);
            if (pendingMatch._userFirst == obj._userFirst
                && pendingMatch._userSecond == obj._userSecond
                && pendingMatch._valueOfFirstUser == obj._valueOfFirstUser
                && pendingMatch._valueOfSecondUser == obj._valueOfSecondUser
                ||
                (
                    pendingMatch._userFirst == obj._userSecond
                    && pendingMatch._userSecond == obj._userFirst
                    && pendingMatch._valueOfFirstUser == obj._valueOfSecondUser
                    && pendingMatch._valueOfSecondUser == obj._valueOfFirstUser
                )
            ) {
                return (true, pendingMatch);
            }
        }
        return (false, tmp);
    }

    function _approveMatch(Match memory subj) private {
        for (uint256 idx = 0; idx < _pendingMatches.length; idx++) {
            Match memory pendingMatch = _pendingMatches[idx];
            if (pendingMatch._userFirst == subj._userFirst
                && pendingMatch._userSecond == subj._userSecond
                && pendingMatch._valueOfFirstUser == subj._valueOfFirstUser
                && pendingMatch._valueOfSecondUser == subj._valueOfSecondUser
                ||
                (
                    pendingMatch._userFirst == subj._userSecond
                    && pendingMatch._userSecond == subj._userFirst
                    && pendingMatch._valueOfFirstUser == subj._valueOfSecondUser
                    && pendingMatch._valueOfSecondUser == subj._valueOfFirstUser
                )
            ) {
                if (pendingMatch._userFirst == msg.sender) {
                    _pendingMatches[idx]._approvedByFirstUser = true;
                    break;
                } else if (pendingMatch._userSecond == msg.sender) {
                    _pendingMatches[idx]._approvedBySecondUser = true;
                    break;
                }
            }
        }
    }

    function _stringsEquals(string memory s1, string memory s2) private pure returns (bool) {
        bytes memory b1 = bytes(s1);
        bytes memory b2 = bytes(s2);
        uint256 l1 = b1.length;
        if (l1 != b2.length) return false;
        for (uint256 i=0; i<l1; i++) {
            if (b1[i] != b2[i]) return false;
        }
        return true;
    }
} 