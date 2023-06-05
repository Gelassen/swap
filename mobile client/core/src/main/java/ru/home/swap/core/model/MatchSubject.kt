package ru.home.swap.core.model

import com.google.gson.Gson
import java.math.BigInteger
// TODO verify SwapMatch {@link ru.home.swap.core.model.SwapMatch} could fully replace this class
data class MatchSubject(
    var uid: Long = 0L,
    val userFirst: String,
    val valueOfFirstUser: BigInteger, // tokenId of SwapValue.sol owned by _userFirst
    var userSecond: String,
    var valueOfSecondUser: BigInteger, // tokenId of SwapValue.sol owned by _userSecond
    var approvedByFirstUser: Boolean = false,
    var approvedBySecondUser: Boolean = false
) : IPayload {
    override fun toJson(): String {
        return Gson().toJson(this)
    }
}