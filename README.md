# ScreenshotToClipboard

このModは、**あなたのスクリーンショットをクリップボードへコピー**してすぐに共有できるFabric用Modです。

## 動作環境

> - Minecraft Java Edition 1.20.5 - 1.21.11
> - Modローダー：Fabric
> - Fabric API：必須

## 導入方法

1. **Fabricの最新バージョン**を[インストールする](https://fabricmc.net/use/installer/ "Fabricのインストールページへ")

2. **Fabric API**を[インストールする](https://modrinth.com/mod/fabric-api "Fabric APIをModrinthからインストール")

> [!WARNING]
> *Fabric APIは必ずインストールしてください。インストールしない場合Modが動作しません。*

> [!NOTE]
> *FabricとFabric APIの詳しいインストール方法は省略します。  不明点は[都度検索してください](https://google.com/search?q=Fabric+インストール方法)。*

3. **このリポジトリのReleases**から`.jar`ファイルをダウンロードする

4. **Fabricの起動構成がある**フォルダを開いて、`mods`フォルダにダウンロードした`.jar`ファイルを入れる

## 使用方法

- F2でスクリーンショットを撮影すると、スクリーンショットが自動でクリップボードにコピーされます。

  - 特別な設定は必要ありません。

- `config.json`で詳細な設定を変更できます。

## config.json

```json
{
    "showMessage": true,
    "notificationType": "CHAT"
}
```

| 項目 | 説明 | 値 (種類) | デフォルト |
| ---- | ---- | ---- | ---- |
| `showMessage` | コピー時にメッセージを表示するかどうか。 | `true \| false`(Boolean) | `true` |
| `notificationType` | コピー時のメッセージの表示方法の変更。 | `"CHAT" \| "TOAST"` (String) | `"CHAT"` |

## ライセンス

This mod is licensed under the CC0 1.0 License.
