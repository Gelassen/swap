package com.example.wallet.debug.contract

open class ERC721impl: IERC721 {
    override fun balanceOf(owner: String): Int {
        TODO("Not yet implemented")
    }

    override fun ownerOf(tokenId: String): String {
        TODO("Not yet implemented")
    }

    override fun safeTransferFrom(from: String, to: String, tokenId: String) {
        TODO("Not yet implemented")
    }

    override fun transferFrom(from: String, to: String, tokenId: String) {
        TODO("Not yet implemented")
    }

    override fun approve(to: String, tokenId: String) {
        TODO("Not yet implemented")
    }

    override fun setApprovalForAll(operator: String, approved: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getApproved(tokenId: String): String {
        TODO("Not yet implemented")
    }

    override fun isApprovedForAll(owner: String, operator: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun safeMint(to: String, tokenId: String) {
        TODO("Not yet implemented")
    }

    override fun burn(tokenId: String) {
        TODO("Not yet implemented")
    }
}