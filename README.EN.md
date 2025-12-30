> [!NOTE]
> 🌍️ このリポジトリには日本語版のREADMEがあります。
> [**ここ**](README.md)をクリックして日本語版READMEへジャンプします。

***

# ScreenshotToClipboard  (EN)

This mod is *for Fabric* and *allows* you to **copy your screenshots to the clipboard and share them** instantly.

## Operating Environment

> - Minecraft Java Edition 1.18.2 - 1.21.8
> - Mod Loader: Fabric
> - Fabric API: **REQUIRED**

## How To Install

1. [Install](https://fabricmc.net/use/installer/ "Go To Install Page of Fabric") the **latest version of Fabric**

2. [Install](https://modrinth.com/mod/fabric-api "Install Fabric API from Modrinth") **Fabric API**

> [!WARNING]
> *Fabric API is Required. If you don't install it, this mod will **NOT** be work.*

> [!NOTE]
> *We will not cover how to install Fabric and Fabric API here.  Please [GOOGLE](https://google.com/search?q=Fabric+How+to+Install) them if you're unsure.*

3. **Download `.jar` file** from Release for this repository

4. **Place the `.jar` file** in the `mods` folder in your game directory (Fabric)

## How To Use

- When you take a screenshot, it is automatically copied to the clipboard.

  - You don't need to change settings especially.

- You can change advanced settings for edit `config.json`.

## config.json

```json
{
    "showMessage": true,
    "notificationType": "CHAT"
}
```

| ITEM | DESCRIPTION | VALUE (TYPE) | DEFAULT |
| ---- | ---- | ---- | ---- |
| `showMessage` | Whether show messages when the screenshot copied. | `true \| false`(Boolean) | `true` |
| `notificationType` | Changed how messages are displayed when copying. | `"CHAT" \| "TOAST"` (String) | `"CHAT"` |

## LICENSE

This mod is licensed under the CC0 1.0 License.
