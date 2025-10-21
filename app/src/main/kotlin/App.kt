fun main() {
    while (true) {
        println("🚀 Wallet CLI")
        println("1) Create wallet (encrypt+save)")
        println("2) Restore from seed (manual)")
        println("3) Restore from encrypted file")
        println("4) Show BTC/ETH addresses")
        println("5) Show BTC addresses range (index start..end)")
        println("6) Exit")
        print("> ")
        when (readlnOrNull()?.trim()) {
            "1" -> createFlow()
            "2" -> restoreManualFlow()
            "3" -> restoreFromFileFlow()
            "4" -> showSingleAddressesFlow()
            "5" -> showRangeFlow()
            "6" -> return
            else -> println("❌ Unknown option\n")
        }
        println()
    }
}

private fun createFlow() {
    val seed = WalletOps.generateSeed()
    println("🧩 Seed:\n$seed")

    val eth = WalletOps.ethAddressFromSeed(seed)
    val btc = WalletOps.btcAddressFromSeed(seed, format = "bech32", network = "mainnet")
    println("💎 ETH: $eth")
    println("₿ BTC (bech32, mainnet): $btc")

    val enc = KeyVault.encrypt(seed.encodeToByteArray())
    SeedStorage.saveEncrypted(enc)
    println("🔐 Seed encrypted and saved: ${SeedStorage.location()}")

    val dec = String(KeyVault.decrypt(enc))
    println("✅ Decrypted equals seed: ${dec == seed}")
}

private fun restoreManualFlow() {
    print("Enter seed: ")
    val seed = readlnOrNull()?.trim().orEmpty()
    if (seed.isBlank()) return println("❌ Empty seed")

    val eth = WalletOps.ethAddressFromSeed(seed)
    val btc = WalletOps.btcAddressFromSeed(seed, format = "bech32", network = "mainnet")
    println("💎 ETH: $eth")
    println("₿ BTC (bech32, mainnet): $btc")

    print("Save encrypted to file? (y/N): ")
    if (readlnOrNull()?.trim()?.lowercase() == "y") {
        val enc = KeyVault.encrypt(seed.encodeToByteArray())
        SeedStorage.saveEncrypted(enc)
        println("💾 Saved to: ${SeedStorage.location()}")
    }
}

private fun restoreFromFileFlow() {
    val enc = SeedStorage.loadEncryptedOrNull()
    if (enc == null) {
        println("⚠️ No encrypted seed file found.")
        return
    }
    val seed = String(KeyVault.decrypt(enc))
    println("🔓 Seed restored from file.")
    val eth = WalletOps.ethAddressFromSeed(seed)
    val btc = WalletOps.btcAddressFromSeed(seed, format = "bech32", network = "mainnet")
    println("💎 ETH: $eth")
    println("₿ BTC (bech32, mainnet): $btc")
}

private fun showSingleAddressesFlow() {
    val seed = loadSeedOrAsk() ?: return
    val net = ask("Network [mainnet/testnet]", "mainnet")
    val fmt = ask("BTC format [legacy/bech32]", "bech32")
    val idx = ask("Index (0..n)", "0").toIntOrNull() ?: 0

    val eth = WalletOps.ethAddressFromSeed(seed)
    val btc = WalletOps.btcAddressFromSeed(seed, format = fmt, network = net, index = idx)
    println("💎 ETH: $eth")
    println("₿ BTC ($fmt, $net, index=$idx): $btc")
}

private fun showRangeFlow() {
    val seed = loadSeedOrAsk() ?: return
    val net = ask("Network [mainnet/testnet]", "mainnet")
    val fmt = ask("BTC format [legacy/bech32]", "bech32")
    val start = ask("Start index", "0").toIntOrNull() ?: 0
    val end = ask("End index", "4").toIntOrNull() ?: 4

    val list = WalletOps.btcAddressesRange(
        mnemonic = seed,
        format = fmt,
        network = net,
        startIndex = start,
        endIndex = end
    )
    println("₿ BTC ($fmt, $net) indices $start..$end:")
    list.forEachIndexed { i, addr -> println("  ${start + i}: $addr") }
}

private fun loadSeedOrAsk(): String? {
    val enc = SeedStorage.loadEncryptedOrNull()
    return if (enc != null) {
        String(KeyVault.decrypt(enc))
    } else {
        print("No encrypted seed. Enter seed: ")
        val s = readlnOrNull()?.trim().orEmpty()
        if (s.isBlank()) { println("❌ Empty seed"); null } else s
    }
}

private fun ask(label: String, def: String): String {
    print("$label (default: $def): ")
    val v = readlnOrNull()?.trim().orEmpty()
    return if (v.isBlank()) def else v
}
