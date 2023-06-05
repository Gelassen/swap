package ru.home.swap.wallet.storage.model

class Schema {

    object ChainTransaction {
        const val TABLE_NAME: String = "ChainTransaction"
        const val UID: String = "uid"
        const val TX_TYPE: String = "txType"
        const val PAYLOAD_AS_JSON: String = "payloadAsJsonString"
        const val STATUS: String = "status"
        const val DEFAULT_PAGE_SIZE = 20
    }

    object ServerMetadata {
        const val TABLE_NAME: String = "ServerTransactionMetadata"
        const val UID: String = "uid"
        const val REQUEST_TYPE: String = "requestType"
        const val PAYLOAD_AS_JSON = "payloadAsJsonString"
        const val TX_CHAIN_ID: String = "txChainId"
        const val STATUS: String = "status"
    }

    object ChainMatch {
        const val TABLE_NAME: String = "SwapMatch"
        const val UID: String = "uid"
    }

    object ChainService {
        const val TABLE_NAME: String = "SwapService"
        const val UID: String = "uid"
    }

    object Views {
        object ChainTxWithServerMetadata {
            const val VIEW_NAME: String = "ChainTxWithServerMetadata"
        }
    }
}