<script setup lang="ts">
import AppHeader from '../../shared/components/AppHeader.vue'
import AppFooter from '../../shared/components/AppFooter.vue'

const { showNav = true } = defineProps<{
  showNav?: boolean
}>()
</script>

<template>
  <div class="auth-layout">
    <AppHeader :show-nav="showNav" />
    <main class="auth-content">
      <slot />
    </main>
    <AppFooter v-if="showNav" />
  </div>
</template>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: grid;
  grid-template-rows: auto 1fr auto;
  background-image:
    linear-gradient(rgba(251, 247, 236, 0.62), rgba(251, 247, 236, 0.62)),
    url('../../assets/images/backgrounds/auth.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.auth-content {
  width: min(960px, 100%);
  margin: 0 auto;
  padding: 20px 16px 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
