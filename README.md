# TaskBotApp

TaskBotApp は、`Spring Boot` バックエンドと `Vue 3 + Vite` フロントエンドで構成されたアプリです。

## ディレクトリ構成

- `taskbotapp-backend`: Spring Boot アプリ
- `taskbotapp-frontend`: Vue 3 + Vite アプリ

## 前提環境

- `Java 21`
- `Node.js 18+`（推奨: LTS）
- `npm`

## 起動方法

### 1. バックエンドを起動

```powershell
cd taskbotapp-backend
.\mvnw.cmd spring-boot:run
```

起動後、`http://localhost:8080/` にアクセスすると `TaskBot is running.` が表示されます。

### 2. フロントエンドを起動

別ターミナルで実行:

```powershell
cd taskbotapp-frontend
npm install
npm run dev
```

起動後、表示された URL（通常は `http://localhost:5173/`）にアクセスしてください。

## ビルド

### フロントエンド

```powershell
cd taskbotapp-frontend
npm run build
```

### バックエンド

```powershell
cd taskbotapp-backend
.\mvnw.cmd clean package
```

## テスト

### バックエンド

```powershell
cd taskbotapp-backend
.\mvnw.cmd test
```
