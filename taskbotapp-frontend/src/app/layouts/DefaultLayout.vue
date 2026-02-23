<script setup>
import AppHeader from '../../shared/components/AppHeader.vue'
import AppFooter from '../../shared/components/AppFooter.vue'

defineProps({
  showNav: {
    type: Boolean,
    default: true,
  },
})
</script>

<template>
  <div class="layout">
    <AppHeader :show-nav="showNav" />
    <main class="content">
      <slot />
    </main>
    <AppFooter v-if="showNav" />
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: grid;
  grid-template-rows: auto 1fr auto;
}

.content {
  width: min(960px, 100%);
  margin: 0 auto;
  padding: 24px 16px 40px;
}
</style>
