// SPDX-License-Identifier: MIT
pragma solidity ^0.8.9;

struct User {
    address user;
    bytes32 name;
    bytes32[] offers;
    bytes32[] demands;
}

struct Match {
    address _userFirst;
    uint256 _valueOfFirstUser; // tokenId of SwapValue.sol owned by _userFirst
    address _userSecond;
    uint256 _valueOfSecondUser; // tokenId of SwapValue.sol owned by _userSecond
    bool _approvedByFirstUser;
    bool _approvedBySecondUser;
}

struct MatchDebug {
    string _userFirst;
    uint256 _valueOfFirstUser; // tokenId of SwapValue.sol owned by _userFirst
    string _userSecond;
    uint256 _valueOfSecondUser; // tokenId of SwapValue.sol owned by _userSecond
    bool _approvedByFirstUser;
    bool _approvedBySecondUser;
}

struct Value {
    string _offer;
    uint256 _availableSince;
    uint256 _availabilityEnd;
    bool _isConsumed;
    uint256 _lockedUntil;
}