// SPDX-License-Identifier: MIT

pragma solidity ^0.8.4;

import { User } from "./Model.sol";
import "../contracts/ISwapChain.sol";
import "../contracts/SwapValue.sol";
import { Utils } from "../contracts/Utils.sol";
import "hardhat/console.sol";

/**
 * ERC721 and ERC20 standarts already has links on tokens owned by address. 
 * Take a look on balanceOf(owner) and tokenOfOwnerByIndex(address owner, uint256 index)
 * https://docs.openzeppelin.com/contracts/3.x/api/token/erc721#IERC721Enumerable-tokenOfOwnerByIndex-address-uint256-
 * https://docs.openzeppelin.com/contracts/3.x/api/token/erc721#IERC721-balanceOf-address-
 * 
 * For things like instanceOf() and typeOf() use check by interface implementation
 * like described in this blog post https://kushgoyal.com/solidity-how-to-check-type-of-the-contract-erc-165/
 * 
 * 
 */
contract SwapChain is ISwapChain {

    // uint256 _usersCounter = 0;
    // mapping(address => SwapValue[]) _users;
    Utils _utils;
    address[] _users;
    address[] _nfts;
    mapping(string => address[]) _indexNfts;
    mapping(address => string[]) _demandsByUsers;

    
    constructor(address utilsAddr) {
        _utils = Utils(utilsAddr);
    }

    function registerUser(address user) public override {
        // require(_users[user] == [], "User should not be registered yet or have any tokens.");
        // _users[user] = [];
        // _usersCounter++;
        require(_isUserExist(user) != true, "User should not be registered yet.");
        _users.push(user);
    }

    function registerOffer(address user, address nft) external override returns (bool) { 
            //     // TODO add SwapValue based on nft address to the _users[user] 
    //     // require()
    //     // TODO add check user owns this nft
    //     // TODO add che
    //     // _users.push(SwapValue(nft));

    //     return false; // TODO complete me 
        return false;
    }

    function registerDemand(address user, string memory demand) external {
        console.log("[start] registerDemand()");
        // TODO check do we need to check is the user registered in system
        string[] memory demands = _demandsByUsers[user];
        bool isExist = false;
        for (uint idx = 0; idx < demands.length; idx++) {
            console.log(demands[idx], demand);
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
        console.log("[end] registerDemand()");
    }

    function usersInTotal() external view override returns (uint256) {
        return _users.length;
    }

    function getDemandsByUser(address user) external view override returns (string[] memory) {
        return _demandsByUsers[user];
    }

    function showMatches(address user) external override pure returns(address[] memory) {
        // require(_isUserExist(user) == true, "User should be registered first to view matches.");
        // require(_demandsByUsers[user].length != 0, "User should have at least one registered demand.");

        // address[] storage matches;
        // for (uint idx = 0; idx < _demandsByUsers[user].length; idx++) {
        //     string memory demand = _demandsByUsers[user][idx];
        //     if (_indexNfts[demand].length != 0) { _addToCollection(matches, _indexNfts[demand]); }
        // }
        // // address[] memory result = matches;
        // // return address[] memory addresss;
        return new address[](0);
    }

    function swap(address actorOne, address actorTwo, bytes32 actorOneOffer, bytes32 actorTwoOffer) external override returns (bool) {
        return false; // TODO complete me 
    }

    function _addToCollection(address[] storage result, address[] memory addrs) private returns (address[] memory) {
        for (uint idx = 0; idx < addrs.length; idx++) {
            result.push(addrs[idx]);
        }
        return result;
    }

    function _isUserExist(address userAddr) private view returns (bool) {
        for (uint idx = 0; idx < _users.length; idx++) {
            if (_users[idx] == userAddr) {
                return true;
            }
        }
        return false;
    }
}