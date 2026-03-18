<script setup lang="ts">
import { onMounted, ref } from 'vue'

type MenuKey = 'basic' | 'account' | 'notice'
type ProfileResponse = {
  username?: string | null
  email?: string | null
  profileMessage?: string | null
  profileImageUrl?: string | null
}
type ProfileImageUploadResponse = {
  profileImageUrl?: string | null
}

const selectedMenu = ref<MenuKey>('basic')
const profileImageUrl = ref('')
const selectedImageFile = ref<File | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)
const username = ref('')
const email = ref('')
const message = ref('')
const isLoading = ref(false)
const isSaving = ref(false)
const isLoggingOut = ref(false)
const isWithdrawing = ref(false)
const error = ref('')
const success = ref('')
const accountStatus = ref('')
const accountError = ref('')

const fetchProfile = async (): Promise<void> => {
  isLoading.value = true
  error.value = ''
  success.value = ''
  try {
    const res = await fetch('http://localhost:8080/api/account/profile', {
      method: 'GET',
      credentials: 'include',
    })
    if (!res.ok) throw new Error('failed')

    const data = (await res.json()) as ProfileResponse
    username.value = data.username ?? ''
    email.value = data.email ?? ''
    message.value = data.profileMessage ?? ''
    profileImageUrl.value = data.profileImageUrl ?? ''
  } catch {
    error.value = 'プロフィール取得に失敗しました。'
  } finally {
    isLoading.value = false
  }
}

const onSaveProfile = async (): Promise<void> => {
  if (isSaving.value) return

  isSaving.value = true
  error.value = ''
  success.value = ''
  try {
    if (selectedImageFile.value) {
      const formData = new FormData()
      formData.append('file', selectedImageFile.value)
      const imageRes = await fetch('http://localhost:8080/api/account/profile-image', {
        method: 'POST',
        credentials: 'include',
        body: formData,
      })
      if (!imageRes.ok) throw new Error('image_upload_failed')
      const uploaded = (await imageRes.json()) as ProfileImageUploadResponse
      profileImageUrl.value = uploaded.profileImageUrl ?? profileImageUrl.value
      selectedImageFile.value = null
    }

    const res = await fetch('http://localhost:8080/api/account/profile', {
      method: 'PUT',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: username.value,
        email: email.value,
        profileMessage: message.value,
        profileImageUrl: profileImageUrl.value || null,
      }),
    })
    if (!res.ok) throw new Error('failed')

    success.value = '保存しました。'
  } catch {
    error.value = '保存に失敗しました。'
  } finally {
    isSaving.value = false
  }
}

const onMovePasswordChange = (): void => {
  window.location.href = '/auth/forgot-password?from=account'
}

const onLogout = async (): Promise<void> => {
  if (isLoggingOut.value) return

  isLoggingOut.value = true
  accountError.value = ''
  accountStatus.value = ''
  try {
    await fetch('http://localhost:8080/api/auth/logout', {
      method: 'POST',
      credentials: 'include',
    })
  } finally {
    window.location.href = '/'
  }
}

const onWithdraw = async (): Promise<void> => {
  if (isWithdrawing.value) return

  const confirmed = window.confirm('退会するとアカウントは利用できなくなります。退会しますか？')
  if (!confirmed) return

  isWithdrawing.value = true
  accountError.value = ''
  accountStatus.value = ''
  try {
    const res = await fetch('http://localhost:8080/api/account/withdraw', {
      method: 'DELETE',
      credentials: 'include',
    })
    if (!res.ok) throw new Error('failed')

    window.location.href = '/'
  } catch {
    accountError.value = '退会に失敗しました。時間をおいて再度お試しください。'
  } finally {
    isWithdrawing.value = false
  }
}

onMounted(() => {
  void fetchProfile()
})

const selectMenu = (menu: MenuKey): void => {
  selectedMenu.value = menu
}

const openImagePicker = (): void => {
  fileInput.value?.click()
}

const onPickImage = (event: Event): void => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  selectedImageFile.value = file
  if (profileImageUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(profileImageUrl.value)
  }
  profileImageUrl.value = URL.createObjectURL(file)
}
</script>

<template>
  <section class="account-page" aria-label="account-page">
    <aside class="sidebar">
      <h2 class="sidebar-title">設定メニュー</h2>
      <nav class="sidebar-nav">
        <button
          type="button"
          class="sidebar-item"
          :class="{ 'is-active': selectedMenu === 'basic' }"
          @click="selectMenu('basic')"
        >
          基本設定
        </button>

        <button
          type="button"
          class="sidebar-item"
          :class="{ 'is-active': selectedMenu === 'account' }"
          @click="selectMenu('account')"
        >
          アカウント管理
        </button>

        <button
          type="button"
          class="sidebar-item"
          :class="{ 'is-active': selectedMenu === 'notice' }"
          @click="selectMenu('notice')"
        >
          通知設定
        </button>
      </nav>
    </aside>

    <section class="content">
      <div v-if="selectedMenu === 'basic'" class="basic-panel">
        <p v-if="isLoading" class="status">読み込み中...</p>
        <p v-if="error" class="status status-error">{{ error }}</p>
        <p v-if="success" class="status status-success">{{ success }}</p>

        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          class="hidden-input"
          @change="onPickImage"
        />

        <button class="avatar" type="button" @click="openImagePicker">
          <img v-if="profileImageUrl" :src="profileImageUrl" alt="プロフィール画像" />
          <span v-else />
        </button>

        <div class="fields-shell">
          <div class="field-row">
            <label for="userNameInput">ユーザ名：</label>
            <input id="userNameInput" v-model="username" type="text" maxlength="20" />
          </div>

          <div class="field-row">
            <label for="emailInput">メールアドレス：</label>
            <input id="emailInput" v-model="email" type="email" readonly />
          </div>

          <div class="field-row field-row-message">
            <label for="messageInput">メッセージ：</label>
            <textarea id="messageInput" v-model="message" />
          </div>
        </div>
        <button type="button" class="save-button" :disabled="isSaving || isLoading" @click="onSaveProfile">
          {{ isSaving ? '保存中...' : '保存' }}
        </button>
      </div>
      <div v-else-if="selectedMenu === 'account'" class="account-panel">
        <p v-if="accountError" class="status status-error">{{ accountError }}</p>
        <p v-if="accountStatus" class="status status-success">{{ accountStatus }}</p>

        <div class="account-row">
          <span>パスワード変更：</span>
          <div class="account-action-wrap">
            <button type="button" class="account-action" @click="onMovePasswordChange">変更</button>
            <p class="account-note">（パスワード再設定画面へ移動します）</p>
          </div>
        </div>

        <div class="account-row">
          <span>ログアウト：</span>
          <button type="button" class="account-action" :disabled="isLoggingOut" @click="onLogout">
            {{ isLoggingOut ? '処理中...' : 'ログアウト' }}
          </button>
        </div>

        <div class="account-row">
          <span>退会処理：</span>
          <button type="button" class="account-action account-action-danger" :disabled="isWithdrawing" @click="onWithdraw">
            {{ isWithdrawing ? '処理中...' : '退会' }}
          </button>
        </div>
      </div>
      <div v-else>通知設定の中身</div>
    </section>
  </section>
</template>

<style scoped>
.account-page {
  min-height: calc(100vh - 140px);
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr) 240px;
  gap: 28px;
  padding-top: 14px;
  padding-inline: 20px;
}

.sidebar {
  border: 1px solid rgba(47, 44, 40, 0.28);
  border-radius: 10px;
  background: rgba(251, 247, 236, 0.82);
  padding: 14px;
  align-self: start;
}

.sidebar-title {
  margin: 0 0 10px;
  font-size: 18px;
  color: #3f3a34;
}

.sidebar-nav {
  display: grid;
  gap: 8px;
}

.sidebar-item {
  width: 100%;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  text-align: left;
  padding: 10px 12px;
  font: inherit;
  color: #3f3a34;
  cursor: pointer;
}

.sidebar-item:hover {
  background: rgba(47, 44, 40, 0.08);
}

.sidebar-item.is-active {
  border-color: rgba(47, 44, 40, 0.25);
  background: rgba(47, 44, 40, 0.12);
  font-weight: 700;
}

.content {
  grid-column: 2;
  min-height: 420px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.basic-panel {
  width: min(780px, 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 36px;
  padding-top: 14px;
}

.hidden-input {
  display: none;
}

.avatar {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  border: none;
  background: #c9c9cc;
  overflow: hidden;
  cursor: pointer;
  padding: 0;
  transform: translateX(-10px);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar span {
  display: block;
  width: 100%;
  height: 100%;
}

.fields-shell {
  width: 420px;
  display: grid;
  gap: 44px;
  margin: 0 auto;
}

.field-row {
  position: relative;
}

.field-row label {
  position: absolute;
  right: calc(100% + 16px);
  top: 8px;
  white-space: nowrap;
  font-family: inherit;
  font-size: 20px;
  color: #1f1f1f;
  line-height: 1;
}

.field-row input {
  width: 420px;
  font-family: inherit;
  border: none;
  border-bottom: 1px solid rgba(47, 44, 40, 0.45);
  background: transparent;
  height: 40px;
  font-size: 18px;
  padding: 0 4px;
}

.field-row input[readonly] {
  color: #5b5b5b;
  opacity: 0.9;
  cursor: default;
}

.field-row-message label {
  top: 10px;
}

.field-row textarea {
  width: 420px;
  min-height: 180px;
  font-family: inherit;
  border: 1px solid rgba(47, 44, 40, 0.45);
  background: transparent;
  resize: vertical;
  font-size: 18px;
  padding: 10px;
}

.status {
  margin: 0;
  color: #3f3a34;
  font-size: 14px;
}

.status-error {
  color: #b91c1c;
}

.status-success {
  color: #166534;
}

.save-button {
  width: 220px;
  height: 54px;
  border: 1px solid #686156;
  border-radius: 10px;
  background: #e0d2ab;
  color: #3e3a35;
  font: inherit;
  font-size: 22px;
  cursor: pointer;
}

.save-button:disabled {
  opacity: 0.7;
  cursor: wait;
}

.account-panel {
  width: min(780px, 100%);
  padding-top: 120px;
  display: grid;
  justify-items: center;
  gap: 24px;
}

.account-row {
  width: fit-content;
  display: grid;
  grid-template-columns: 260px 220px;
  align-items: start;
  column-gap: 12px;
  margin-inline: auto;
}

.account-row span {
  height: 54px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  text-align: right;
  white-space: nowrap;
  font-size: 20px;
  line-height: 1;
  color: #1f1f1f;
}

.account-note {
  margin: 6px 0 0;
  width: 220px;
  text-align: left;
  font-size: 12px;
  color: #4f4b44;
}

.account-action-wrap {
  display: grid;
  justify-items: start;
}

.account-action {
  width: 220px;
  height: 54px;
  border: 1px solid #686156;
  border-radius: 10px;
  background: #e0d2ab;
  color: #3e3a35;
  font: inherit;
  font-size: 22px;
  cursor: pointer;
}

.account-action-danger {
  background: #e0d2ab;
}

.account-action:disabled {
  opacity: 0.7;
  cursor: wait;
}
</style>
