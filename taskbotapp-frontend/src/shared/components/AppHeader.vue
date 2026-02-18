<template>
  <header class="header">
    <div class="inner">
      <strong>TaskBotApp</strong>
      <nav class="nav">
        <a href="#">Tasks</a>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.header {
  border-bottom: 1px solid #e5e7eb;
  background: #ffffff;
}

.inner {
  width: min(960px, 100%);
  margin: 0 auto;
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav a {
  color: #2563eb;
  text-decoration: none;
  font-weight: 600;
}
</style>
