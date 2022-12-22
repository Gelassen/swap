// SPDX-License-Identifier: MIT
pragma solidity ^0.8.4;

import { User, Match, MatchDebug } from "../contracts/structs/StructDeclaration.sol";
import "../contracts/ISwapChainV2.sol";
import "../contracts/SwapValue.sol";
import { Utils } from "../contracts/utils/Utils.sol";
// import "../contracts/hardhat/hardhat-core/console.sol";

contract SwapChainV2Debug is ISwapChainV2 {

    /* 
        In case of match by address relationship for two addresses we have 
        two the same match objects which should be changed simultaneously => 
        use hash from two addresses as a key
    */
    
    int8 constant private NOT_VALID = -1; 

    SwapValue _swapValueContract;
    Utils _utils;
    address[] _users;
    mapping(uint256 => Match[]) _matchesByUser; 

    constructor(
        address utilsAddr, 
        address valueToken) {
        _utils = Utils(utilsAddr);
        _swapValueContract = SwapValue(valueToken);
    }

    /**
     * We have to pass msg.sender of origin function caller as a parameter as there is a 
     * known case when this function is called by the contract which acts as a proxy. An 
     * origin caller is not visible which opens a security issue for anyone can call 
     * 'authorised only' and 'match object owner' functions.    
     */
    modifier callerIsRegisteredUser(address originMsgSender, address userFirst, address userSecond) {
        require(originMsgSender == userFirst || originMsgSender == userSecond, "Caller should be an one of the users defined in the passed match object.");
        _;
    }

    modifier userExists(address userFirst, address userSecond) {
        require(_isUserExist(userFirst) == true, "The first user should be registered.");
        require(_isUserExist(userSecond) == true, "The second user should be registered.");
        _;
    }

    modifier bothHaveValidTokens(Match memory subj) {
        require(_swapValueContract.offer(subj._valueOfFirstUser)._isConsumed != true 
                    && _swapValueContract.offer(subj._valueOfSecondUser)._isConsumed != true,
                    "Tokens should not be already consumed.");
        require(block.timestamp < _swapValueContract.offer(subj._valueOfFirstUser)._availabilityEnd 
                    && block.timestamp < _swapValueContract.offer(subj._valueOfSecondUser)._availabilityEnd,
                    "One or both tokens has been expired.");  
        _;
    }

    function registerUser(address user) public override {
        require(_isUserExist(user) != true, "User already registered.");
        _users.push(user);
    }

    function getUsers() public view override returns (address[] memory) {
        return _users;
    }
    
    function swap(Match calldata subj) public override {
        _swap(msg.sender, subj);
    }

    function swap(address firstUser, address secondUser, MatchDebug calldata subj) public {
        Match memory matchParam;
        matchParam._userFirst = firstUser;
        matchParam._userSecond = secondUser;
        matchParam._valueOfFirstUser = subj._valueOfFirstUser;
        matchParam._valueOfSecondUser = subj._valueOfSecondUser;
        matchParam._approvedByFirstUser = false;
        matchParam._approvedBySecondUser = false;
        _swap(msg.sender, matchParam);
    }

    function approveSwap(Match calldata subj) public override {
        _approveSwap(msg.sender, subj);
    }

    function approveSwap(address firstUser, address secondUser, MatchDebug calldata subj) public {
        Match memory matchParam;
        matchParam._userFirst = firstUser;
        matchParam._userSecond = secondUser;
        matchParam._valueOfFirstUser = subj._valueOfFirstUser;
        matchParam._valueOfSecondUser = subj._valueOfSecondUser;
        matchParam._approvedByFirstUser = false;
        matchParam._approvedBySecondUser = false;
        _approveSwap(msg.sender, matchParam);
    }

    function _approveSwap(address msgSender, Match memory subj) private 
        callerIsRegisteredUser(msgSender, subj._userFirst, subj._userSecond)
        userExists(subj._userFirst, subj._userSecond)
        bothHaveValidTokens(subj) 
        {
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
        if (matchItemIndex == NOT_VALID) {
            _addNewMatch(subj);
            matchItemIndex = _getMatchItemIndex(subj);
            require(matchItemIndex != NOT_VALID, "Can not add this new match object into storage. Check _getMatchItemIndex() function.");
        }

        int256 hashedKey = _getValidHash(subj._userFirst, subj._userSecond);
        require(hashedKey != NOT_VALID, "Can not add match object. Do yours keys can be hashed? ");

        Match memory item = _matchesByUser[uint256(hashedKey)][uint256(matchItemIndex)]; // we have to use this version instead of subj because users and its values might have changed order
        require(item._approvedByFirstUser == false || item._approvedBySecondUser == false, "This match object already approved by both users.");

        if (msg.sender == item._userFirst) {
            require(item._approvedByFirstUser == false, "Match is already approved by this, first, user.");
            _matchesByUser[uint256(hashedKey)][uint256(matchItemIndex)]._approvedByFirstUser = true;
        } else if (msg.sender == item._userSecond) {
            require(item._approvedBySecondUser == false, "Match is already approved by this, second, user.");
            _matchesByUser[uint256(hashedKey)][uint256(matchItemIndex)]._approvedBySecondUser = true;
        }
    }

    function _swap(address msgSender, Match memory subj) private 
        callerIsRegisteredUser(msgSender, subj._userFirst, subj._userSecond)
        userExists(subj._userFirst, subj._userSecond)
        bothHaveValidTokens(subj) 
        {
        int256 matchItemIndex = _getMatchItemIndex(subj);
        require(matchItemIndex != NOT_VALID, "Match item is not found. Did you pass correct match object?");

        int256 hashedKey = _getValidHash(subj._userFirst, subj._userSecond);
        require(hashedKey != NOT_VALID, "Could not find a match. Do you have it in storage?");            

        Match memory item = _matchesByUser[uint256(hashedKey)][uint256(matchItemIndex)];
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
            
        _cleanUp(uint256(hashedKey), uint256(matchItemIndex));
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
        int256 result = NOT_VALID;
        int256 hashedKey = _getValidHash(subj._userFirst, subj._userSecond);
        if (hashedKey == NOT_VALID) {
            return result;
        }

        Match[] memory matches = _matchesByUser[uint256(hashedKey)];
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

    function _getValidHash(address userFirst, address userSecond) private view returns (int256) {
        uint256 hashedFirst = _utils.hash(userFirst, userSecond);
        uint256 hashedSecond = _utils.hash(userSecond, userFirst);
        if (_matchesByUser[hashedFirst].length != 0) {
            return int256(hashedFirst);
        } else if (_matchesByUser[hashedSecond].length != 0) {
            return int256(hashedSecond);
        } else {
            return NOT_VALID;
        }
    }

    function _addNewMatch(Match memory subj) private {
        uint256 hashedKey = _utils.hash(subj._userFirst, subj._userSecond);
        subj._approvedByFirstUser = false;
        subj._approvedBySecondUser = false;
        _matchesByUser[hashedKey].push(subj);
    }
 
}