// SPDX-License-Identifier: MIT

pragma solidity ^0.8.4;

import "hardhat/console.sol";

/**
    Pushing into non-initialized array or pass it as a parameter into the function
    do not pass remix linter check. We have to work with initialized arrays and 
    this wrapper around it. 

    TODO: is enough initialize data structure at the first time or it should be 
    dynamically resized like new address[](data.length + DEFAULT_SIZE); when 
    new item reach the upper boundary? 
*/
contract DynamicArray {

    int private NO_VALUE = -1;
    uint public DEFAULT_SIZE = 20;
    address public EMPTY_ADDRESS = 0x0000000000000000000000000000000000000000;

    address[] data;

    constructor() {
        console.log("[start] DynamicArray constructor");
        data = new address[](DEFAULT_SIZE);
        console.log("[end] DynamicArray constructor");
    }

    function asPlainArray(bool dropEmptySlots) external view returns(address[] memory) {
        address[] memory result;
        if (dropEmptySlots) { 
            result = _dropEmptySlots(); 
        } else { 
            result = data;
        }
        return result;
    } 

    function push(address addr) external {
        require(addr != EMPTY_ADDRESS, "0x0000000000000000000000000000000000000000 is reserved as empty address. Did you pass correct address?");
        int idx = _getFirstEmptySlotIndex();
        if (idx == NO_VALUE) {
            data.push(addr);
        } else {
            data[uint256(idx)] = addr;
        }
    }

    function get(uint index) external view returns(address) {
        require(index < data.length && data.length != 0, "Index is out of array range.");
        return data[index];
    }

    function length() external view returns(uint) {
        return data.length;
    }

    function dropEmptySlots() public returns(address[] memory) {
        console.log("[start] dropEmptySlots");
        int index = _getFirstEmptySlotIndex();
        console.log("_getFirstEmptySlotIndex() has been called");
        if (index == NO_VALUE) { return data; }
        address[] memory result = data;
        for (uint idx = uint(index); idx < data.length; idx++) { 
            uint next = idx + 1;
            if (next < data.length) {
                result[idx] = data[next];
            }
        }
        console.log("[end] dropEmptySlots");
        return result;
    }

    /**
        Function is aimed to delete item by its index into the array. However, 
        it would not work in loop: each removeByIndex(index) call would change 
        the size and order of origin array, but each next index passed into 
        the function would reffer to old copy of array. 

        delete data[idx] hasn't been supported since 0.6.0. Remove array item
        by index requires its own implementation
    */
    function removeByIndex(uint index) public {
        console.log("[start] removeByIndex()");
        console.logUint(index);
        console.logUint(data.length);
        // require(index >= data.length, "Passed index is out of array range."); // TODO investigate why does it cause transaction to be reverted 
        if (data.length == 1) { 
            console.log("data = new address[](0) is going to call");
            data = new address[](0);
            console.log("data = new address[](0) has been called."); 
        }

        console.log("removeByIndex()::loop start");
        for (uint idx = index; idx < data.length; idx++) {
            uint next = idx + 1;
            if (next < data.length) { data[idx] = data[next]; }
        }
        data.pop(); // we shift the array and have to pop up a redundant item 
        console.log("[end] removeByIndex()");
    }

    function _dropEmptySlots() private view returns(address[] memory) {
        console.log("[start] _dropEmptySlots");
        int index = _getFirstEmptySlotIndex();
        console.log('_getFirstEmptySlotIndex() has been called with returned index $s', uint(index));
        if (index == NO_VALUE) { return data; }
        
        // TODO create a new array with size of index
        uint emptySlotsCounter = 0;
        for (uint idx = 0; idx < data.length; idx++) {
            if (data[idx] == EMPTY_ADDRESS) { emptySlotsCounter++; }
        }
        address[] memory result = new address[](data.length - emptySlotsCounter);
        uint resultIdx = 0;
        for (uint idx = 0; idx < data.length; idx++) {
            if (data[idx] != EMPTY_ADDRESS) {
                result[resultIdx] = data[idx];
                resultIdx++;
            }
        }

        // for (uint idx = 0; idx < uint(index); idx++) { 
        //     uint next = idx + 1;
        //     if (next < data.length) {
        //         result[idx] = data[next];
        //     }
        // }
        console.log("Result with all droped empty slots has size $s", result.length);
        console.log("[end] _dropEmptySlots");
        return result;
    }

    function _getFirstEmptySlotIndex() private view returns(int) {
        int result = NO_VALUE;
        for (uint idx = 0; idx < DEFAULT_SIZE; idx++) { 
            console.log('data[idx] == EMPTY_ADDRESS $s', data[idx] == EMPTY_ADDRESS);
            console.log('data[idx] %s', data[idx]);
            if (data[idx] == EMPTY_ADDRESS) { 
                result = int(idx); 
                break;
            }
        }
        return result;
    }

}