// SPDX-License-Identifier: MIT

pragma solidity ^0.8.4;

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

    address[] _data;

    constructor() {
        _data = new address[](DEFAULT_SIZE);
        _removeEmptySlots();
    }

    function asPlainArray(bool dropEmptySlots) external view returns(address[] memory) {
        return _data;
    } 

    function push(address addr) external {
        require(addr != EMPTY_ADDRESS, "0x0000000000000000000000000000000000000000 is reserved as empty address. Did you pass correct address?");
        _data.push(addr);
    }

    function get(uint index) external view returns(address) {
        require(index < _data.length && _data.length != 0, "Index is out of array range.");
        return _data[index];
    }

    function length() external view returns(uint) {
        return _data.length;
    }

    function dropEmptySlots() public returns(address[] memory) {
        int index = _getFirstEmptySlotIndex();
        if (index == NO_VALUE) { return _data; }
        address[] memory result = _data;
        for (uint idx = uint(index); idx < _data.length; idx++) { 
            uint next = idx + 1;
            if (next < _data.length) {
                result[idx] = _data[next];
            }
        }
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
        require(_data.length != 0, "You can not remove any value from the empty array.");
        require(_data.length > index, "You can not remove item with index out of this array range.");
        // require(index >= data.length, "Passed index is out of array range."); // TODO investigate why does it cause transaction to be reverted 
        if (_data.length == 1) { 
            _data = new address[](0);
        } else {
        for (uint idx = index; idx < _data.length; idx++) {
            uint next = idx + 1;
            if (next < _data.length) { _data[idx] = _data[next]; }
        }
        _data.pop(); // we shift the array and have to pop up a redundant item 
        }
    }

    function _dropEmptySlots() private view returns(address[] memory) {
        int index = _getFirstEmptySlotIndex();
        if (index == NO_VALUE) { return _data; }
        
        // TODO create a new array with size of index
        uint emptySlotsCounter = 0;
        for (uint idx = 0; idx < _data.length; idx++) {
            if (_data[idx] == EMPTY_ADDRESS) { emptySlotsCounter++; }
        }
        address[] memory result = new address[](_data.length - emptySlotsCounter);
        uint resultIdx = 0;
        for (uint idx = 0; idx < _data.length; idx++) {
            if (_data[idx] != EMPTY_ADDRESS) {
                result[resultIdx] = _data[idx];
                resultIdx++;
            }
        }

        return result;
    }

    function _getFirstEmptySlotIndex() private view returns(int) {
        int result = NO_VALUE;
        for (uint idx = 0; idx < DEFAULT_SIZE; idx++) { 
            if (_data[idx] == EMPTY_ADDRESS) { 
                result = int(idx); 
                break;
            }
        }
        return result;
    }

    function _removeEmptySlots() private {
        while(_data.length != 0) {
            uint lastIndex = _data.length - 1;
            if (_data[lastIndex] == EMPTY_ADDRESS) { _data.pop(); }
        }
    }

}