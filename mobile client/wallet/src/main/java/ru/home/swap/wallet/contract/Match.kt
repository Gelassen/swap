package ru.home.swap.wallet.contract

import org.json.JSONObject
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import ru.home.swap.core.model.MatchSubject
import ru.home.swap.wallet.contract.MatchConst.APPROVED_BY_FIRST_USER
import ru.home.swap.wallet.contract.MatchConst.APPROVED_BY_SECOND_USER
import ru.home.swap.wallet.contract.MatchConst.USER_FIRST
import ru.home.swap.wallet.contract.MatchConst.USER_SECOND
import ru.home.swap.wallet.contract.MatchConst.VALUE_OF_FIRST_USER
import ru.home.swap.wallet.contract.MatchConst.VALUE_OF_SECOND_USER
import java.math.BigInteger

class Match: DynamicStruct {
    val userFirst: String
    val valueOfFirstUser: BigInteger // tokenId of SwapValue.sol owned by _userFirst
    var userSecond: String
    var valueOfSecondUser: BigInteger // tokenId of SwapValue.sol owned by _userSecond
    var approvedByFirstUser: Boolean = false
    var approvedBySecondUser: Boolean = false

    constructor(
        userFirst: String = "",
        valueOfFirstUser: BigInteger = BigInteger.valueOf(0),
        userSecond: String = "",
        valueOfSecondUser: BigInteger = BigInteger.valueOf(0),
        approvedByFirstUser: Boolean = false,
        approvedBySecondUser: Boolean = false
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

private object MatchConst {
    const val USER_FIRST = "userFirst"
    const val VALUE_OF_FIRST_USER = "valueOfFirstUser"
    const val USER_SECOND = "userSecond"
    const val VALUE_OF_SECOND_USER = "valueOfSecondUser"
    const val APPROVED_BY_FIRST_USER = "approvedByFirstUser"
    const val APPROVED_BY_SECOND_USER = "approvedBySecondUser"
}

fun Match.toMatchSubject(): MatchSubject {
    return MatchSubject(
        userFirst = this.userFirst,
        valueOfFirstUser = this.valueOfFirstUser,
        userSecond = this.userSecond,
        valueOfSecondUser = this.valueOfSecondUser,
        approvedByFirstUser = this.approvedByFirstUser,
        approvedBySecondUser = this.approvedBySecondUser
    )
}

fun Match.convertToJson(): String {
    val result = JSONObject()
    result.put(USER_FIRST, this.userFirst)
    result.put(USER_SECOND, this.userSecond)
    result.put(VALUE_OF_FIRST_USER, this.valueOfFirstUser)
    result.put(VALUE_OF_SECOND_USER, this.valueOfSecondUser)
    result.put(APPROVED_BY_FIRST_USER, this.approvedByFirstUser)
    result.put(APPROVED_BY_SECOND_USER, this.approvedBySecondUser)
    return result.toString()
}

fun Match.fromJson(json: String): Match {
    val obj = JSONObject(json)
    return Match(
        userFirst = obj.getString(USER_FIRST),
        valueOfFirstUser = BigInteger.valueOf(obj.getLong(VALUE_OF_FIRST_USER)),
        userSecond = obj.getString(USER_SECOND),
        valueOfSecondUser = BigInteger.valueOf(obj.getLong(VALUE_OF_SECOND_USER)),
        approvedByFirstUser = obj.getBoolean(APPROVED_BY_FIRST_USER),
        approvedBySecondUser = obj.getBoolean(APPROVED_BY_SECOND_USER)
    )
}