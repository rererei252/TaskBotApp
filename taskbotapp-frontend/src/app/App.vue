<script setup lang="ts">
import { computed } from 'vue'
import AuthLayout from './layouts/AuthLayout.vue'
import LoginPage from '../features/auth/pages/LoginPage.vue'
import VerifyPage from '../features/auth/pages/VerifyPage.vue'
import ForgotPassword from '../features/auth/pages/ForgotPassword.vue'
import ResetPasswordPage from '../features/auth/pages/ResetPasswordPage.vue'

const path = computed(() => window.location.pathname)
const isVerifyPath = computed(() => path.value.startsWith('/auth/verify'))
const isForgotPasswordPath = computed(() => path.value === '/auth/forgot-password')
const isResetPasswordPath = computed(() => path.value === '/auth/password-reset')
</script>

<template>
  <AuthLayout :show-nav="false">
    <VerifyPage v-if="isVerifyPath" />
    <ForgotPassword v-else-if="isForgotPasswordPath" />
    <ResetPasswordPage v-else-if="isResetPasswordPath" />
    <LoginPage v-else />
  </AuthLayout>
</template>