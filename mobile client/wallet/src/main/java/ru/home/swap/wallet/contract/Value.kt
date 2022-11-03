package ru.home.swap.wallet.contract

import org.json.JSONObject
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class Value: DynamicStruct {

    lateinit var offer: String
    lateinit var availableSince: BigInteger
    lateinit var availabilityEnd: BigInteger
    var isConsumed: Boolean = false
    lateinit var lockedUntil: BigInteger

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

private object ValueConst {
    const val OFFER_FIELD = "offer"
    const val AVAILABLE_SINCE = "availableSince"
    const val AVAILABLE_END = "availableEnd"
    const val IS_CONSUMED = "isConsumed"
    const val LOCKED_UNTIL = "lockedUntil"
}

fun Value.convertToJson(): String {
    val result = JSONObject()
    result.put(ValueConst.OFFER_FIELD, offer)
    result.put(ValueConst.AVAILABLE_SINCE, availableSince.toLong())
    result.put(ValueConst.AVAILABLE_END, availabilityEnd.toLong())
    result.put(ValueConst.IS_CONSUMED, isConsumed)
    result.put(ValueConst.LOCKED_UNTIL, lockedUntil)
    return result.toString()
}

fun Value.fromJson(json: String): Value {
    val subj = JSONObject(json)
    return Value(
        subj.optString(ValueConst.OFFER_FIELD),
        BigInteger.valueOf(subj.optLong(ValueConst.AVAILABLE_SINCE)),
        BigInteger.valueOf(subj.optLong(ValueConst.AVAILABLE_END)),
        subj.optBoolean(ValueConst.IS_CONSUMED),
        BigInteger.valueOf(subj.optLong(ValueConst.LOCKED_UNTIL))
    )
}