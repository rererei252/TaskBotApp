<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

type VerifyState = 'loading' | 'success' | 'error'

type VerifyResponse = {
  message?: string
}

const state = ref<VerifyState>('loading')
const message = ref('認証を確認しています...')

const badgeText = computed(() => (state.value === 'success' ? '初回登録設定' : '認証エラー'))
const titleText = computed(() =>
  state.value === 'success' ? '登録完了しました' : state.value === 'loading' ? '認証中です' : '認証に失敗',
)
const messageClass = computed(() => (state.value === 'error' ? 'desc error' : 'desc'))

const goLogin = () => {
  window.location.href = '/'
}

onMounted(async () => {
  const token = new URLSearchParams(window.location.search).get('token')
  if (!token) {
    state.value = 'error'
    message.value = '認証トークンが見つかりません。'
    return
  }

  try {
    const res = await fetch(`http://localhost:8080/api/auth/verify-token?token=${encodeURIComponent(token)}`)
    const raw = await res.text()

    let body: VerifyResponse = {}
    if (raw) {
      try {
        body = JSON.parse(raw) as VerifyResponse
      } catch {
        body = { message: raw }
      }
    }

    if (!res.ok) {
      state.value = 'error'
      message.value = body.message ?? '認証リンクが不正、または期限切れです'
      return
    }

    state.value = 'success'
    message.value = body.message ?? 'メール認証が完了しました。'
  } catch {
    state.value = 'error'
    message.value = 'サーバーに接続できませんでした。'
  }
})
</script>

<template>
  <div class="verify-overlay">
    <article
      class="verify-popup"
      :class="{ 'error-mode': state === 'error' }"
      role="dialog"
      aria-modal="true"
      aria-label="認証結果"
    >
      <div class="badge">{{ badgeText }}</div>
      <h1 class="title">{{ titleText }}</h1>
      <p :class="messageClass">{{ message }}</p>
      <button class="action" type="button" @click="goLogin">ログイン画面へ</button>
    </article>
  </div>
</template>

<style scoped>
.verify-overlay {
  width: 100%;
  min-height: calc(100vh - 120px);
  display: grid;
  place-items: center;
}

.verify-popup {
  width: min(520px, 100%);
  min-height: 340px;
  border-radius: 16px;
  border: 1px solid rgba(233, 224, 207, 0.95);
  background:
    linear-gradient(rgba(250, 246, 237, 0.84), rgba(250, 246, 237, 0.84)),
    repeating-linear-gradient(
      90deg,
      rgba(195, 179, 146, 0.12),
      rgba(195, 179, 146, 0.12) 12px,
      rgba(248, 244, 236, 0.08) 12px,
      rgba(248, 244, 236, 0.08) 24px
    );
  box-shadow: 0 18px 30px rgba(84, 68, 39, 0.16);
  padding: 32px 36px 30px;
  display: grid;
  gap: 20px;
  justify-items: center;
  align-content: center;
  text-align: center;
}

.verify-popup.error-mode {
  position: relative;
  overflow: visible;
  min-height: 420px;
  padding-top: 64px;
  grid-template-rows: auto auto auto auto;
  align-content: center;
  gap: 18px;
}

.verify-popup.error-mode .title {
  margin: -4px 0 0;
  width: 100%;
  text-align: center;
}

.verify-popup.error-mode .badge {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translate(-50%, -50%);
  margin: 0;
  z-index: 1;
}

.verify-popup.error-mode .desc {
  width: 100%;
  max-width: 420px;
  text-align: center;
  min-height: 0;
}

.badge {
  margin-top: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  width: max-content;
  max-width: calc(100% - 24px);
  background: rgba(248, 244, 236, 0.95);
  border: 1px solid #e5dcc7;
  border-radius: 14px;
  padding: 10px 42px;
  font-size: 30px;
  line-height: 1;
  color: #65625a;
}

.title {
  margin: 8px 0 0;
  font-size: 62px;
  line-height: 1.1;
  color: #69655f;
}

.desc {
  margin: 0;
  min-height: 62px;
  font-size: 20px;
  line-height: 1.5;
  color: #5f5b54;
}

.desc.error {
  color: #b91c1c;
}

.action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: min(430px, 100%);
  height: 70px;
  border: 1px solid #686156;
  border-radius: 15px;
  padding: 0 12px;
  background: #e0d2ab;
  color: #3e3a35;
  font-size: 35px;
  line-height: 1;
  cursor: pointer;
}

.action:hover {
  background: #d7c696;
}

@media (max-width: 480px) {
  .verify-overlay {
    min-height: calc(100vh - 110px);
  }

  .badge {
    font-size: 28px;
  }

  .title {
    font-size: 44px;
  }

  .desc {
    font-size: 20px;
  }

  .action {
    font-size: 30px;
  }
}
</style>
