# TaskBotApp

TaskBotApp は、`Spring Boot` バックエンドと `Vue 3 + Vite` フロントエンドで構成されたアプリです。

## ディレクトリ構成

- `taskbotapp-backend`: Spring Boot アプリ
- `taskbotapp-frontend`: Vue 3 + Vite アプリ

## 前提環境

- `Java 21`
- `Node.js 18+`（推奨: LTS）
- `npm`
- `PostgreSQL 16+`（推奨）

## PostgreSQL 設定

バックエンドは以下を既定値として PostgreSQL に接続します。

- DB名: `taskkan_db`
- URL: `jdbc:postgresql://localhost:5432/taskkan_db`
- ユーザー: `postgres`
- パスワード: `postgres`

必要に応じて環境変数で上書きできます。

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

## 認証 API

- `POST /api/auth/signup`
- `POST /api/auth/login`

### signup リクエスト例

```json
{
  "username": "taro",
  "email": "taro@example.com",
  "password": "abc12345"
}
```

### login リクエスト例

```json
{
  "email": "taro@example.com",
  "password": "abc12345"
}
```

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
