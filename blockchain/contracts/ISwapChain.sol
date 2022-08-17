// SPDX-License-Identifier: MIT
pragma solidity ^0.8.4;

interface ISwapChain {

    function registerUser(address user) external;

    function registerOffer(address user, address nft) external returns (bool);

    function usersInTotal() external view returns (uint256);
    
    function registerDemand(address user, string memory demand) external;

    function getDemandsByUser(address user) external view returns (string[] memory);

    function showMatches(address user) external pure returns (address[] memory);

    function swap(address actorOne, address actorTwo, bytes32 actorOneOffer, bytes32 actorTwoOffer) external returns (bool);
    
}