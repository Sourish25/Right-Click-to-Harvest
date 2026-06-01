# Right Click to Harvest - Fabric Mod (Multi-Version)

A polished, server-side-only Fabric mod that lets players harvest fully-grown crops simply by right-clicking them, and automatically replants a seed immediately. It also supports instant melon/pumpkin breaking, cocoa beans on jungle logs, nether wart, and Sniffer ancient crops.

---

## 📂 Project Structure

To support the massive unobfuscated mappings transition and Java runtime changes in Minecraft 26.1+, this workspace is structured into **four independent standalone Gradle projects**:

*   **[`mc-1.20/`](file:///C:/Users/Sourish/Desktop/Right%20Click%20to%20Harvest/mc-1.20/)**: Minecraft 1.20.x Mod (Yarn Mappings, Java 17 compatibility)
*   **[`mc-1.21/`](file:///C:/Users/Sourish/Desktop/Right%20Click%20to%20Harvest/mc-1.21/)**: Minecraft 1.21.x Mod (Yarn Mappings, Java 21 compatibility)
*   **[`mc-26.1/`](file:///C:/Users/Sourish/Desktop/Right%20Click%20to%20Harvest/mc-26.1/)**: Minecraft 26.1.x Mod (Mojang Mappings, Java 25 compatibility)
*   **[`mc-26.2/`](file:///C:/Users/Sourish/Desktop/Right%20Click%20to%20Harvest/mc-26.2/)**: Minecraft 26.2-pre-2 Mod (Mojang Mappings, Java 25 compatibility)

---

## 🌾 Supported Crops & Blocks

- **Wheat, Carrots, Potatoes, Beetroots**: Resets age to `0`, playing block break particles/sounds, and consumes exactly `1` seed/crop item from the drops to replant.
- **Cocoa Beans**: Works perfectly on Jungle logs, resetting the Cocoa block age to `0`.
- **Nether Wart**: Resets the Nether Wart block age to `0`.
- **Ancient Sniffer Crops**: Full support for **Torchflowers** and **Pitcher Crops** (replants Torchflower Seeds and Pitcher Pods respectively).
- **Melons & Pumpkins**: Instantly breaks the block (saving your axe durability, and attributing drops to the player).

---

## ☕ Automatic Java Setup (Gradle Toolchains)

Every project is configured using **Gradle Toolchains**:
- **No manual Java setup is required on your PC.**
- Even if your system runs Java 17, Gradle will automatically download the correct JDK version (Java 21 or 25) from Adoptium during compile time, extract it locally, and compile the mod using it.

---

## 🚀 How to Build

1. Open your IDE of choice (IntelliJ IDEA is recommended).
2. Choose **Open or Import** and select the folder of the version you wish to build (e.g. [`mc-1.20/`](file:///C:/Users/Sourish/Desktop/Right%20Click%20to%20Harvest/mc-1.20/)).
3. The IDE will automatically detect, download, and configure the Gradle environment for you.
4. Open the Gradle tab on the right side and run the **`build`** task (or run `gradle build` in a terminal inside that folder).
5. The compiled mod JAR will be generated under `build/libs/`.

---

## 💾 Installation

- **For Server-Side Use**: Drop the compiled `.jar` file and the corresponding **Fabric API** jar file into your server's `mods/` directory. Players connecting with a vanilla client can enjoy all features perfectly!
- **For Single Player**: Drop the compiled `.jar` file and the **Fabric API** jar file into your local game client's `mods/` directory.
