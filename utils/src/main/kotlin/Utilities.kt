import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.WalletUtils
import org.bitcoinj.core.LegacyAddress
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.SegwitAddress
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.wallet.DeterministicSeed
import java.nio.file.Files
import java.nio.file.Path
import java.security.SecureRandom
import java.util.Base64

// --------- Хранилище ключа шифрования (Tink AEAD) ----------
object KeyVault {
    private val aead by lazy {
        AeadConfig.register()
        val tpl = KeyTemplates.get("AES256_GCM") // рекомендовано Tink
        KeysetHandle.generateNew(tpl)
            .getPrimitive(com.google.crypto.tink.Aead::class.java)
    }

    fun encrypt(plain: ByteArray): ByteArray = aead.encrypt(plain, null)
    fun decrypt(cipher: ByteArray): ByteArray = aead.decrypt(cipher, null)
}

// --------- Логика кошелька ----------
object WalletOps {
    /** 12 слов (128-бит энтропии). */
    fun generateSeed(): String {
        val entropy = ByteArray(16)
        SecureRandom().nextBytes(entropy)
        return MnemonicUtils.generateMnemonic(entropy)
    }

    /** ETH (web3j по умолчанию m/44'/60'/0'/0/0). */
    fun ethAddressFromSeed(mnemonic: String): String =
        WalletUtils.loadBip39Credentials("", mnemonic).address

    /**
     * BTC адрес по мнемонике.
     * @param format  "legacy" (BIP44, P2PKH) | "bech32" (BIP84, P2WPKH)
     * @param network "mainnet" | "testnet"
     * @param account HD account (по умолчанию 0)
     * @param change  0 (external) | 1 (internal)
     * @param index   индекс адреса
     */
    fun btcAddressFromSeed(
        mnemonic: String,
        format: String = "bech32",
        network: String = "mainnet",
        account: Int = 0,
        change: Int = 0,
        index: Int = 0
    ): String {
        val params: NetworkParameters =
            if (network.equals("testnet", ignoreCase = true)) TestNet3Params.get()
            else MainNetParams.get()

        val purpose = if (format.equals("legacy", true)) 44 else 84 // BIP44 vs BIP84
        val seed = DeterministicSeed(mnemonic, null, "", 0L)
        val chain = DeterministicKeyChain.builder().seed(seed).build()

        val path = listOf(
            ChildNumber(purpose, true),
            ChildNumber(0, true),                 // coin_type' (0 = BTC; test/main — через params)
            ChildNumber(account, true),           // account'
            ChildNumber(change, false),           // external/internal
            ChildNumber(index, false)             // address index
        )
        val key = chain.getKeyByPath(path, true)

        return if (format.equals("legacy", true))
            LegacyAddress.fromKey(params, key).toString()
        else
            SegwitAddress.fromKey(params, key).toBech32()
    }

    /** Сгенерировать диапазон BTC-адресов с индексами [startIndex, endIndex] включительно. */
    fun btcAddressesRange(
        mnemonic: String,
        format: String = "bech32",
        network: String = "mainnet",
        account: Int = 0,
        change: Int = 0,
        startIndex: Int = 0,
        endIndex: Int = 4
    ): List<String> {
        require(startIndex >= 0 && endIndex >= startIndex) { "invalid index range" }
        return (startIndex..endIndex).map { i ->
            btcAddressFromSeed(mnemonic, format, network, account, change, i)
        }
    }
}

// --------- Примитивное файловое хранилище зашифрованного seed (ПК) ----------
object SeedStorage {
    private val dir: Path = Path.of(System.getProperty("user.home"), ".wallet-starter")
    private val file: Path = dir.resolve("seed.enc")

    fun saveEncrypted(bytes: ByteArray) {
        if (!Files.exists(dir)) Files.createDirectories(dir)
        Files.write(file, Base64.getEncoder().encode(bytes))
    }

    fun loadEncryptedOrNull(): ByteArray? {
        if (!Files.exists(file)) return null
        val encoded = Files.readAllBytes(file)
        return Base64.getDecoder().decode(encoded)
    }

    fun exists(): Boolean = Files.exists(file)

    fun location(): String = file.toAbsolutePath().toString()
}
