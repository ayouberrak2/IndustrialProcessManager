<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../services/authService'

const router = useRouter()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const errorMessage = ref('')

async function submitLogin() {
  errorMessage.value = ''
  loading.value = true

  try {
    const response = await login(username.value, password.value)
    const user = response.user
    localStorage.setItem('currentUser', JSON.stringify(user))
    if (user.role === 'ADMIN') router.push({ name: 'Admin' })
    else if (user.role === 'CHEF_ATELIER') router.push({ name: 'Chef' })
    else if (user.role === 'TECHNICIEN_LABO') router.push({ name: 'Labo' })
    else router.push({ name: 'Login' })
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <div class="login-shell">
      <section class="login-message">


        <span class="login-badge">OCP Group</span>
        <h1>Espace de gestion industrielle</h1>
        <p>
          Acces reserve aux utilisateurs autorises pour le suivi des ateliers
          et des operations de production.
        </p>

        <div class="login-highlights" aria-label="Informations systeme">
          <div>
            <strong>OCP</strong>
            <span>Environnement interne</span>
          </div>
          <div>
            <strong>24/7</strong>
            <span>Suivi production</span>
          </div>
          <div>
            <strong>Java</strong>
            <span>API locale securisee</span>
          </div>
        </div>
      </section>

      <section class="login-panel">
        <div class="login-header">
          <span>Acces production</span>
          <h2>Connexion</h2>
          <p>Veuillez saisir vos identifiants professionnels.</p>
        </div>

        <form class="login-form" @submit.prevent="submitLogin">
          <label for="username">Nom d'utilisateur</label>
          <div class="input-wrapper">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm7 8a7 7 0 0 0-14 0" />
            </svg>
            <input
              id="username"
              v-model.trim="username"
              name="username"
              type="text"
              placeholder="Saisissez votre identifiant"
              autocomplete="username"
              required
            />
          </div>

          <label for="password">Mot de passe</label>
          <div class="input-wrapper">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <rect x="5" y="10" width="14" height="10" rx="2" />
              <path d="M8 10V7a4 4 0 0 1 8 0v3" />
            </svg>
            <input
              id="password"
              v-model="password"
              name="password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="Saisissez votre mot de passe"
              autocomplete="current-password"
              required
            />
            <button
              class="password-toggle"
              type="button"
              :aria-label="showPassword ? 'Masquer le mot de passe' : 'Afficher le mot de passe'"
              @click="showPassword = !showPassword"
            >
              {{ showPassword ? 'Masquer' : 'Afficher' }}
            </button>
          </div>

          <div class="login-options">
            <a href="#" class="password-reset" @click.prevent>Mot de passe oublie ?</a>
          </div>

          <p v-if="errorMessage" class="error-message" role="alert">
            {{ errorMessage }}
          </p>

          <button class="submit-button" type="submit" :disabled="loading">
            <span v-if="loading" class="spinner" aria-hidden="true"></span>
            {{ loading ? 'Connexion...' : 'Se connecter' }}
          </button>
        </form>

        <p class="security-note">
          <span aria-hidden="true"></span>
          Acces controle par le serveur Java
        </p>
      </section>
    </div>
  </main>
</template>
