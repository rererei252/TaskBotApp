<script setup lang="ts">
import AppHeader from '../../shared/components/AppHeader.vue'
import AppFooter from '../../shared/components/AppFooter.vue'

const { showNav = true, fullWidth = false } = defineProps<{
  showNav?: boolean
  fullWidth?: boolean
}>()
</script>

<template>
  <div class="layout">
    <AppHeader :show-nav="showNav" />
    <main class="content" :class="{ 'content-full': fullWidth }">
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

.content-full {
  width: 100%;
  margin: 0;
  padding: 24px 0 40px;
}
</style>
