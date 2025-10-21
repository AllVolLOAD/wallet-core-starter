import org.yourproject.wallet.utils.Logger

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        handleCommandLine(args)
        return
    }

    // Старое интерактивное меню
    interactiveMenu()
}

private fun handleCommandLine(args: Array<String>) {
    try {
        when (val command = args[0]) {
            "create" -> handleCreateCommand(args)
            "restore-file" -> handleRestoreFileCommand(args)
            "show" -> handleShowCommand(args)
            "range" -> handleRangeCommand(args)
            "--help", "-h" -> printHelp()
            else -> {
                Logger.error("Unknown command: $command")
                printHelp()
                kotlin.system.exitProcess(1)
            }
        }
        kotlin.system.exitProcess(0) // Успешное завершение
    } catch (e: Exception) {
        Logger.error("Command failed: ${e.message}")
        kotlin.system.exitProcess(1) // Ошибка
    }
}

private fun handleCreateCommand(args: Array<String>) {
    Logger.info("Starting wallet creation...")
    val seed = WalletOps.generateSeed()
    println("🧩 Seed: $seed")

    val eth = WalletOps.ethAddressFromSeed(seed)
    val btc = WalletOps.btcAddressFromSeed(seed, purpose = 84, network = "mainnet")

    Logger.info("Generated ETH address: $eth")
    Logger.info("Generated BTC address: $btc")

    val enc = KeyVault.encrypt(seed.encodeToByteArray())
    SeedStorage.saveEncrypted(enc)
    Logger.success("Wallet created successfully! Seed saved to: ${SeedStorage.location()}")
}


private fun handleRestoreFileCommand(args: Array<String>) {
    val enc = SeedStorage.loadEncryptedOrNull()
    if (enc == null) {
        println("❌ No encrypted seed file found")
        kotlin.system.exitProcess(1)
    }
    val seed = String(KeyVault.decrypt(enc))
    println("🔓 Seed restored from file")

    val eth = WalletOps.ethAddressFromSeed(seed)
    val btc = WalletOps.btcAddressFromSeed(seed, purpose = 84, network = "mainnet")
    println("💎 ETH: $eth")
    println("₿ BTC (bech32, mainnet): $btc")
}

private fun handleShowCommand(args: Array<String>) {
    val seed = loadSeedFromFileOrExit()
    val network = getArgValue(args, "--net") ?: "testnet" // ← testnet по умолчанию
    val format = getArgValue(args, "--fmt") ?: "bech32"
    val purpose = getArgValue(args, "--purpose")?.toIntOrNull() ?: when (format) {
        "legacy" -> 44
        "bech32" -> 84
        else -> 84
    }
    val index = getArgValue(args, "--index")?.toIntOrNull() ?: 0

    val eth = WalletOps.ethAddressFromSeed(seed)
    val btc = WalletOps.btcAddressFromSeed(seed, purpose = purpose, network = network, index = index)

    println("💎 ETH: $eth")
    val purposeName = when (purpose) {
        44 -> "legacy (P2PKH)"
        49 -> "p2sh-segwit (P2SH)"
        84 -> "bech32 (P2WPKH)"
        else -> "unknown"
    }
    println("₿ BTC ($purposeName, $network, index=$index): $btc")
}

private fun handleRangeCommand(args: Array<String>) {
    val seed = loadSeedFromFileOrExit()
    val network = getArgValue(args, "--net") ?: "testnet"
    val purpose = getArgValue(args, "--purpose")?.toIntOrNull() ?: 84
    val start = getArgValue(args, "--start")?.toIntOrNull() ?: run {
        Logger.error("--start parameter required")
        kotlin.system.exitProcess(1)
    }
    val end = getArgValue(args, "--end")?.toIntOrNull() ?: run {
        Logger.error("--end parameter required")
        kotlin.system.exitProcess(1)
    }

    Logger.info("Generating addresses: purpose=$purpose, network=$network, range=$start..$end")

    val addresses = WalletOps.btcAddressesRange(
        mnemonic = seed,
        purpose = purpose,
        network = network,
        startIndex = start,
        endIndex = end
    )

    Logger.success("Generated ${addresses.size} BTC addresses")
    println("₿ BTC indices $start..$end:")
    addresses.forEachIndexed { i, addr ->
        println("  ${start + i}: $addr")
    }
}

private fun loadSeedFromFileOrExit(): String {
    val enc = SeedStorage.loadEncryptedOrNull()
    if (enc == null) {
        println("❌ No encrypted seed file found. Use 'create' command first.")
        kotlin.system.exitProcess(1)
    }
    return String(KeyVault.decrypt(enc))
}

private fun getArgValue(args: Array<String>, flag: String): String? {
    val index = args.indexOf(flag)
    return if (index != -1 && index + 1 < args.size) args[index + 1] else null
}

private fun printHelp() {
    println(
        """
        🚀 Wallet CLI - Command Line Interface
        
        Usage: java -jar wallet-cli.jar <command> [options]
        
        Commands:
          create                   Generate new seed phrase and encrypt to file
          restore-file             Restore seed from existing encrypted file
          show                     Show address for specific index
          range                    Show addresses in range
        
        Options for 'show' & 'range':
          --net <mainnet|testnet>  Network (default: testnet)
          --fmt <legacy|bech32>    Address format (default: bech32)  
          --purpose <44|84>        BIP purpose: 44=legacy, 84=bech32
          --index <N>              Address index (default: 0)
        
        Options for 'range':
          --start <A>              Start index (required)
          --end <B>                End index (required)
        
        Examples:
          java -jar wallet-cli.jar create
          java -jar wallet-cli.jar show --purpose 49 --net testnet --index 0
          java -jar wallet-cli.jar range --purpose 44 --start 0 --end 3 --net mainnet
        
        Run without arguments for interactive menu.
        """.trimIndent()
    )
}

// Оригинальное интерактивное меню (переименовано)
private fun interactiveMenu() {
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

// Остальные оригинальные функции остаются БЕЗ ИЗМЕНЕНИЙ
private fun createFlow() {
    val seed = WalletOps.generateSeed()
    println("🧩 Seed:\n$seed")

    val eth = WalletOps.ethAddressFromSeed(seed)
    val btc = WalletOps.btcAddressFromSeed(seed, purpose = 84, network = "mainnet")
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
    val btc = WalletOps.btcAddressFromSeed(seed, purpose = 84, network = "mainnet")
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
    val btc = WalletOps.btcAddressFromSeed(seed, purpose = 84, network = "mainnet")
    println("💎 ETH: $eth")
    println("₿ BTC (bech32, mainnet): $btc")
}

private fun showSingleAddressesFlow() {
    val seed = loadSeedOrAsk() ?: return
    val net = ask("Network [mainnet/testnet]", "mainnet")
    val fmt = ask("BTC format [legacy/bech32]", "bech32")
    val purpose = when (fmt) { // ← ДОБАВЛЕНО КОНВЕРТАЦИЯ format → purpose
        "legacy" -> 44
        "bech32" -> 84
        else -> 84
    }
    val idx = ask("Index (0..n)", "0").toIntOrNull() ?: 0

    val eth = WalletOps.ethAddressFromSeed(seed)
    val btc = WalletOps.btcAddressFromSeed(seed, purpose = purpose, network = net, index = idx) // ← ИСПРАВЛЕНО
    println("💎 ETH: $eth")
    val purposeName = when (purpose) {
        44 -> "legacy"
        84 -> "bech32"
        else -> "unknown"
    }
    println("₿ BTC ($purposeName, $net, index=$idx): $btc")
}

private fun showRangeFlow() {
    val seed = loadSeedOrAsk() ?: return
    val net = ask("Network [mainnet/testnet]", "mainnet")
    val fmt = ask("BTC format [legacy/bech32]", "bech32")
    val purpose = when (fmt) { // ← ДОБАВЛЕНО КОНВЕРТАЦИЯ format → purpose
        "legacy" -> 44
        "bech32" -> 84
        else -> 84
    }
    val start = ask("Start index", "0").toIntOrNull() ?: 0
    val end = ask("End index", "4").toIntOrNull() ?: 4

    val list = WalletOps.btcAddressesRange(
        mnemonic = seed,
        purpose = purpose, // ← ИСПРАВЛЕНО
        network = net,
        startIndex = start,
        endIndex = end
    )
    val purposeName = when (purpose) {
        44 -> "legacy"
        84 -> "bech32"
        else -> "unknown"
    }
    println("₿ BTC ($purposeName, $net) indices $start..$end:")
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