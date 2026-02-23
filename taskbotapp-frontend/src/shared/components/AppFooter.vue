<script setup>
defineProps({
  showNav: {
    type: Boolean,
    default: true,
  },
})
</script>

<template>
  <footer class="footer">
    <div class="inner">TaskBotApp Frontend</div>
  </footer>
</template>

<style scoped>
.footer {
  border-top: 1px solid #e5e7eb;
  background: #ffffff;
}

.inner {
  width: min(960px, 100%);
  margin: 0 auto;
  padding: 12px 16px;
  color: #6b7280;
  font-size: 14px;
}
</style>
