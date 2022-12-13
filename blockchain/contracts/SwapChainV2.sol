pragma solidity ^0.8.4;

import { User, Match } from "../contracts/structs/StructDeclaration.sol";
import "../contracts/data_structures/MatchDynamicArray.sol";
import "../contracts/ISwapChainV2.sol";
import "../contracts/SwapValue.sol";
import { Utils } from "../contracts/utils/Utils.sol";

contract SwapChainV2 is ISwapChainV2 {

    // TODO: in case of match by address relationship for two addresses we have two the same match objects
    // which should be changed simultaneously => find out a way two addreses to become an one single key
    
    SwapValue _swapValueContract;
    Utils _utils;
    address[] _users;
    mapping(uint256 => Match[]) _matchesByUser; // TODO replace by hash function see https://ethereum.stackexchange.com/questions/76492/combine-two-addresses-into-one

    constructor(
        address utilsAddr, 
        address valueToken) {
        _utils = Utils(utilsAddr);
        _swapValueContract = SwapValue(valueToken);
    }

    modifier callerIsRegisteredUser(address userFirst, address userSecond) {
        require(msg.sender == userFirst || msg.sender == userSecond, "Caller should be an one of the users defined in the passed match object.");
        _;
    }

    modifier userExists(address userFirst, address userSecond) {
        require(_isUserExist(userFirst) == true, "The first user should be registered first.");
        require(_isUserExist(userSecond) == true, "The second user should be registered first.");
        _;
    }

    modifier bothHaveValidTokens(Match memory subj) {
        require(_swapValueContract.offer(subj._valueOfFirstUser)._isConsumed != true 
                    && _swapValueContract.offer(subj._valueOfSecondUser)._isConsumed != true,
                    "Tokens should not be already consumed.");
        require(block.timestamp < _swapValueContract.offer(subj._valueOfFirstUser)._lockedUntil 
                    && block.timestamp < _swapValueContract.offer(subj._valueOfSecondUser)._lockedUntil,
                    "Current time should not exceed time of value's has been locked.");  
        _;
    }

    function registerUser(address user) public override {
        require(_isUserExist(user) != true, "User should not be registered yet.");
        _users.push(user);
        // _updateNftsIndex(user);
    }
    
    function swap(Match memory subj) external override
        callerIsRegisteredUser(subj._userFirst, subj._userSecond)
        userExists(subj._userFirst, subj._userSecond)
        bothHaveValidTokens(subj) {

        int256 matchItemIndex = _getMatchItemIndex(subj);
        require(matchItemIndex != -1, "Match item is not found. Did you pass correct match object?");

        uint256 hashedKey = _getValidHash(subj._userFirst, subj._userSecond);
        Match memory item = _matchesByUser[hashedKey][uint256(matchItemIndex)];
        require(item._approvedByFirstUser && item._approvedBySecondUser, "One or both users hasn't confirmed yet swap.");

        _swapValueContract.makeTokenConsumed(item._valueOfFirstUser);
        _swapValueContract.makeTokenConsumed(item._valueOfSecondUser);
            
        _swapValueContract.safeTransferFrom(
            item._userFirst, 
            item._userSecond, 
            item._valueOfFirstUser
        );
        _swapValueContract.safeTransferFrom(
            item._userSecond, 
            item._userFirst, 
            item._valueOfSecondUser
        );
            
        _cleanUp(hashedKey, uint256(matchItemIndex));
    }

    function approveSwap(Match memory subj) external override
        callerIsRegisteredUser(subj._userFirst, subj._userSecond)
        userExists(subj._userFirst, subj._userSecond)
        bothHaveValidTokens(subj) {

        /* require(msg.sender == subj._userFirst || msg.sender == subj._userSecond, "Caller should be an one of the users defined in the passed match object."); */
        // both users are registered
        /* require(_isUserExist(subj._userFirst) == true, "The first user should be registered first.");
        require(_isUserExist(subj._userSecond) == true, "The second user should be registered first."); */
        // TODO 1st user has associated value 
        // TODO 2nd user has associated value
        // 1st user's associated value is not consumed and not expired  
        // 2nd user's associated value is not consumed and not expired
        /* require(_swapValueContract.offer(subj._valueOfFirstUser)._isConsumed != true 
                    && _swapValueContract.offer(subj._valueOfSecondUser)._isConsumed != true,
                    "Tokens should not be already consumed.");
        require(block.timestamp < _swapValueContract.offer(subj._valueOfFirstUser)._lockedUntil 
                    && block.timestamp < _swapValueContract.offer(subj._valueOfSecondUser)._lockedUntil,
                    "Current time should not exceed time of value's has been locked."); */       
        // TODO 1st user's associated value match with 2nd user's associated value (how to implement it here without extra gas usage?)
        // TODO 2nd user's associated value match with 1st user's associated value (how to implement it here without extra gas usage?)
        int256 matchItemIndex = _getMatchItemIndex(subj);
        require(matchItemIndex != -1, "Match item is not found. Did you pass correct match object?");

        uint256 hashedKey = _getValidHash(subj._userFirst, subj._userSecond);
        Match memory item = _matchesByUser[hashedKey][uint256(matchItemIndex)]; // we have to use this version instead of subj because users and its values might have changed order
        require(item._approvedByFirstUser == false || item._approvedBySecondUser == false, "This match object already approved by both users.");

        if (msg.sender == item._userFirst) {
            require(item._approvedByFirstUser == false, "Match is already approved by this, first, user.");
            _matchesByUser[hashedKey][uint256(matchItemIndex)]._approvedByFirstUser = true;
        } else if (msg.sender == item._userSecond) {
            require(item._approvedBySecondUser == true, "Match is already approved by this, second, user.");
            _matchesByUser[hashedKey][uint256(matchItemIndex)]._approvedBySecondUser = true;
        }
    }

    function _isUserExist(address userAddr) private view returns (bool) {
        for (uint idx = 0; idx < _users.length; idx++) {
            if (_users[idx] == userAddr) {
                return true;
            }
        }
        return false;
    }    

    function _getMatchItemIndex(Match memory subj) private view returns (int256) {
        int256 result = -1;
        uint256 hashedKey = _getValidHash(subj._userFirst, subj._userSecond);
        Match[] memory matches = _matchesByUser[hashedKey];
        for (uint256 idx = 0; idx < matches.length; idx++) {
            Match memory item = matches[idx];
            bool isTheSameUsers = (subj._userFirst == item._userFirst && subj._userSecond == item._userSecond) 
                ||  (subj._userFirst == item._userSecond && subj._userSecond == item._userFirst);
            bool isTheSameTokens = (subj._valueOfFirstUser == item._valueOfFirstUser && subj._valueOfSecondUser == item._valueOfSecondUser)
                ||  (subj._valueOfFirstUser == item._valueOfSecondUser && subj._valueOfSecondUser == item._valueOfFirstUser);
            if (isTheSameUsers && isTheSameTokens) {
                result = int256(idx);
                break;
            }
        }
        return result;
    }

    function _cleanUp(uint256 hashedKey, uint256 matchItemIndex) private {
        Match[] memory _pendingMatches = _matchesByUser[hashedKey];
        if (_pendingMatches.length == 0) return;
        uint resultIdx = 0;
        for (uint idx = 0; idx < _pendingMatches.length - 1; idx++){
            if (idx == matchItemIndex) {
                // skip item and since this index we will take item under [idx + 1]
                ++resultIdx;
            } 
            if (resultIdx > _pendingMatches.length - 1) { 
                continue;
            }
            _matchesByUser[hashedKey][idx] = _pendingMatches[resultIdx];
            resultIdx++;
        }
        _matchesByUser[hashedKey].pop();
    }

    function _getValidHash(address userFirst, address userSecond) private view returns (uint256) {
        uint256 hashedFirst = _utils.hash(userFirst, userSecond);
        uint256 hashedSecond = _utils.hash(userSecond, userFirst);
        if (_matchesByUser[hashedFirst].length != 0) {
            return hashedFirst;
        } else if (_matchesByUser[hashedSecond].length != 0) {
            return hashedSecond;
        } else {
            revert("Can not find any match object for this users. Did you pass correct Match objects?");
        }
    }

    /*
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
    */    
}