package com.lonley.dev.vault.model

enum class EntryType { Password, Passphrase, CryptoWallet }

enum class Network(val label: String) {
    Bitcoin("Bitcoin"), Ethereum("Ethereum"), Solana("Solana"),
    Polygon("Polygon"), Arbitrum("Arbitrum"), Base("Base"),
    Avalanche("Avalanche"), BNBChain("BNB Chain"), Other("Other")
}
