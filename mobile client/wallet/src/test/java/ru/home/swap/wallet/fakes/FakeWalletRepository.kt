package ru.home.swap.wallet.fakes

import com.google.gson.Gson
import ru.home.swap.wallet.contract.SwapValue
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.repository.IWalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.web3j.protocol.core.methods.response.TransactionReceipt
import ru.home.swap.core.network.Response
import ru.home.swap.wallet.contract.Match
import ru.home.swap.wallet.model.Token
import java.math.BigInteger

class FakeWalletRepository: IWalletRepository {

    /* omit private */ val swapValueResponse: SwapValueResponse = SwapValueResponse()

    override fun balanceOf(owner: String): Flow<Response<BigInteger>> {
        return flow {
            val response = swapValueResponse.getBalanceResponse()
            emit(response)
        }
    }

    override fun mintTokenAsFlow(
        to: String,
        value: Value,
        uri: String
    ): Flow<Response<TransactionReceipt>> {
        TODO("Not yet implemented")
    }

    override suspend fun mintToken(
        to: String,
        value: Value,
        uri: String
    ): Response<TransactionReceipt> {
        return swapValueResponse.getMintTokenResponse()
    }

    override fun getTokensNotConsumedAndBelongingToMe(account: String): Flow<SwapValue.TransferEventResponse> {
        TODO("Not yet implemented")
    }

    override fun getTransferEvents(): Flow<SwapValue.TransferEventResponse> {
        TODO("Not yet implemented")
    }

    override fun getOffer(tokenId: String): Flow<Response<Value>> {
        TODO("Not yet implemented")
    }

    override fun registerUserOnSwapMarketAsFlow(userWalletAddress: String): Flow<Response<TransactionReceipt>> {
        TODO("Not yet implemented")
    }

    override suspend fun registerUserOnSwapMarket(userWalletAddress: String): Response<TransactionReceipt> {
        return swapValueResponse.getRegisterUserResponse()
    }

    override suspend fun approveTokenManager(
        operator: String,
        approved: Boolean
    ): Response<TransactionReceipt> {
        TODO("Not yet implemented")
    }

    override fun approveTokenManagerAsFlow(
        operator: String,
        approved: Boolean
    ): Flow<Response<TransactionReceipt>> {
        TODO("Not yet implemented")
    }

    override fun approveSwap(matchSubj: Match): Flow<Response<TransactionReceipt>> {
        TODO("Not yet implemented")
    }

    override fun registerDemand(
        userWalletAddress: String,
        demand: String
    ): Flow<Response<TransactionReceipt>> {
        TODO("Not yet implemented")
    }

    override fun getTokenIdsForUser(userWalletAddress: String): Flow<Response<List<*>>> {
        TODO("Not yet implemented")
    }

    override fun getTokenIdsWithValues(
        userWalletAddress: String,
        withConsumed: Boolean
    ): Flow<Response<List<Token>>> {
        TODO("Not yet implemented")
    }

    override fun swap(subj: Match): Flow<Response<TransactionReceipt>> {
        TODO("Not yet implemented")
    }

    fun setPositiveBalanceOfResponse() {
        swapValueResponse.setPositiveBalance()
    }

    class SwapValueResponse {

        private var balanceResponse: Response<BigInteger> = getDefaultBalanceResponse()
        private var mintTokenResponse: Response<TransactionReceipt> = getDefaultMintTokenResponse()
        private var registerUserResponse: Response<TransactionReceipt> = getDefaultRegisterUserResponse()
        private var approveTokenManagerResponse: Response<TransactionReceipt> = getDefaultApproveTokenManager()

        // START BLOCK: balance

        fun getBalanceResponse(): Response<BigInteger> {
            return balanceResponse
        }

        fun setPositiveBalance() {
            val testValue = BigInteger.valueOf(42)
            val response = Response.Data(testValue)
            this.balanceResponse = response
        }

        // END BLOCK

        // START BLOCK: mint token

        fun getMintTokenResponse(): Response<TransactionReceipt> {
            return mintTokenResponse
        }

        fun setPositiveMintTokenResponse() {
            val txReceipt: TransactionReceipt = Gson().fromJson(MINT_TOKEN_OK_RESPONSE, TransactionReceipt::class.java)
            val response: Response.Data<TransactionReceipt> = Response.Data(txReceipt)
            this.mintTokenResponse = response
        }

        fun setNegativeMintTokenResponse() {
            val txReceipt: TransactionReceipt = Gson().fromJson(MINT_TOKEN_NEGATIVE_RESPONSE, TransactionReceipt::class.java)
            val response: Response.Data<TransactionReceipt> = Response.Data(txReceipt)
            this.mintTokenResponse = response;
        }

        fun setErrorMintTokenResponse() {
            val txReceipt: TransactionReceipt = Gson().fromJson(MINT_TOKEN_NEGATIVE_RESPONSE, TransactionReceipt::class.java)
            val response = Response.Error.Message(txReceipt.revertReason)
            this.mintTokenResponse = response;
        }

        fun setExceptionErrorMintTokenResponse() {
            val txReceipt: TransactionReceipt = Gson().fromJson(MINT_TOKEN_NEGATIVE_RESPONSE, TransactionReceipt::class.java)
            val response = Response.Error.Exception(RuntimeException(txReceipt.revertReason))
            this.mintTokenResponse = response;
        }

        // END BLOCK

        // START BLOCK: register user

        fun getRegisterUserResponse(): Response<TransactionReceipt> {
            return registerUserResponse
        }

        fun setPositiveRegisterUserResponse() {
            val txReceipt: TransactionReceipt = Gson().fromJson(REGISTER_USER_OK_RESPONSE, TransactionReceipt::class.java)
            val response: Response.Data<TransactionReceipt> = Response.Data(txReceipt)
            this.registerUserResponse = response
        }

        fun setNegativeRegisterUserResponse() {
            val txReceipt: TransactionReceipt = Gson().fromJson(REGISTER_USER_NEGATIVE_RESPONSE, TransactionReceipt::class.java)
            val response: Response.Data<TransactionReceipt> = Response.Data(txReceipt)
            this.registerUserResponse = response
        }

        fun setErrorRegisterUserResponse() {
            val txReceipt: TransactionReceipt = Gson().fromJson(REGISTER_USER_NEGATIVE_RESPONSE, TransactionReceipt::class.java)
            val response = Response.Error.Message(txReceipt.revertReason)
            this.registerUserResponse = response;
        }

        fun setExceptionErrorRegisterUserResponse() {
            val response = Response.Error.Exception(RuntimeException(REGISTER_USER_EXCEPTION_MESSAGE))
            this.registerUserResponse = response;
        }

        private fun getDefaultBalanceResponse(): Response<BigInteger> {
            val testValue = BigInteger.valueOf(0)
            val response = Response.Data(testValue)
            return response
        }

        private fun getDefaultMintTokenResponse(): Response<TransactionReceipt> {
            return mintTokenResponse
        }

        private fun getDefaultRegisterUserResponse(): Response<TransactionReceipt> {
            return registerUserResponse
        }

        private fun getDefaultApproveTokenManager(): Response<TransactionReceipt> {
            return approveTokenManagerResponse
        }

        private companion object {
            // Revert reason: 'execution reverted: User already registered.'
            // Reverted tx (on require() call fail): {"jsonrpc":"2.0","id":4,"error":{"code":3,"message":"execution reverted: Minting a token is allowed only by person itself.","data":"0x08c379a0000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000314d696e74696e67206120746f6b656e20697320616c6c6f776564206f6e6c7920627920706572736f6e20697473656c662e000000000000000000000000000000"}}
            const val MINT_TOKEN_OK_RESPONSE =
                "{\n" +
                        "   \"blockHash\":\"0xf10f239e26eebf4e9db7301c764dfef05b2719cedd9697ed9684f0971629241a\",\n" +
                        "   \"blockNumber\":\"0x1d12\",\n" +
                        "   \"contractAddress\":null,\n" +
                        "   \"cumulativeGasUsed\":\"0x31214\",\n" +
                        "   \"effectiveGasPrice\":\"0xf4610900\",\n" +
                        "   \"from\":\"0x52e7400ba1b956b11394a5045f8bc3682792e1ac\",\n" +
                        "   \"gasUsed\":\"0x31214\",\n" +
                        "   \"logs\":[\n" +
                        "      {\n" +
                        "         \"address\":\"0xeb1aa6da9c4598ae28b793331a9b711c01670a7e\",\n" +
                        "         \"topics\":[\n" +
                        "            \"0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef\",\n" +
                        "            \"0x0000000000000000000000000000000000000000000000000000000000000000\",\n" +
                        "            \"0x00000000000000000000000052e7400ba1b956b11394a5045f8bc3682792e1ac\",\n" +
                        "            \"0x0000000000000000000000000000000000000000000000000000000000000002\"\n" +
                        "         ],\n" +
                        "         \"data\":\"0x\",\n" +
                        "         \"blockNumber\":\"0x1d12\",\n" +
                        "         \"transactionHash\":\"0xfdc79086a0cf4a4bc90c49e2884eea38e912496b2984f1a2a29e48b11321f320\",\n" +
                        "         \"transactionIndex\":\"0x0\",\n" +
                        "         \"blockHash\":\"0xf10f239e26eebf4e9db7301c764dfef05b2719cedd9697ed9684f0971629241a\",\n" +
                        "         \"logIndex\":\"0x0\",\n" +
                        "         \"removed\":false\n" +
                        "      }\n" +
                        "   ],\n" +
                        "   \"logsBloom\":\"0x04000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000008000000000000000000000000000000000100000000000000020000000000000000000800000000000000000000000010000000020000000000000000000000200000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000020000000000000001000000000000000000000000800008000000000000000000000\",\n" +
                        "   \"status\":\"0x1\",\n" +
                        "   \"to\":\"0xeb1aa6da9c4598ae28b793331a9b711c01670a7e\",\n" +
                        "   \"transactionHash\":\"0xfdc79086a0cf4a4bc90c49e2884eea38e912496b2984f1a2a29e48b11321f320\",\n" +
                        "   \"transactionIndex\":\"0x0\",\n" +
                        "   \"type\":\"0x0\"\n" +
                        "}"
            const val MINT_TOKEN_NEGATIVE_RESPONSE =
                "{\n" +
                        "   \"blockHash\":\"0xf10f239e26eebf4e9db7301c764dfef05b2719cedd9697ed9684f0971629241a\",\n" +
                        "   \"blockNumber\":\"0x1d12\",\n" +
                        "   \"contractAddress\":null,\n" +
                        "   \"cumulativeGasUsed\":\"0x31214\",\n" +
                        "   \"effectiveGasPrice\":\"0xf4610900\",\n" +
                        "   \"from\":\"0x52e7400ba1b956b11394a5045f8bc3682792e1ac\",\n" +
                        "   \"gasUsed\":\"0x31214\",\n" +
                        "   \"logs\":[\n" +
                        "      {\n" +
                        "         \"address\":\"0xeb1aa6da9c4598ae28b793331a9b711c01670a7e\",\n" +
                        "         \"topics\":[\n" +
                        "            \"0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef\",\n" +
                        "            \"0x0000000000000000000000000000000000000000000000000000000000000000\",\n" +
                        "            \"0x00000000000000000000000052e7400ba1b956b11394a5045f8bc3682792e1ac\",\n" +
                        "            \"0x0000000000000000000000000000000000000000000000000000000000000002\"\n" +
                        "         ],\n" +
                        "         \"data\":\"0x\",\n" +
                        "         \"blockNumber\":\"0x1d12\",\n" +
                        "         \"transactionHash\":\"0xfdc79086a0cf4a4bc90c49e2884eea38e912496b2984f1a2a29e48b11321f320\",\n" +
                        "         \"transactionIndex\":\"0x0\",\n" +
                        "         \"blockHash\":\"0xf10f239e26eebf4e9db7301c764dfef05b2719cedd9697ed9684f0971629241a\",\n" +
                        "         \"logIndex\":\"0x0\",\n" +
                        "         \"removed\":false\n" +
                        "      }\n" +
                        "   ],\n" +
                        "   \"logsBloom\":\"0x04000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000008000000000000000000000000000000000100000000000000020000000000000000000800000000000000000000000010000000020000000000000000000000200000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000020000000000000001000000000000000000000000800008000000000000000000000\",\n" +
                        "   \"status\":\"0x0\",\n" +
                        "   \"revertReason\":\"Artificially made negative response caused by 'revert'\",\n" +
                        "   \"to\":\"0xeb1aa6da9c4598ae28b793331a9b711c01670a7e\",\n" +
                        "   \"transactionHash\":\"0xfdc79086a0cf4a4bc90c49e2884eea38e912496b2984f1a2a29e48b11321f320\",\n" +
                        "   \"transactionIndex\":\"0x0\",\n" +
                        "   \"type\":\"0x0\"\n" +
                        "}"

            const val REGISTER_USER_OK_RESPONSE = "{\n" +
                    "   \"blockHash\":\"0xe330e6fcb19b1156a89a92f4a5790f0a6ea05df7d62de639d320218d96b6061e\",\n" +
                    "   \"blockNumber\":\"0x1e7f\",\n" +
                    "   \"contractAddress\":null,\n" +
                    "   \"cumulativeGasUsed\":\"0xe1f1\",\n" +
                    "   \"effectiveGasPrice\":\"0xf4610900\",\n" +
                    "   \"from\":\"0x62f8dc8a5c80db6e8fcc042f0cc54a298f8f2ffd\",\n" +
                    "   \"gasUsed\":\"0xe1f1\",\n" +
                    "   \"logs\":[\n" +
                    "      \n" +
                    "   ],\n" +
                    "   \"logsBloom\":\"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\n" +
                    "   \"status\":\"0x1\",\n" +
                    "   \"to\":\"0xf8c91ac48e437d3e56bfbffece2d0d4663f37e6f\",\n" +
                    "   \"transactionHash\":\"0xf3fdf99a245a0e7d40b8be77b6d032ccc88682d9f26fd9a7d1265738596186cd\",\n" +
                    "   \"transactionIndex\":\"0x0\",\n" +
                    "   \"type\":\"0x0\"\n" +
                    "}"

            const val REGISTER_USER_NEGATIVE_RESPONSE = "{\n" +
                    "   \"blockHash\":\"0xe330e6fcb19b1156a89a92f4a5790f0a6ea05df7d62de639d320218d96b6061e\",\n" +
                    "   \"blockNumber\":\"0x1e7f\",\n" +
                    "   \"contractAddress\":null,\n" +
                    "   \"cumulativeGasUsed\":\"0xe1f1\",\n" +
                    "   \"effectiveGasPrice\":\"0xf4610900\",\n" +
                    "   \"from\":\"0x62f8dc8a5c80db6e8fcc042f0cc54a298f8f2ffd\",\n" +
                    "   \"gasUsed\":\"0xe1f1\",\n" +
                    "   \"logs\":[\n" +
                    "      \n" +
                    "   ],\n" +
                    "   \"logsBloom\":\"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\n" +
                    "   \"status\":\"0x0\",\n" +
                    "   \"revertReason\":\"Artificially made negative response\",\n" +
                    "   \"to\":\"0xf8c91ac48e437d3e56bfbffece2d0d4663f37e6f\",\n" +
                    "   \"transactionHash\":\"0xf3fdf99a245a0e7d40b8be77b6d032ccc88682d9f26fd9a7d1265738596186cd\",\n" +
                    "   \"transactionIndex\":\"0x0\",\n" +
                    "   \"type\":\"0x0\"\n" +
                    "}"

            const val REGISTER_USER_EXCEPTION_MESSAGE = "Transaction 0xe48d2704a0c3ec9d86288736709fb2cf0d3fcc4b1a0797f136ad59ebc83445b9 has failed with status: 0x0. Gas used: 33112. Revert reason: 'execution reverted: User already registered.'."
        }
    }

}