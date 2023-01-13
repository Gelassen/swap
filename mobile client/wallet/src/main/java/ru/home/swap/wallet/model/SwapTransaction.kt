package ru.home.swap.wallet.model

import com.google.gson.GsonBuilder
import ru.home.swap.wallet.contract.Match
import ru.home.swap.wallet.converters.MatchTypeAdapter
import ru.home.swap.wallet.storage.ChainTransactionEntity
import ru.home.swap.wallet.storage.TxStatus
import ru.home.swap.wallet.storage.TxType

data class SwapTransaction(
    override var uid: Long = 0,
    override var status: String = TxStatus.TX_PENDING,
    var match: Match
): ITransaction {
    override fun fromDomain(): ChainTransactionEntity {
        val gson = GsonBuilder()
            .registerTypeAdapter(Match::class.java, MatchTypeAdapter())
            .create()
        return ChainTransactionEntity(
            uid = this.uid,
            txType = TxType.TX_SWAP,
            payloadAsJson = gson.toJson(match, Match::class.java),
            status = this.status
        )
    }
}