<script setup>
import AuthLayout from './layouts/AuthLayout.vue'
import LoginPage from '../features/auth/pages/LoginPage.vue'
</script>

<template>
  <AuthLayout :show-nav="false">
    <LoginPage />
  </AuthLayout>
</template>
