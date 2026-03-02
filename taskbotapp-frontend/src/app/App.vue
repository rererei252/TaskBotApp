<script setup lang="ts">
import { computed } from 'vue'
import AuthLayout from './layouts/AuthLayout.vue'
import LoginPage from '../features/auth/pages/LoginPage.vue'
import VerifyPage from '../features/auth/pages/VerifyPage.vue'

const isVerifyPath = computed(() => window.location.pathname.startsWith('/auth/verify'))
</script>

<template>
  <AuthLayout :show-nav="false">
    <VerifyPage v-if="isVerifyPath" />
    <LoginPage v-else />
  </AuthLayout>
</template>