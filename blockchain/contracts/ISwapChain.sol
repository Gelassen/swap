pragma solidity ^0.8.4;

interface ISwapChain {

    function registerUser(address user) external returns (bool);

    function registerOffer(address user) external returns (bool);

    function usersInTotal() external view returns (uint256);

    function showMatches(address user) external pure returns(address[]);

    function swap(address actorOne, address actorTwo, bytes32 actorOneOffer, bytes32 actorTwoOffer) external returns (bool);
    
}