// SPDX-License-Identifier: GPL-3.0
        
pragma solidity >=0.4.22 <0.9.0;

// This import is automatically injected by Remix
import "remix_tests.sol"; 

// This import is required to use custom transaction context
// Although it may fail compilation in 'Solidity Compiler' plugin
// But it will work fine in 'Solidity Unit Testing' plugin
import "remix_accounts.sol";
import "../contracts/QuasiDynamicArray.sol";

// File name has to end with '_test.sol', this file can contain more than one testSuite contracts
contract DynamicArraySuite {

    DynamicArray subject;

    /// 'beforeAll' runs before all other tests
    /// More special functions are: 'beforeEach', 'beforeAll', 'afterEach' & 'afterAll'
    function beforeAll() public {
        // <instantiate contract>
        subject = new DynamicArray();
        Assert.equal(uint(1), uint(1), "1 should be equal to 1");
    }

    function onAsPlainArray_atFirstInstance_returnsArraysWithDefaultSizeAndEmptySlots() public {
        uint defaultSizeConst = 20;
        
        address[] memory result = subject.asPlainArray(false);

        Assert.equal(result.length, defaultSizeConst, "Array size does not match default size.");
        Assert.equal(result[0], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[1], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[2], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[3], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[4], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[5], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[6], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[7], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[8], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[9], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[10], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[11], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[12], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[13], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[14], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[15], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[16], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[17], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[18], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
        Assert.equal(result[19], 0x0000000000000000000000000000000000000000, "Array item doesn't have default value.");
    }

    function onDropEmptySlots() public {
        address[] memory result = subject.dropEmptySlots();
        
        Assert.equal(result.length, 0, "Drop empty slots function doesn't work correctly.");
    }

    // function onAsPlainArray_atFirstInstanceWithDropEmptySlotsFlag_returnsZeroSizeArray() public {
    //     address[] memory result = subject.asPlainArray(true);

    //     Assert.equal(result.length, 0, "Array at the first instance has some value after empty slots have been droped.");
    // }

    // function onPush_initiallyEmptyState_arrayHasOneItem() public {
    //     address sampleValue = 0x0000000000000000000000000000000000000042; 

    //     subject.push(sampleValue);
        
    //     address[] memory result = subject.asPlainArray(true);
    //     Assert.equal(result.length, 1, "Array doesn't have expected single value.");
    //     Assert.equal(result[0], sampleValue, "Array the first value is not an address that has been pushed.");
    // }

    // function onPush_initiallyHasValue_arrayHasTwoItems() public {
    //     address sampleValue = 0x0000000000000000000000000000000000000042; 
    //     subject.push(0x0000000000000000000000000000000000000024);

    //     subject.push(sampleValue);

    //     address[] memory result = subject.asPlainArray(true);
    //     Assert.equal(result.length, 2, "Array doesn't have expected amount of items.");
    //     Assert.equal(result[1], sampleValue, "Array doesn't have pushed value at its expected index.");
    // }

    // function checkSuccess() public {
    //     // Use 'Assert' methods: https://remix-ide.readthedocs.io/en/latest/assert_library.html
    //     Assert.ok(2 == 2, 'should be true');
    //     Assert.greaterThan(uint(2), uint(1), "2 should be greater than to 1");
    //     Assert.lesserThan(uint(2), uint(3), "2 should be lesser than to 3");
    // }

    // function checkSuccess2() public pure returns (bool) {
    //     // Use the return value (true or false) to test the contract
    //     return true;
    // }
    
    // function checkFailure() public {
    //     Assert.notEqual(uint(1), uint(1), "1 should not be equal to 1");
    // }

    // /// Custom Transaction Context: https://remix-ide.readthedocs.io/en/latest/unittesting.html#customization
    // /// #sender: account-1
    // /// #value: 100
    // function checkSenderAndValue() public payable {
    //     // account index varies 0-9, value is in wei
    //     Assert.equal(msg.sender, TestsAccounts.getAccount(1), "Invalid sender");
    //     Assert.equal(msg.value, 100, "Invalid value");
    // }
}
    