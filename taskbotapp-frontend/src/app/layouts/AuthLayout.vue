<script setup>
import AppHeader from '../../shared/components/AppHeader.vue'
import AppFooter from '../../shared/components/AppFooter.vue'
import authBackground from '../../assets/images/backgrounds/auth.png'

defineProps({
  showNav: {
    type: Boolean,
    default: true,
  },
})

const backdropStyle = {
  backgroundImage: `linear-gradient(rgba(231, 222, 202, 0.35), rgba(231, 222, 202, 0.35)), url(${authBackground})`,
}
</script>

<template>
  <div class="auth-layout">
    <div class="backdrop" :style="backdropStyle"></div>
    <AppHeader :show-nav="showNav" />
    <main class="content">
      <slot />
    </main>
    <AppFooter v-if="showNav" />
  </div>
</template>

<style scoped>
.auth-layout {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-rows: auto 1fr auto;
  background: #e7deca;
}

.backdrop {
  position: absolute;
  inset: 0;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  opacity: 1;
  pointer-events: none;
  z-index: 0;
}

.auth-layout > :not(.backdrop) {
  position: relative;
  z-index: 1;
}

.content {
  display: grid;
  place-items: center;
  padding: 20px 12px;
}
</style>
