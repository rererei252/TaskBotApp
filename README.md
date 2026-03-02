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
- `GET /api/auth/verify?token=...`

## メール設定の恒久化（Gmail / 完全無料）

`taskbotapp-backend/src/main/resources/application-local.properties` を作成して、SMTP情報を保存します。

1. テンプレートをコピー

```powershell
cd taskbotapp-backend
Copy-Item src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

2. `application-local.properties` の値を編集
   - `spring.mail.username` に Gmail アドレス
   - `spring.mail.password` に Google アプリパスワード（16桁）
   - `app.auth.mail-from` に同じ Gmail アドレス

3. Google アカウント側で 2 段階認証を有効化し、アプリパスワードを発行
   - 通常のログインパスワードは SMTP 送信に使えません

4. `local` プロファイルで起動

```powershell
cd taskbotapp-backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

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
