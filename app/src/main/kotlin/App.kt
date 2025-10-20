fun main() {
    println("🚀 Wallet CLI")
    println("1) Create wallet")
    println("2) Restore from seed")
    print("> ")
    when (readlnOrNull()?.trim()) {
        "1" -> createFlow()
        "2" -> restoreFlow()
        else -> println("❌ Unknown option")
    }
}

private fun createFlow() {
    val seed = WalletOps.generateSeed()
    println("🧩 Seed:\n$seed")

    val eth = WalletOps.ethAddressFromSeed(seed)
    val btc = WalletOps.btcAddressFromSeed(seed)
    println("💎 ETH: $eth")
    println("₿ BTC:  $btc")

    val enc = KeyVault.encrypt(seed.encodeToByteArray())
    println("🔐 Encrypted (bytes): ${enc.size}")

    val dec = String(KeyVault.decrypt(enc))
    println("✅ Decrypted equals seed: ${dec == seed}")
}

private fun restoreFlow() {
    print("Enter seed: ")
    val seed = readlnOrNull()?.trim().orEmpty()
    if (seed.isBlank()) {
        println("❌ Empty seed")
        return
    }
    val eth = WalletOps.ethAddressFromSeed(seed)
    val btc = WalletOps.btcAddressFromSeed(seed)
    println("💎 ETH: $eth")
    println("₿ BTC:  $btc")
}
