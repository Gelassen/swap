package com.example.wallet.debug.contract

interface Ownable {

    fun owner(): String

    /**
     * @dev Throws if the sender is not the owner.
     */
//    fun _checkOwner()

    /**
     * @dev Leaves the contract without owner. It will not be possible to call
     * `onlyOwner` functions anymore. Can only be called by the current owner.
     *
     * NOTE: Renouncing ownership will leave the contract without an owner,
     * thereby removing any functionality that is only available to the owner.
     */
    fun renounceOwnership()

    /**
     * @dev Transfers ownership of the contract to a new account (`newOwner`).
     * Can only be called by the current owner.
     */
    fun transferOwnership(newOwner: String)

    /**
     * @dev Transfers ownership of the contract to a new account (`newOwner`).
     * Internal function without access restriction.
     */
    // fun _transferOwnership(newOwner: String)
}