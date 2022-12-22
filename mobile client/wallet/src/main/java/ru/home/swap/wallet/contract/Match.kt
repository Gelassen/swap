package ru.home.swap.wallet.contract

import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class Match: DynamicStruct {
    val userFirst: String
    val valueOfFirstUser: BigInteger // tokenId of SwapValue.sol owned by _userFirst
    var userSecond: String
    var valueOfSecondUser: BigInteger // tokenId of SwapValue.sol owned by _userSecond
    var approvedByFirstUser: Boolean = false
    var approvedBySecondUser: Boolean = false

    constructor(
        userFirst: String,
        valueOfFirstUser: BigInteger,
        userSecond: String,
        valueOfSecondUser: BigInteger,
        approvedByFirstUser: Boolean,
        approvedBySecondUser: Boolean
    ) : super(
        Utf8String(userFirst),
        Uint256(valueOfFirstUser),
        Utf8String(userSecond),
        Uint256(valueOfSecondUser),
        Bool(approvedByFirstUser),
        Bool(approvedBySecondUser)
    ) {
        this.userFirst = userFirst
        this.valueOfFirstUser = valueOfFirstUser
        this.userSecond = userSecond
        this.valueOfSecondUser = valueOfSecondUser
        this.approvedByFirstUser = approvedByFirstUser
        this.approvedBySecondUser = approvedBySecondUser
    }

    constructor(
        userFirst: Utf8String,
        valueOfFirstUser: Uint256,
        userSecond: Utf8String,
        valueOfSecondUser: Uint256,
        approvedByFirstUser: Bool,
        approvedBySecondUser: Bool
    ) : super(userFirst, valueOfFirstUser, userSecond, valueOfSecondUser, approvedByFirstUser, approvedBySecondUser) {
        this.userFirst = userFirst.value
        this.valueOfFirstUser = valueOfFirstUser.value
        this.userSecond = userSecond.value
        this.valueOfSecondUser = valueOfSecondUser.value
        this.approvedByFirstUser = approvedByFirstUser.value
        this.approvedBySecondUser = approvedBySecondUser.value
    }

    override fun toString(): String {
        return "{ " +
                "\"userFirst\": \"${userFirst}\", " +
                "\"valueOfFirstUser\": \"${valueOfFirstUser}\", " +
                "\"userSecond\": \"${userSecond}\", " +
                "\"valueOfSecondUser\" : \"${valueOfSecondUser}\", " +
                "\"approvedByFirstUser\" : \"${approvedByFirstUser}\", " +
                "\"approvedBySecondUser\" : \"${approvedBySecondUser}\" " +
                "}"

    }


}