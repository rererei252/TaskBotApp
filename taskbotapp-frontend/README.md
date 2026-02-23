# taskbotapp-frontend

TaskBotApp のフロントエンド（Vue 3 + Vite）です。

## ディレクトリ構成

```txt
src/
  app/
    App.vue
    layouts/
      DefaultLayout.vue
  features/
    auth/
      pages/
        LoginPage.vue
    tasks/
      api/
      components/
      pages/
        TasksPage.vue
      stores/
  shared/
    components/
      AppFooter.vue
      AppHeader.vue
    lib/
    styles/
      global.css
```

## 前提環境

- `Node.js 18+`
- `npm`

## セットアップ

```powershell
npm install
```

## 開発サーバー起動

```powershell
npm run dev
```

通常は `http://localhost:5173/` で確認できます。

## ビルド

```powershell
npm run build
```

## ビルド結果のプレビュー

```powershell
npm run preview
```
