// SPDX-License-Identifier: MIT
pragma solidity ^0.8.4;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/Counters.sol";
import { Value } from "../contracts/StructDeclaration.sol";
import "hardhat/console.sol";

contract SwapValue is ERC721, ERC721URIStorage, Ownable {
    using Counters for Counters.Counter;

    Counters.Counter private _tokenIdCounter;
    mapping(uint256 => Value) private _offerPerToken; 

    constructor() ERC721("SwapValue", "SWV") {}

    function _baseURI() internal pure override returns (string memory) {
        return "https://gelassen.github.io/blog/";
    }

    // TODO override mint function to support mint from whitelisted accounts

    function safeMint(address to, Value calldata value, string memory uri) public onlyOwner {
        uint256 tokenId = _tokenIdCounter.current();
        _tokenIdCounter.increment();
        _safeMint(to, tokenId);
        _setTokenURI(tokenId, uri);
        _setOffer(value, tokenId);
        console.log("Minted token id", tokenId);
    }

    // The following functions are overrides required by Solidity.

    function _burn(uint256 tokenId) internal override(ERC721, ERC721URIStorage) {
        super._burn(tokenId);
    }

    function _setOffer(Value calldata value, uint256 tokenId) internal {
        _offerPerToken[tokenId] = value;
    }

    function tokenURI(uint256 tokenId)
        public
        view
        override(ERC721, ERC721URIStorage)
        returns (string memory)
    {
        return super.tokenURI(tokenId);
    }

    function offer(uint256 tokenId) public view returns(Value memory){
        return _offerPerToken[tokenId];
    }

    
}