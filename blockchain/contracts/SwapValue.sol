// SPDX-License-Identifier: MIT
pragma solidity ^0.8.4;

import "../contracts/openzeppelin/token/ERC721/ERC721.sol";
import "../contracts/openzeppelin/token/ERC721/extensions/ERC721URIStorage.sol";
import "../contracts/openzeppelin/access/Ownable.sol";
import "../contracts/openzeppelin/utils/Counters.sol";
import { Value } from "../contracts/structs/StructDeclaration.sol";
import { ISwapValue } from "../contracts/ISwapValue.sol";
// import "../contracts/hardhat/hardhat-core/console.sol";


/**
 * SwapValue extend ERC721 spec implementation by adding:
 *  - support extra metadata per tokenId
 *  - support tokens ids per user
 */
contract SwapValue is ERC721, ERC721URIStorage, Ownable, ISwapValue {
    using Counters for Counters.Counter;

    Counters.Counter private _tokenIdCounter;
    mapping(uint256 => Value) private _offerPerToken; 
    mapping(address => uint256[]) private _tokensIdsPerUser;
    // _tokensIdsPerUser does not need to have references on approve() and revokeApprove() addresses  

    constructor() ERC721("SwapValue", "SWV") {}

    /**
     * Modifier for function calls which should be performed only by token owner or
     * approved by him users or contracts.
     *   
     * @param tokenId Token id which belongs to owner or addresses within its approval list 
     */
    modifier onlyAuthorised(uint256 tokenId) {
        // console.log("msg.sender ", msg.sender);
        // console.log("ownerOf(tokenId) ", ownerOf(tokenId));
        // console.log("getApproved(tokenId) ", getApproved(tokenId));
        // console.log(ownerOf(tokenId) == msg.sender || getApproved(tokenId) == msg.sender);
        require(ownerOf(tokenId) == msg.sender 
            || getApproved(tokenId) == msg.sender
            || isApprovedForAll(ownerOf(tokenId), msg.sender), 
            "Only token owner or actor approved by owner is allowed to call this function.");
        _;
    }

    function _baseURI() internal pure override returns (string memory) {
        return "https://gelassen.github.io/blog/";
    }

    // TODO override mint function to support mint from whitelisted accounts

    function safeMint(
        address to, 
        Value calldata value, 
        string memory uri
    ) public onlyOwner {
        uint256 tokenId = _tokenIdCounter.current();
        _tokenIdCounter.increment();
        _safeMint(to, tokenId);
        _setTokenURI(tokenId, uri);
        _setOffer(value, tokenId);
        // console.log("Minted token id", tokenId);
    }

    function transferFrom(
        address from,
        address to,
        uint256 tokenId
    ) public virtual override {
        super.transferFrom(from, to, tokenId);
        _transferMetadata(from, to, tokenId); 
    }

    function burn(address owner, uint256 tokenId) public {
        _burn(tokenId);
        _burnMetadata(owner, tokenId);
    }

    function tokenURI(uint256 tokenId)
        public
        view
        override(ERC721, ERC721URIStorage)
        returns (string memory)
    {
        return super.tokenURI(tokenId);
    }

    function offer(uint256 tokenId) external view override returns(Value memory){
        require(_exists(tokenId), "This token does not exist.");
        return _offerPerToken[tokenId];
    }

    function getTokensIdsForUser(address user) public view override returns(uint256[] memory) {
        return _tokensIdsPerUser[user];
    }

    function makeTokenConsumed(uint256 tokenId) external override onlyAuthorised(tokenId) {
        _offerPerToken[tokenId]._isConsumed = true;
        _offerPerToken[tokenId]._lockedUntil = 0;
    }
 
    function lockToken(uint256 tokenId, uint256 timeOfLockEnds) external override onlyAuthorised(tokenId) {
        _offerPerToken[tokenId]._lockedUntil = timeOfLockEnds;
    }

    function _burn(uint256 tokenId) internal override(ERC721, ERC721URIStorage) {
        super._burn(tokenId);
    }

    function _burnMetadata(address owner, uint256 tokenId) private {
        uint256[] storage tokens = _tokensIdsPerUser[owner];
        for (uint256 idx = 0; idx < tokens.length; idx++) {
            if (tokens[idx] == tokenId) {
                _remove(idx, tokens);
            }
        }
        delete _offerPerToken[tokenId];
    }

    function _setOffer(Value calldata value, uint256 tokenId) internal {
        _offerPerToken[tokenId] = value;
        // console.log("_offerPerToken[tokenId]", tokenId);
    }

    function _safeMint(address to, uint256 tokenId) internal virtual override {
        super._safeMint(to, tokenId);
        _tokensIdsPerUser[to].push(tokenId);
    }

    // TODO refactor it to be part of some another class or data structure 
    function _remove(uint index, uint256[] storage array) private {
        if (index >= array.length) return;

        for (uint i = index; i<array.length-1; i++){
            array[i] = array[i+1];
        }
        array.pop();
    }

    function _transferMetadata(
        address from, 
        address to,
        uint256 tokenId
    ) private {
        uint256[] storage tokensByUser = _tokensIdsPerUser[from];
        for (uint idx = 0; idx < tokensByUser.length; idx++) {
            if (tokensByUser[idx] == tokenId) {
                _tokensIdsPerUser[to].push(tokenId);
                _remove(idx, tokensByUser);
            }
        }   
        _offerPerToken[tokenId]._isConsumed = true;
    }
}