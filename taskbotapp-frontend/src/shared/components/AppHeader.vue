<script setup lang="ts">
import { onMounted, ref } from 'vue'

const { showNav = true } = defineProps<{
  showNav?: boolean
}>()

type MeResponse = {
  username?: string
}

const username = ref('ユーザ名')

const fetchCurrentUser = async (): Promise<void> => {
  if (!showNav) return

  try {
    const res = await fetch('http://localhost:8080/api/auth/me', {
      method: 'GET',
      credentials: 'include',
    })
    if (!res.ok) return

    const body = (await res.json()) as MeResponse
    if (body.username && body.username.trim()) {
      username.value = body.username
    }
  } catch {
    // Keep fallback label.
  }
}

onMounted(() => {
  void fetchCurrentUser()
})
</script>

<template>
  <header class="header">
    <div class="inner">
      <strong>タスク缶</strong>
      <nav v-if="showNav" class="nav" aria-label="global navigation">
        <a href="/top">Top</a>
        <a href="#">タスク一覧</a>
        <a href="#" aria-label="通知">🔔</a>
        <div class="user-menu">
          <button class="user-trigger" type="button">
            {{ username }} <span class="caret">▼</span>
          </button>
          <div class="dropdown">
            <a href="/account">アカウント設定</a>
            <button type="button" @click="$emit('logout')">ログアウト</button>
          </div>
        </div>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.header {
  background: rgba(230, 216, 188, 0.8);
}

.inner {
  width: 100%;
  margin: 0;
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

strong {
  font-family: inherit;
  font-size: 30px;
  font-weight: 700;
  color: #5c5b5b;
}

.nav {
  display: inline-flex;
  align-items: center;
}

.nav > * {
  font-family: inherit;
  padding: 0 14px;
  line-height: 1;
  font-size: 20px;
  color: #2f2c28;
}

.nav > * + * {
  border-left: 1px solid rgba(47, 44, 40, 0.5);
}

.nav a {
  color: #2f2c28;
  text-decoration: none;
  font-weight: 500;
}

.user-menu {
  position: relative;
}

.user-menu::after {
  content: '';
  position: absolute;
  top: 100%;
  right: 0;
  width: 220px;
  height: 14px;
}

.user-trigger {
  border: none;
  background: transparent;
  font: inherit;
  color: inherit;
  cursor: pointer;
  padding: 0;
}

.dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  min-width: 190px;
  padding: 8px;
  border: 1px solid rgba(47, 44, 40, 0.22);
  border-radius: 10px;
  background: rgba(248, 243, 233, 0.96);
  box-shadow: 0 10px 22px rgba(47, 44, 40, 0.18);
  display: grid;
  gap: 6px;
  z-index: 20;
  opacity: 0;
  transform: translateY(-6px);
  visibility: hidden;
  pointer-events: none;
  transition:
    opacity 0.2s ease,
    transform 0.2s ease,
    visibility 0s linear 0.2s;
}

.user-menu:hover .dropdown,
.user-menu:focus-within .dropdown {
  opacity: 1;
  transform: translateY(0);
  visibility: visible;
  pointer-events: auto;
  transition-delay: 0.2s, 0.2s, 0s;
}

.dropdown a,
.dropdown button {
  font: inherit;
  color: #2f2c28;
  text-decoration: none;
  border: none;
  background: transparent;
  text-align: left;
  padding: 8px 10px;
  border-radius: 7px;
  cursor: pointer;
}

.dropdown a:hover,
.dropdown button:hover {
  background: rgba(47, 44, 40, 0.08);
}

.caret {
  font-size: 12px;
  margin-left: 6px;
}
</style>
