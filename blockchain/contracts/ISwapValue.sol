// SPDX-License-Identifier: MIT
pragma solidity ^0.8.4;

import { Value } from "../contracts/structs/StructDeclaration.sol";

interface ISwapValue {

    function offer(uint256 tokenId) external view returns(Value memory);

    function getTokensIdsForUser(address user) external view returns(uint256[] memory);

    function makeTokenConsumed(uint256 tokenId) external;

    function lockToken(uint256 tokenId, uint256 timeOfLockEnds) external;

}
