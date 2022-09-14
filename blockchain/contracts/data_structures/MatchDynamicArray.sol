// SPDX-License-Identifier: MIT

pragma solidity ^0.8.4;

import { Match } from "../../contracts/structs/StructDeclaration.sol";
import "hardhat/console.sol";

contract MatchDynamicArray {

    uint private DEFAULT_SIZE = 20;
    // Match[]
    // TODO the idea is to pass array into this contract and do all necessary extra 
    // operations which is better to encapsulate in the separate class
    // Compare with creation a separate contract for dynamic data structure array
    // utils is the better fit for current aims (grows by demand)

    Match[] _subject;

    function push(Match memory obj) public {
        _subject.push(obj);
    }

    function getByIndex(uint idx) public view returns(Match memory) {
        require(_subject.length != 0, "You can not request data when array is empty.");
        require(idx < _subject.length, "You can not request data for out of array bounds index." );

        return _subject[idx];
    }

    function clear() public {
        while(_subject.length != 0) {
            _subject.pop();
        }
    }

    function getLength() public view returns(uint) {
        return _subject.length;
    }

    function getArray() public view returns(Match[] memory) {
        return _subject;
    }
}