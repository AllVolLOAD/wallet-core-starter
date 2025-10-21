# wallet-core-starter

# ГАЙС ГАЙС ГАЙС я деанон, в режимной жопе, создам нон-каст кошельки для наших партнеров, клиентов в прямом эфире твича, сделаю # опенсурс пацанский всем гл

# WALLET-STARTER 🔐

[![Kotlin](https://img.shields.io/badge/kotlin-2.0.0-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

**Non-custodial HD wallet generator for Bitcoin & Ethereum** - Built for live streaming and transparent operations.

## 🚀 What is this?

This is an open-source CLI wallet generator that I use **live on stream** to create non-custodial wallets for our partners and clients. Everything happens in real-time - no backdoors, no funny business.

## ✨ Features

- ✅ **BIP39 Seed Phrases** - Generate secure mnemonic seeds
- ✅ **HD Wallet Derivation** - BTC (BIP44/84) + ETH addresses
- ✅ **Military-grade Encryption** - Google Tink encryption for seed storage
- ✅ **Cross-platform** - Runs anywhere with Java 8+
- ✅ **Transparent by Design** - Perfect for live streaming and audits

## 🛠 Quick Start

```bash
# Build the project
./gradlew :app:shadowJar

# Create new wallet
java -jar wallet-cli.jar create

# Show specific address
java -jar wallet-cli.jar show --net testnet --fmt bech32 --index 0

# Generate address range
java -jar wallet-cli.jar range --start 0 --end 5 --net mainnet
📖 Usage Examples
Create New Wallet
bash
java -jar wallet-cli.jar create
Generates new seed phrase, encrypts it to ~/.wallet-starter/seed.enc

Show Address
bash
java -jar wallet-cli.jar show --net mainnet --fmt bech32 --index 0
Batch Generate
bash
java -jar wallet-cli.jar range --start 0 --end 10 --net testnet --fmt legacy
🏗 Architecture
text
wallet-starter/
├── core/           # Crypto logic (BIP39, HD derivation)
├── app/            # CLI interface
└── utils/          # Shared utilities
🔒 Security
Non-custodial - We never see your keys

Local encryption - Seeds encrypted with Tink/AES256

Transparent code - Audit everything yourself

Live stream verified - Watch me build wallets in real-time

🎥 Live Stream
I use this tool live on Twitch to create wallets for our partners. Watch the process from seed generation to address creation - everything happens transparently.

🤝 Contributing
This is #opensorce for the people. PRs welcome! Let's build secure, transparent financial tools together.

⚠️ Disclaimer
For educational purposes. Always secure your seed phrases. Use testnet for practice.
