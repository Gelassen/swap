// SPDX-License-Identifier: MIT
pragma solidity ^0.8.4;

import { Match } from "../contracts/structs/StructDeclaration.sol";

interface ISwapChain {

    /**
     * Register user automatically means he registers his offers minted 
     * over SwapValue.sol ERC721 implementation
     * 
     */
    function registerUser(address user) external;

    function usersInTotal() external view returns (uint256);
    
    function registerDemand(address user, string memory demand) external;

    function getDemandsByUser(address user) external view returns (string[] memory);

    /**
     * TODO:
     * Current use cases implies this method to be private
     */
    function showMatches(address user) external returns (Match[] memory);

    function swap(Match memory subj) external;

    function swapNoParams(address userWhoLooksForMatch) external;

    function approveSwap(Match memory subj) external; 
    
    function approveSwapNoParams() external;
}