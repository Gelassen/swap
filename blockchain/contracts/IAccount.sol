pragma solidity ^0.8.4;

/**
 * Redundant legacy code
 */

/**
    Why do we need blockchain for this task? 

    a. To make exchange of goods & services safe for all parties (both 
    parties confirms they had received goods & services offline)

    b. To limit fraud (existing transactions proofs credibility of user)

    c. Exchange is made on the platform at the first place, exchange in 
    the real world goes after this. Consider to have 2nd confirmation of 
    exchange in real world to reduce cases of fraud 

    1. Use etherscan API to call transactions history, otherwise you have to
    iterate over bunch of blocks to collect all requested data. 

    https://ethereum.stackexchange.com/questions/88274/how-to-get-list-of-transactions-history-about-specific-contract-address-using-we

    2. Transaction by itself is not engough, you have to check its status as 
    it mights end up with error -- to be not valid

    https://ethereum.stackexchange.com/questions/16112/how-to-obtain-all-transaction-of-a-contract/16113#16113

    3. Watch out of incoming message calls (what used to be called internal 
    transactions). They have a different logic behind their storage. 
 */
interface IAccount {

    function issueOffer() external returns (uint256);

    function issueDemand(string memory name) external returns (uint256);

    /* function getAllHistoryOfExchanges() external view returns(Value[] history); */

    function confirmSwap() external returns  (bool); 

}