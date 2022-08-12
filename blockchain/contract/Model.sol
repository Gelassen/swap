pragma solidity ^0.8.2;

struct User {
    address user;
    bytes32 name;
    bytes32[] offers;
    bytes32[] demands;
}