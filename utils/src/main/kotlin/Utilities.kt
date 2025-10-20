import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.SecureRandomUtils
import org.web3j.crypto.WalletUtils
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.DeterministicKeyChain
import org.bitcoinj.crypto.ChildNumber

object KeyVault {
    private val aead by lazy {
        AeadConfig.register()
        KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM)
            .getPrimitive(com.google.crypto.tink.Aead::class.java)
    }

    fun encrypt(plain: ByteArray): ByteArray = aead.encrypt(plain, null)
    fun decrypt(cipher: ByteArray): ByteArray = aead.decrypt(cipher, null)
}

object WalletOps {
    fun generateSeed(): String =
        MnemonicUtils.generateMnemonic(SecureRandomUtils.secureRandom())

    fun ethAddressFromSeed(mnemonic: String): String =
        WalletUtils.loadBip39Credentials("", mnemonic).address

    fun btcAddressFromSeed(mnemonic: String): String {
        val params = MainNetParams.get()
        val seed = DeterministicSeed(mnemonic, null, "", 0L)
        val chain = DeterministicKeyChain.builder().seed(seed).build()
        // путь: m/44'/0'/0'/0/0 (пример)
        val key = chain.getKeyByPath(
            listOf(
                ChildNumber(44, true),
                ChildNumber(0, true),
                ChildNumber(0, true),
                ChildNumber.ZERO, // внешние
                ChildNumber.ZERO  // индекс 0
            ), true
        )
        return key.toAddress(params).toString()
    }
}
