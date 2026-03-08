<script setup lang="ts">
import { ref } from 'vue'

type PasswordResetRequest = {
  email: string
}

const email = ref<string>('')
const isSubmitting = ref<boolean>(false)
const message = ref<string>('')
const error = ref<string>('')

const goBack = (): void => {
  if (window.history.length > 1) {
    window.history.back()
    return
  }
  window.location.href = '/'
}

const onSubmit = async (): Promise<void> => {
  if (!email.value.trim()) {
    error.value = 'メールアドレスを入力してください。'
    return
  }

  isSubmitting.value = true
  message.value = ''
  error.value = ''

  const payload: PasswordResetRequest = {
    email: email.value.trim(),
  }

  try {
    await fetch('http://localhost:8080/api/auth/password-reset/request', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    message.value = '入力いただいたメールアドレス宛に再設定手順を送信しました（存在する場合）。'
  } catch {
    error.value = '通信エラーが発生しました。時間をおいて再度お試しください。'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="forgot-shell">
    <article class="forgot-card">
      <div class="forgot-badge">パスワード再設定</div>

      <p class="lead">
        登録済みのメールアドレスまたはユーザーIDを入力してください。<br />
        パスワード再設定用のリンクをメールでお送りします。
      </p>

      <form class="form" @submit.prevent="onSubmit">
        <input
          v-model="email"
          type="email"
          inputmode="email"
          autocomplete="email"
          placeholder="メールアドレスまたはユーザーIDをここに入力"
          required
        />

        <p v-if="message" class="message">{{ message }}</p>
        <p v-if="error" class="error">{{ error }}</p>

        <button type="submit" :disabled="isSubmitting">
          {{ isSubmitting ? '送信中...' : '送信' }}
        </button>
      </form>

      <button class="back-button" type="button" @click="goBack">
        ログイン画面へ戻る
      </button>
    </article>
  </div>
</template>

<style scoped>
.forgot-shell {
  width: 100%;
  min-height: calc(100vh - 120px);
  display: grid;
  place-items: center;
  padding: 16px;
}

.forgot-card {
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

.forgot-badge {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 10px 34px;
  border: 1px solid #d8cdb2;
  border-radius: 14px;
  background: rgba(241, 234, 219, 0.96);
  font-size: 44px;
  line-height: 1;
  color: #65625a;
  white-space: nowrap;
}

.lead {
  margin: 0;
  text-align: center;
  color: #706a60;
  font-size: 14px;
  line-height: 1.6;
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
  cursor: wait;
}

button:hover {
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

.message {
  margin: 0;
  color: #1f6d34;
  font-size: 14px;
  text-align: left;
}

.error {
  margin: 0;
  color: #b91c1c;
  font-size: 14px;
  text-align: left;
}

@media (max-width: 480px) {
  .forgot-shell {
    min-height: calc(100vh - 110px);
  }

  .forgot-card {
    padding: 48px 16px 20px;
  }

  .forgot-badge {
    font-size: 32px;
  }

  button {
    font-size: 30px;
  }
}
</style>
