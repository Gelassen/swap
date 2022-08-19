// SPDX-License-Identifier: MIT
pragma solidity ^0.8.4;

contract Utils {

    function stringsEquals(string memory s1, string memory s2) public pure returns (bool) {
        bytes memory b1 = bytes(s1);
        bytes memory b2 = bytes(s2);
        uint256 l1 = b1.length;
        if (l1 != b2.length) return false;
        for (uint256 i=0; i<l1; i++) {
            if (b1[i] != b2[i]) return false;
        }
        return true;
    }

    function stringsEqualsHardWay(string memory a, string memory b) public pure returns (bool) {
        return (keccak256(abi.encodePacked((a))) == keccak256(abi.encodePacked((b))));
    }

    function concatenate(string memory _In1, string memory _In2) public view returns(string memory){
        return string(abi.encodePacked(_In1, _In2));
    }

    function getMaxUint() public pure returns(uint256){
        unchecked{
            return uint256(0) - 1;
        }
    }

}