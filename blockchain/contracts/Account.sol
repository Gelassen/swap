pragma solidity ^0.8.4;

import "../contracts/IAccount.sol";
import "@openzeppelin/contracts/token/ERC721/ERC721.sol";

/**
 * Redundant legacy code
 */
contract Account is IAccount {

    /// fallback function
    fallback () external { /* no op */}

    string _name;
    ERC721[] _offers;
    string[] _demands;

    function issueOffer() external override returns (uint256) {
        // TODO complete me 
        return 0;
    }

    function issueDemand(string memory name) external override returns (uint256) {
        // TODO complete me 
        return 0;
    }

    /* function getAllHistoryOfExchanges() external view returns(Value[] history); */

    function confirmSwap() external override returns (bool) {
        // TODO complete me 
        return false;
    } 
    
}