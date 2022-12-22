// SPDX-License-Identifier: MIT
pragma solidity ^0.8.4;

import { Match, MatchDebug } from "../contracts/structs/StructDeclaration.sol";

interface ISwapChainV2 {
    function registerUser(address user) external;
    function getUsers() external returns (address[] memory);
    function swap(Match calldata subj) external;
    function approveSwap(Match calldata subj) external;
    /**
     * We have to pass address fields out of struct because struct with address inside is not
     * supported by web3j yet. Call from js client side works fine. 
     */
    function approveSwap(address firstUser, address secondUser, MatchDebug calldata subj) external;
}