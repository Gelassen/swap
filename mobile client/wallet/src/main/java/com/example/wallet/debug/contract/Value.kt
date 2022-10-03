package com.example.wallet.debug.contract

import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class Value: DynamicStruct {

    private lateinit var offer: String
    private lateinit var availableSince: BigInteger
    private lateinit var availabilityEnd: BigInteger
    private var isConsumed: Boolean = false
    private lateinit var lockedUntil: BigInteger

    constructor(
        offer: String,
        availableSince: BigInteger,
        availabilityEnd: BigInteger,
        isConsumed: Boolean,
        lockedUntil: BigInteger
    ) : super(
        Utf8String(offer), Uint256(availableSince),
        Uint256(availabilityEnd), Bool(isConsumed),
        Uint256(lockedUntil)
    ) {
        this.offer = offer
        this.availableSince = availableSince
        this.availabilityEnd = availabilityEnd
        this.isConsumed = isConsumed
        this.lockedUntil = lockedUntil
    }

    constructor(
        offer: Utf8String,
        availableSince: Uint256,
        availabilityEnd: Uint256,
        isConsumed: Bool,
        lockedUntil: Uint256
    ) : super(offer, availableSince, availabilityEnd, isConsumed, lockedUntil) {
        this.offer = offer.value
        this.availableSince = availableSince.value
        this.availabilityEnd = availabilityEnd.value
        this.isConsumed = isConsumed.value
        this.lockedUntil = lockedUntil.value
    }

}