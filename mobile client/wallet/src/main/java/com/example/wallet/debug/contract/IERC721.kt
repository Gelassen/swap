package com.example.wallet.debug.contract

interface IERC721 {

    fun balanceOf(owner: String): Int

    fun ownerOf(tokenId: String): String

    // void safeTransferFrom(ERC721Context ctx, String from, String to, String tokenId, String data);

    // void safeTransferFrom(ERC721Context ctx, String from, String to, String tokenId, String data);
    fun safeTransferFrom(from: String, to: String, tokenId: String)

    fun transferFrom(from: String, to: String, tokenId: String)

    fun approve(to: String, tokenId: String)

    fun setApprovalForAll(operator: String, approved: Boolean)

    fun getApproved(tokenId: String): String

    fun isApprovedForAll(owner: String, operator: String): Boolean

    fun safeMint(to: String, tokenId: String)

    fun burn(tokenId: String)

}