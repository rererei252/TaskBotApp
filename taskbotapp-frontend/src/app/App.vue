<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AuthLayout from './layouts/AuthLayout.vue'
import DefaultLayout from './layouts/DefaultLayout.vue'
import TopPage from '../features/home/pages/TopPage.vue'
import AccountPage from '../features/account/pages/AccountPage.vue'
import LoginPage from '../features/auth/pages/LoginPage.vue'
import VerifyPage from '../features/auth/pages/VerifyPage.vue'
import ForgotPassword from '../features/auth/pages/ForgotPassword.vue'
import ResetPasswordPage from '../features/auth/pages/ResetPasswordPage.vue'

const isCheckingSession = ref(true)
const path = computed(() => window.location.pathname)
const isTopPath = computed(() => path.value === '/top')
const isAccountPath = computed(() => path.value === '/account')
const isVerifyPath = computed(() => path.value.startsWith('/auth/verify'))
const isForgotPasswordPath = computed(() => path.value === '/auth/forgot-password')
const isResetPasswordPath = computed(() => path.value === '/auth/password-reset')

const checkSession = async (): Promise<void> => {
  try {
    const response = await fetch('http://localhost:8080/api/auth/me', {
      method: 'GET',
      credentials: 'include',
    })

    if (response.ok) {
      if (window.location.pathname === '/') {
        window.location.href = '/top'
      }
      return
    }

    if (window.location.pathname === '/top' || window.location.pathname === '/account') {
      window.location.href = '/'
    }
  } catch {
    if (window.location.pathname === '/top' || window.location.pathname === '/account') {
      window.location.href = '/'
    }
  } finally {
    isCheckingSession.value = false
  }
}

onMounted(() => {
  void checkSession()
})
</script>

<template>
  <div v-if="isCheckingSession" class="booting" />

  <template v-else>
    <DefaultLayout v-if="isTopPath" :full-width="true">
      <TopPage />
    </DefaultLayout>

    <DefaultLayout v-else-if="isAccountPath" :full-width="true">
      <AccountPage />
    </DefaultLayout>

    <AuthLayout v-else :show-nav="false">
      <VerifyPage v-if="isVerifyPath" />
      <ForgotPassword v-else-if="isForgotPasswordPath" />
      <ResetPasswordPage v-else-if="isResetPasswordPath" />
      <LoginPage v-else />
    </AuthLayout>
  </template>
</template>

<style scoped>
.booting {
  min-height: 100vh;
}
</style>
