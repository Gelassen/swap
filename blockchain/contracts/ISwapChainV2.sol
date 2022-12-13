// SPDX-License-Identifier: MIT
pragma solidity ^0.8.4;

import { Match } from "../contracts/structs/StructDeclaration.sol";

interface ISwapChainV2 {
    function registerUser(address user) external;
    function swap(Match memory subj) external;
    function approveSwap(Match memory subj) external;
}