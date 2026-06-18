<script setup>
</script>

<template>
  <router-view />
</template>

<style scoped>
.simple-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  background: #f3f6f2;
}

.simple-card {
  width: min(520px, calc(100% - 32px));
  padding: 36px;
  text-align: center;
  background: #fff;
  border: 1px solid #dce4da;
  border-radius: 20px;
  box-shadow: 0 20px 50px rgb(31 61 45 / 9%);
}

.eyebrow {
  color: #df7b19;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.13em;
  text-transform: uppercase;
}

.simple-card h1 {
  margin-top: 8px;
  color: #17382a;
}

.simple-card p {
  margin-top: 10px;
  color: #66756d;
}

.logout-button {
  margin-top: 24px;
  padding: 11px 18px;
  color: #fff;
  font: inherit;
  font-weight: 800;
  background: #16854b;
  border: 0;
  border-radius: 10px;
  cursor: pointer;
}
</style>
