<script setup>
import { computed, ref } from 'vue'

const mode = ref('signin')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const rememberMe = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const isSignIn = computed(() => mode.value === 'signin')
const title = computed(() => (isSignIn.value ? 'サインイン' : '新規登録'))
const submitLabel = computed(() => (isSignIn.value ? 'サインイン' : '登録'))

const switchMode = (nextMode) => {
  mode.value = nextMode
  errorMessage.value = ''
}

const onSubmit = () => {
  if (!email.value || !password.value) {
    errorMessage.value = 'メールアドレスとパスワードを入力してください。'
    return
  }

  if (!isSignIn.value && password.value !== confirmPassword.value) {
    errorMessage.value = '確認用パスワードが一致しません。'
    return
  }

  errorMessage.value = ''
  // TODO: API連携時に認証処理を実装
  console.log('Auth submit:', {
    mode: mode.value,
    email: email.value,
    rememberMe: rememberMe.value,
  })
}
</script>

<template>
  <section class="auth-card">
    <div class="tabs" role="tablist" aria-label="認証モード切り替え">
      <button
        class="tab"
        :class="{ active: isSignIn }"
        type="button"
        role="tab"
        :aria-selected="isSignIn"
        @click="switchMode('signin')"
      >
        サインイン
      </button>
      <button
        class="tab"
        :class="{ active: !isSignIn }"
        type="button"
        role="tab"
        :aria-selected="!isSignIn"
        @click="switchMode('signup')"
      >
        新規登録
      </button>
    </div>

    <div class="auth-body">
      <form class="form" @submit.prevent="onSubmit">
        <input
          id="email"
          v-model="email"
          type="email"
          autocomplete="email"
          placeholder="メールアドレスをここに入力"
        />

        <div class="password-field">
          <input
            id="password"
            v-model="password"
            :type="showPassword ? 'text' : 'password'"
            :autocomplete="isSignIn ? 'current-password' : 'new-password'"
            placeholder="パスワードをここに入力"
          />
          <button
            class="toggle-visibility"
            type="button"
            :aria-label="showPassword ? 'パスワードを隠す' : 'パスワードを表示'"
            @click="showPassword = !showPassword"
          >
            <svg
              v-if="showPassword"
              class="toggle-icon"
              viewBox="0 0 24 24"
              fill="none"
              aria-hidden="true"
            >
              <path
                d="M3 3l18 18M10.7 10.7a2 2 0 0 0 2.6 2.6M9.9 5.1A10.6 10.6 0 0 1 12 5c5.5 0 9.8 3.7 11 7-0.4 1.1-1.2 2.5-2.3 3.7M6.7 6.7C4.9 8 3.6 9.8 3 12c1.2 3.3 5.5 7 11 7 1.4 0 2.8-0.2 4-0.7"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            <svg
              v-else
              class="toggle-icon"
              viewBox="0 0 24 24"
              fill="none"
              aria-hidden="true"
            >
              <path
                d="M2.3 12s3.6-7 9.7-7 9.7 7 9.7 7-3.6 7-9.7 7-9.7-7-9.7-7Z"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.8" />
            </svg>
          </button>
        </div>

        <div v-if="!isSignIn" class="password-field">
          <input
            id="confirmPassword"
            v-model="confirmPassword"
            :type="showConfirmPassword ? 'text' : 'password'"
            autocomplete="new-password"
            placeholder="確認用パスワードを入力"
          />
          <button
            class="toggle-visibility"
            type="button"
            :aria-label="showConfirmPassword ? '確認用パスワードを隠す' : '確認用パスワードを表示'"
            @click="showConfirmPassword = !showConfirmPassword"
          >
            <svg
              v-if="showConfirmPassword"
              class="toggle-icon"
              viewBox="0 0 24 24"
              fill="none"
              aria-hidden="true"
            >
              <path
                d="M3 3l18 18M10.7 10.7a2 2 0 0 0 2.6 2.6M9.9 5.1A10.6 10.6 0 0 1 12 5c5.5 0 9.8 3.7 11 7-0.4 1.1-1.2 2.5-2.3 3.7M6.7 6.7C4.9 8 3.6 9.8 3 12c1.2 3.3 5.5 7 11 7 1.4 0 2.8-0.2 4-0.7"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            <svg
              v-else
              class="toggle-icon"
              viewBox="0 0 24 24"
              fill="none"
              aria-hidden="true"
            >
              <path
                d="M2.3 12s3.6-7 9.7-7 9.7 7 9.7 7-3.6 7-9.7 7-9.7-7-9.7-7Z"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.8" />
            </svg>
          </button>
        </div>

        <p v-if="!isSignIn" class="signup-note">
          パスワードは半角英数字と半角記号「.」「-」「_」のみで<br>8〜20文字に設定してください。<br />
          英字と数字を必ず1文字以上使用してください。<br />
          メールアドレスの一部をそのままパスワードに設定することは<br>できません。
        </p>

        <label v-if="isSignIn" class="checkbox">
          <input v-model="rememberMe" type="checkbox" />
          ログイン情報を保持する
        </label>

        <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

        <button class="submit" type="submit">{{ submitLabel }}</button>
      </form>

      <p v-if="isSignIn" class="help-link">
        <a href="#">ログインできない方はこちら</a>
      </p>
    </div>
  </section>
</template>

<style scoped>
.auth-card {
  width: min(636px, 100%);
  min-height: 549px;
  padding: 0 22px 24px;
  display: flex;
  flex-direction: column;
  border: 2px solid rgba(241, 234, 219, 0.92);
  border-radius: 12px;
  background:
    linear-gradient(rgba(241, 234, 219, 0.92), rgba(241, 234, 219, 0.92)),
    repeating-linear-gradient(
      90deg,
      rgba(195, 179, 146, 0.16),
      rgba(195, 179, 146, 0.16) 12px,
      rgba(241, 234, 219, 0.08) 12px,
      rgba(241, 234, 219, 0.08) 24px
    );
  box-shadow: 0 22px 34px rgba(84, 68, 39, 0.14);
  animation: authCardRise 650ms cubic-bezier(0.2, 0.8, 0.2, 1) both;
}

.auth-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding-top: 0;
}

.tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  margin: 0 -22px 20px;
}

.tab {
  border: none;
  border-radius: 12px 12px 10px 10px;
  padding: 9px 8px 10px;
  font-size: 35px;
  line-height: 1;
  background: #cdcccd;
  color: #5c5b5b;
  cursor: pointer;
  transition: background 0.2s ease;
}

.tab.active {
  background: rgba(241, 234, 219, 0.6);
  color: #494848;
}

h1 {
  margin: 0 0 14px;
  font-size: 31px;
  line-height: 1.1;
  font-weight: 500;
  color: #4d4b46;
}

.form {
  display: grid;
  gap: 14px;
}

input[type='email'],
input[type='password'],
input[type='text'] {
  width: min(438px, 100%);
  margin-inline: auto;
  border: none;
  border-bottom: 1px solid #7f7a70;
  background: transparent;
  padding: 9px 2px;
  font-size: 15px;
  color: #3e3d3b;
}

input[type='email']:focus,
input[type='password']:focus,
input[type='text']:focus {
  outline: none;
  border-bottom-color: #4f46e5;
}

.password-field {
  width: min(438px, 100%);
  margin-inline: auto;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #7f7a70;
}

.password-field input {
  width: 100%;
  margin-inline: 0;
  border: none;
  border-bottom: none;
  padding-right: 8px;
}

.toggle-visibility {
  border: none;
  background: transparent;
  cursor: pointer;
  color: #65625a;
  line-height: 0;
  padding: 0 2px 4px;
}

.toggle-icon {
  width: 18px;
  height: 18px;
}

.toggle-visibility:hover {
  color: #3e3d3b;
}

.toggle-visibility:focus-visible {
  outline: 2px solid #8e88ea;
  border-radius: 4px;
}

@keyframes authCardRise {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
    box-shadow: 0 8px 16px rgba(84, 68, 39, 0.08);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
    box-shadow: 0 22px 34px rgba(84, 68, 39, 0.14);
  }
}

.checkbox {
  width: min(438px, 100%);
  justify-self: center;
  margin: 2px 0 20px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 7px;
  color: #4f4b44;
  font-size: 16px;
}

.signup-note {
  width: min(438px, 100%);
  justify-self: center;
  margin: -2px 0 4px;
  color: #6a655c;
  font-size: 13px;
  line-height: 1.5;
  text-align: left;
}

.error {
  width: min(438px, 100%);
  justify-self: center;
  margin: -4px 0 0;
  color: #b91c1c;
  font-size: 14px;
  text-align: left;
}

.submit {
  width: min(430px, 100%);
  height: 70px;
  margin-inline: auto;
  margin-top: 14px;
  border: 1px solid #686156;
  border-radius: 15px;
  padding: 0 12px;
  font-size: 35px;
  line-height: 1;
  background: #e0d2ab;
  color: #3e3a35;
  cursor: pointer;
}

.submit:hover {
  background: #d7c696;
}

.help-link {
  margin: 18px 0 0;
  text-align: center;
}

.help-link a {
  color: #4f4b44;
  font-size: 16px;
  text-decoration: none;
}

.help-link a:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .auth-card {
    border-radius: 10px;
    padding: 0 16px 18px;
  }

  .tabs {
    margin: 0 -16px 18px;
  }

  .tab {
    font-size: 29px;
  }

  h1 {
    font-size: 27px;
  }

  .submit {
    font-size: 30px;
  }
}
</style>
