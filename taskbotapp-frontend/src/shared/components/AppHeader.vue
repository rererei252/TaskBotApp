<script setup>
defineProps({
  showNav: {
    type: Boolean,
    default: true,
  },
})
</script>

<template>
  <header class="header">
    <div class="inner">
      <strong>タスク缶</strong>
      <nav v-if="showNav" class="nav">
        <a href="#">Login</a>
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
  font-size: 18px;
  font-weight: 700;
  color: #5c5b5b;
}

.nav a {
  color: #2563eb;
  text-decoration: none;
  font-weight: 600;
}
</style>
