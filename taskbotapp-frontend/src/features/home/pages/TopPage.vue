<script setup lang="ts">
import { onMounted, ref } from 'vue'

type MeResponse = {
  username?: string
  email?: string
}

const username = ref('ユーザー')
const isLoading = ref(true)
const error = ref('')

const fetchMe = async (): Promise<void> => {
  try {
    const res = await fetch('http://localhost:8080/api/auth/me', {
      method: 'GET',
      credentials: 'include',
    })
    if (!res.ok) {
      window.location.href = '/'
      return
    }

    const body = (await res.json()) as MeResponse
    username.value = body.username || 'ユーザー'
  } catch {
    error.value = 'ユーザー情報を取得できませんでした。'
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  void fetchMe()
})
</script>

<template>
  <section class="top-empty" aria-label="top-page">
    <div class="top-panel">
      <p v-if="isLoading">読み込み中...</p>
      <p v-else>{{ username }}さん、ようこそ</p>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </section>
</template>

<style scoped>
.top-empty {
  min-height: calc(100vh - 120px);
  display: grid;
  place-items: center;
}

.top-panel {
  display: grid;
  gap: 10px;
  justify-items: center;
}

.top-panel p {
  margin: 0;
  color: #4f4b44;
  font-size: 18px;
}

.error {
  color: #b91c1c;
  font-size: 14px;
}
</style>
