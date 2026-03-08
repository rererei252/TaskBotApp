<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

type ApiMessageResponse = {
  message?: string
}

const token = ref<string>('')
const password = ref<string>('')
const confirmPassword = ref<string>('')

const isLoading = ref<boolean>(true)
const isTokenValid = ref<boolean>(false)
const isSubmitting = ref<boolean>(false)
const isResetDone = ref<boolean>(false)

const error = ref<string>('')
const success = ref<string>('')

const formDisabled = computed(
  () => isLoading.value || !isTokenValid.value || isSubmitting.value || isResetDone.value,
)
const canSubmit = computed(() => !formDisabled.value)

const goLogin = (): void => {
  window.location.href = '/'
}

const validateToken = async (): Promise<void> => {
  const rawToken = new URLSearchParams(window.location.search).get('token')
  token.value = rawToken?.trim() ?? ''

  if (!token.value) {
    isTokenValid.value = false
    isLoading.value = false
    error.value = '再設定リンクが不正です。'
    return
  }

  try {
    const res = await fetch(
      `http://localhost:8080/api/auth/password-reset/validate?token=${encodeURIComponent(token.value)}`,
    )

    const raw = await res.text()
    let body: ApiMessageResponse = {}
    if (raw) {
      try {
        body = JSON.parse(raw) as ApiMessageResponse
      } catch {
        body = { message: raw }
      }
    }

    if (!res.ok) {
      isTokenValid.value = false
      error.value = body.message ?? '再設定リンクが不正、または期限切れです。'
      return
    }

    isTokenValid.value = true
    success.value = body.message ?? '再設定リンクを確認しました。'
  } catch {
    isTokenValid.value = false
    error.value = 'サーバーに接続できませんでした。'
  } finally {
    isLoading.value = false
  }
}

const onSubmit = async (): Promise<void> => {
  if (!token.value) {
    error.value = '再設定リンクが不正です。'
    return
  }

  if (!password.value || !confirmPassword.value) {
    error.value = '新しいパスワードを入力してください。'
    return
  }

  if (password.value !== confirmPassword.value) {
    error.value = '確認用パスワードが一致しません。'
    return
  }

  error.value = ''
  success.value = ''
  isSubmitting.value = true

  try {
    const res = await fetch('http://localhost:8080/api/auth/password-reset/confirm', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        token: token.value,
        newPassword: password.value,
      }),
    })

    const raw = await res.text()
    let body: ApiMessageResponse = {}
    if (raw) {
      try {
        body = JSON.parse(raw) as ApiMessageResponse
      } catch {
        body = { message: raw }
      }
    }

    if (!res.ok) {
      error.value = body.message ?? 'パスワード再設定に失敗しました。'
      return
    }

    success.value = body.message ?? 'パスワードを更新しました。ログインしてください。'
    password.value = ''
    confirmPassword.value = ''

    // 成功後は同じページで再更新できないようにロック
    isResetDone.value = true
    isTokenValid.value = false
  } catch {
    error.value = 'サーバーに接続できませんでした。'
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  void validateToken()
})
</script>

<template>
  <div class="reset-shell">
    <article class="reset-card">
      <div class="reset-badge">パスワード再設定</div>

      <p class="lead">新しいパスワードを入力してください。</p>

      <div v-if="isLoading" class="status">確認中...</div>

      <form v-else class="form" @submit.prevent="onSubmit">
        <input
          v-model="password"
          type="password"
          autocomplete="new-password"
          placeholder="新しいパスワード"
          :disabled="formDisabled"
        />

        <input
          v-model="confirmPassword"
          type="password"
          autocomplete="new-password"
          placeholder="確認用パスワード"
          :disabled="formDisabled"
        />

        <p v-if="error" class="error">{{ error }}</p>
        <p v-if="success" class="success">{{ success }}</p>

        <button type="submit" :disabled="!canSubmit">
          {{ isSubmitting ? '更新中...' : '更新する' }}
        </button>
      </form>

      <button class="back-button" type="button" @click="goLogin">ログイン画面へ戻る</button>
    </article>
  </div>
</template>

<style scoped>
.reset-shell {
  width: 100%;
  min-height: calc(100vh - 120px);
  display: grid;
  place-items: center;
  padding: 16px;
}

.reset-card {
  width: min(636px, 100%);
  min-height: 430px;
  padding: 56px 28px 28px;
  border: 2px solid rgba(241, 234, 219, 0.9);
  border-radius: 12px;
  background:
    linear-gradient(rgba(241, 234, 219, 0.9), rgba(241, 234, 219, 0.9)),
    repeating-linear-gradient(
      90deg,
      rgba(195, 179, 146, 0.16),
      rgba(195, 179, 146, 0.16) 12px,
      rgba(241, 234, 219, 0.08) 12px,
      rgba(241, 234, 219, 0.08) 24px
    );
  box-shadow: 0 22px 34px rgba(84, 68, 39, 0.14);
  display: grid;
  align-content: center;
  justify-items: center;
  gap: 22px;
  position: relative;
}

.reset-badge {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 10px 34px;
  border: 1px solid #d8cdb2;
  border-radius: 14px;
  background: rgba(241, 234, 219, 0.96);
  font-size: 36px;
  line-height: 1;
  color: #65625a;
  white-space: nowrap;
}

.lead {
  margin: 0;
  text-align: center;
  color: #706a60;
  font-size: 16px;
}

.status {
  color: #706a60;
  font-size: 16px;
}

.form {
  width: min(438px, 100%);
  display: grid;
  gap: 16px;
}

input {
  width: 100%;
  border: none;
  border-bottom: 1px solid #7f7a70;
  background: transparent;
  padding: 10px 2px;
  font-size: 15px;
  color: #3e3d3b;
}

input:focus {
  outline: none;
  border-bottom-color: #4f46e5;
}

button {
  width: min(430px, 100%);
  height: 70px;
  margin-inline: auto;
  margin-top: 10px;
  border: 1px solid #686156;
  border-radius: 15px;
  padding: 0 12px;
  font-size: 35px;
  line-height: 1;
  background: #e0d2ab;
  color: #3e3a35;
  cursor: pointer;
}

button:disabled {
  opacity: 0.75;
  cursor: not-allowed;
}

button:hover:enabled {
  background: #d7c696;
}

.back-button {
  width: auto;
  height: auto;
  margin-top: -2px;
  border: none;
  border-radius: 0;
  padding: 0;
  background: transparent;
  color: #4f4b44;
  font-size: 16px;
  line-height: 1.4;
  text-decoration: underline;
}

.back-button:hover {
  background: transparent;
  color: #2f2c28;
}

.error {
  margin: 0;
  color: #b91c1c;
  font-size: 14px;
  text-align: left;
}

.success {
  margin: 0;
  color: #1f6d34;
  font-size: 14px;
  text-align: left;
}

@media (max-width: 480px) {
  .reset-shell {
    min-height: calc(100vh - 110px);
  }

  .reset-card {
    padding: 48px 16px 20px;
  }

  .reset-badge {
    font-size: 30px;
  }

  button {
    font-size: 30px;
  }
}
</style>