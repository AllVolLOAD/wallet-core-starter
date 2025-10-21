import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UtilitiesTest {

    // Стандартный тестовый сид из BIP39-векторов (12 слов)
    private val testMnemonic =
        "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"

    @Test
    fun `generateSeed returns 12 or 24 words`() {
        val seed = WalletOps.generateSeed()
        val words = seed.trim().split(Regex("\\s+"))
        assertTrue(words.size == 12 || words.size == 24, "Seed must be 12 or 24 words")
        assertTrue(words.all { it.matches(Regex("[a-z]+")) }, "All words should be lowercase a-z")
    }

    @Test
    fun `encryption roundtrip with Tink works`() {
        val seed = WalletOps.generateSeed()
        val enc = KeyVault.encrypt(seed.encodeToByteArray())
        val dec = String(KeyVault.decrypt(enc))
        assertEquals(seed, dec, "Decrypted seed must equal original")
    }

    @Test
    fun `ETH address formatting is valid (0x + 40 hex)`() {
        val eth = WalletOps.ethAddressFromSeed(testMnemonic)
        assertTrue(eth.matches(Regex("^0x[0-9a-fA-F]{40}\$")), "ETH address must be 0x + 40 hex")
    }

    @Test
    fun `BTC bech32 mainnet starts with bc1`() {
        val addr = WalletOps.btcAddressFromSeed(
            mnemonic = testMnemonic,
            format = "bech32",
            network = "mainnet",
            index = 0
        )
        assertTrue(addr.startsWith("bc1"), "Mainnet bech32 must start with bc1")
    }

    @Test
    fun `BTC bech32 testnet starts with tb1`() {
        val addr = WalletOps.btcAddressFromSeed(
            mnemonic = testMnemonic,
            format = "bech32",
            network = "testnet",
            index = 0
        )
        assertTrue(addr.startsWith("tb1"), "Testnet bech32 must start with tb1")
    }

    @Test
    fun `BTC legacy mainnet starts with 1`() {
        val addr = WalletOps.btcAddressFromSeed(
            mnemonic = testMnemonic,
            format = "legacy",
            network = "mainnet",
            index = 0
        )
        assertTrue(addr.startsWith("1"), "Legacy P2PKH mainnet must start with '1'")
    }

    @Test
    fun `BTC legacy testnet starts with m or n`() {
        val addr = WalletOps.btcAddressFromSeed(
            mnemonic = testMnemonic,
            format = "legacy",
            network = "testnet",
            index = 0
        )
        assertTrue(addr.startsWith("m") || addr.startsWith("n"),
            "Legacy P2PKH testnet must start with 'm' or 'n'")
    }

    @Test
    fun `BTC range returns distinct addresses and correct count`() {
        val list = WalletOps.btcAddressesRange(
            mnemonic = testMnemonic,
            format = "bech32",
            network = "mainnet",
            startIndex = 0,
            endIndex = 5
        )
        assertEquals(6, list.size, "Expected 6 addresses (0..5)")
        assertEquals(list.size, list.toSet().size, "Addresses in range must be distinct")
        assertTrue(list.all { it.startsWith("bc1") }, "All bech32 mainnet must start with bc1")
    }
}
