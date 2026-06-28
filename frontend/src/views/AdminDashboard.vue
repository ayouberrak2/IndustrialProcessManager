<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import ocpLogo from '../assets/ocp-logo.png'
import {
  createAtelier,
  createUser,
  deleteAtelier,
  deleteUser,
  getAteliers,
  getDashboardStats,
  getUsers,
  updateAtelier,
  updateUser,
} from '../services/adminService'

const savedUser = localStorage.getItem('currentUser')
const user = ref(savedUser ? JSON.parse(savedUser) : null)

const router = useRouter()
const route = useRoute()

const activeTab = ref('dashboard')
const errorMessage = ref('')
const successMessage = ref('')

const stats = ref({
  totalUsers: 0,
  totalAdmins: 0,
  totalChefsAtelier: 0,
  totalTechniciensLabo: 0,
  totalAteliers: 0,
  totalEquipements: 0,
  totalOperations: 0,
  totalLots: 0,
  recentOperations: [],
})

const users = ref([])
const ateliers = ref([])
const showUserModal = ref(false)
const showAtelierModal = ref(false)

const roles = [
  { value: 'ADMIN', label: 'Admin' },
  { value: 'CHEF_ATELIER', label: 'Chef atelier' },
  { value: 'TECHNICIEN_LABO', label: 'Technicien labo' },
]

const chefUsers = computed(() => users.value.filter((item) => item.role === 'CHEF_ATELIER'))
const availableChefUsers = computed(() =>
  chefUsers.value.filter((chef) => {
    return !ateliers.value.some((atelier) => {
      return atelier.chefAtelierId === chef.id && atelier.id !== atelierForm.value.id
    })
  }),
)

const monthNames = [
  'Janvier',
  'Fevrier',
  'Mars',
  'Avril',
  'Mai',
  'Juin',
  'Juillet',
  'Aout',
  'Septembre',
  'Octobre',
  'Novembre',
  'Decembre',
]

const weekDays = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim']
const calendarDate = ref(new Date())
const calendarTitle = ref('')
const calendarDays = ref([])

const userForm = ref(emptyUserForm())
const atelierForm = ref(emptyAtelierForm())

function emptyUserForm() {
  return {
    id: null,
    username: '',
    password: '',
    role: 'CHEF_ATELIER',
    email: '',
    atelierId: '',
  }
}

function emptyAtelierForm() {
  return {
    id: null,
    codeAtelier: '',
    nomAtelier: '',
    estActif: true,
    chefAtelierId: '',
  }
}

async function loadAll() {
  await Promise.all([loadDashboard(), loadUsers(), loadAteliers()])
}

async function loadDashboard() {
  stats.value = await getDashboardStats()
}

async function loadUsers() {
  users.value = await getUsers()
}

async function loadAteliers() {
  ateliers.value = await getAteliers()
}

async function submitUser() {
  clearMessages()

  try {
    const userToSave = {
      ...userForm.value,
      atelierId: userForm.value.role === 'TECHNICIEN_LABO' ? userForm.value.atelierId : null,
    }

    if (userForm.value.id) {
      await updateUser(userForm.value.id, userToSave)
      successMessage.value = 'Utilisateur modifie avec succes'
    } else {
      await createUser(userToSave)
      successMessage.value = 'Utilisateur cree avec succes'
    }

    userForm.value = emptyUserForm()
    showUserModal.value = false
    await loadUsers()
    await loadDashboard()
  } catch (error) {
    errorMessage.value = error.message
  }
}

function editUser(user) {
  userForm.value = {
    id: user.id,
    username: user.username,
    password: '',
    role: user.role,
    email: user.email,
    atelierId: user.atelierId || '',
  }
  showUserModal.value = true
}

function resetUserForm() {
  userForm.value = emptyUserForm()
}

function openUserModal() {
  userForm.value = emptyUserForm()
  showUserModal.value = true
}

function closeUserModal() {
  showUserModal.value = false
  userForm.value = emptyUserForm()
}

async function removeUser(id) {
  clearMessages()

  try {
    await deleteUser(id)
    successMessage.value = 'Utilisateur supprime'
    await loadUsers()
    await loadDashboard()
  } catch (error) {
    errorMessage.value = error.message
  }
}

async function submitAtelier() {
  clearMessages()

  try {
    if (atelierForm.value.id) {
      await updateAtelier(atelierForm.value.id, atelierForm.value)
      successMessage.value = 'Atelier modifie avec succes'
    } else {
      await createAtelier(atelierForm.value)
      successMessage.value = 'Atelier cree avec succes'
    }

    atelierForm.value = emptyAtelierForm()
    showAtelierModal.value = false
    await loadAteliers()
    await loadUsers()
    await loadDashboard()
  } catch (error) {
    errorMessage.value = error.message
  }
}

function editAtelier(atelier) {
  atelierForm.value = {
    id: atelier.id,
    codeAtelier: atelier.codeAtelier,
    nomAtelier: atelier.nomAtelier,
    estActif: atelier.estActif,
    chefAtelierId: atelier.chefAtelierId || '',
  }
  showAtelierModal.value = true
}

function resetAtelierForm() {
  atelierForm.value = emptyAtelierForm()
}

function openAtelierModal() {
  atelierForm.value = emptyAtelierForm()
  showAtelierModal.value = true
}

function closeAtelierModal() {
  showAtelierModal.value = false
  atelierForm.value = emptyAtelierForm()
}

async function removeAtelier(id) {
  clearMessages()

  try {
    await deleteAtelier(id)
    successMessage.value = 'Atelier supprime'
    await loadAteliers()
    await loadUsers()
    await loadDashboard()
  } catch (error) {
    errorMessage.value = error.message
  }
}

function clearMessages() {
  errorMessage.value = ''
  successMessage.value = ''
}

function logout() {
  localStorage.removeItem('currentUser')
  router.push({ name: 'Login' })
}

function buildCalendar() {
  const year = calendarDate.value.getFullYear()
  const month = calendarDate.value.getMonth()
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const today = new Date()
  const emptyDaysBeforeMonth = (firstDay.getDay() + 6) % 7
  const days = []

  calendarTitle.value = `${monthNames[month]} ${year}`

  for (let index = 0; index < emptyDaysBeforeMonth; index++) {
    days.push({
      label: '',
      isEmpty: true,
      isToday: false,
    })
  }

  for (let day = 1; day <= lastDay.getDate(); day++) {
    const isToday =
      day === today.getDate() &&
      month === today.getMonth() &&
      year === today.getFullYear()

    days.push({
      label: day,
      isEmpty: false,
      isToday,
    })
  }

  calendarDays.value = days
}

function previousMonth() {
  const year = calendarDate.value.getFullYear()
  const month = calendarDate.value.getMonth()

  calendarDate.value = new Date(year, month - 1, 1)
  buildCalendar()
}

function nextMonth() {
  const year = calendarDate.value.getFullYear()
  const month = calendarDate.value.getMonth()

  calendarDate.value = new Date(year, month + 1, 1)
  buildCalendar()
}

onMounted(async () => {
  buildCalendar()

  // initialize active tab from query param `page` if present
  const q = route.query.page
  if (q) activeTab.value = q

  try {
    await loadAll()
  } catch (error) {
    errorMessage.value = error.message
  }
})

// keep activeTab in sync with URL query so refresh preserves tab
watch(activeTab, (val) => {
  const query = val && val !== 'dashboard' ? { page: val } : {}
  router.replace({ name: 'Admin', query })
})
</script>

<template>
  <main class="admin-page">
    <aside class="admin-sidebar">
      <div class="admin-brand">
        <img class="brand-logo" :src="ocpLogo" alt="Logo OCP" />
        <div>
          <strong>OCP System</strong>
          <small>Industrial Manager</small>
        </div>
      </div>

      <nav class="admin-menu">
        <button
          type="button"
          :class="{ active: activeTab === 'dashboard' }"
          @click="activeTab = 'dashboard'"
        >
          <span class="menu-icon">D</span>
          Dashboard global
        </button>

        <button
          type="button"
          :class="{ active: activeTab === 'users' }"
          @click="activeTab = 'users'"
        >
          <span class="menu-icon">U</span>
          Gestion users
        </button>

        <button
          type="button"
          :class="{ active: activeTab === 'ateliers' }"
          @click="activeTab = 'ateliers'"
        >
          <span class="menu-icon">A</span>
          Gestion ateliers
        </button>
      </nav>

      <div class="sidebar-card">
        <span class="status-dot"></span>
        <div>
          <strong>Session active</strong>
          <small>Service admin connecte</small>
        </div>
      </div>

      <button class="logout-link" type="button" @click="logout">
        Se deconnecter
      </button>
    </aside>

    <section class="admin-content">
      <header class="admin-topbar">
        <div>
          <p class="eyebrow">Administration</p>
          <h1>Tableau de bord</h1>
          <p class="topbar-subtitle">
            Connecte en tant que {{ user.username }}. Gestion des acces,
            ateliers et indicateurs principaux.
          </p>
        </div>

        <div class="topbar-actions">
          <button class="soft-button no-margin" type="button" @click="loadAll">
            Actualiser
          </button>
          <span class="role-pill">{{ user.role }}</span>
        </div>
      </header>

      <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
      <p v-if="successMessage" class="alert alert-success">{{ successMessage }}</p>

      <section v-if="activeTab === 'dashboard'" class="panel">
        <div class="section-title">
          <div>
            <p class="eyebrow">Apercu general</p>
            <h2>Dashboard global</h2>
            <p>
              Les indicateurs principaux pour suivre rapidement la partie admin.
            </p>
          </div>

          <button class="soft-button no-margin" type="button" @click="loadDashboard">
            Actualiser
          </button>
        </div>

        <div class="stats-grid">
          <article class="stat-card">
            <span>Users</span>
            <strong>{{ stats.totalUsers }}</strong>
          </article>

          <article class="stat-card">
            <span>Ateliers</span>
            <strong>{{ stats.totalAteliers }}</strong>
          </article>

          <article class="stat-card">
            <span>Equipements</span>
            <strong>{{ stats.totalEquipements }}</strong>
          </article>

          <article class="stat-card">
            <span>Operations</span>
            <strong>{{ stats.totalOperations }}</strong>
          </article>
        </div>

        <div class="dashboard-bottom">
          <div class="recent-section">
            <div class="section-title">
              <div>
                <p class="eyebrow">Suivi process</p>
                <h2>Operations recentes</h2>
              </div>
            </div>

            <div class="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Ordre fab</th>
                    <th>Type</th>
                    <th>Statut</th>
                    <th>Date debut</th>
                    <th>Date fin</th>
                  </tr>
                </thead>

                <tbody>
                  <tr v-if="stats.recentOperations.length === 0">
                    <td class="empty-cell" colspan="5">Aucune operation recente</td>
                  </tr>

                  <tr v-for="operation in stats.recentOperations" :key="operation.id">
                    <td class="strong-cell">{{ operation.numOrdreFab }}</td>
                    <td>{{ operation.typeOperation }}</td>
                    <td><span class="mini-pill">{{ operation.statutOperation }}</span></td>
                    <td>{{ operation.dateDebut }}</td>
                    <td>{{ operation.dateFin || 'Non terminee' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="calendar-card">
            <div class="calendar-header">
              <button type="button" @click="previousMonth">Prev</button>
              <strong>{{ calendarTitle }}</strong>
              <button type="button" @click="nextMonth">Next</button>
            </div>

            <div class="calendar-weekdays">
              <span v-for="day in weekDays" :key="day">{{ day }}</span>
            </div>

            <div class="calendar-grid">
              <span
                v-for="(day, index) in calendarDays"
                :key="index"
                :class="{ empty: day.isEmpty, today: day.isToday }"
              >
                {{ day.label }}
              </span>
            </div>
          </div>
        </div>
      </section>

      <section v-if="activeTab === 'users'" class="panel users-panel">
        <div class="users-header">
          <div>
            <p class="eyebrow">Gestion des acces</p>
            <h2>Utilisateurs</h2>
            <p>Les techniciens labo choisissent un atelier. Les chefs sont assignes depuis Gestion ateliers.</p>
          </div>

          <div class="detail-cards">
            <article>
              <span>Total users</span>
              <strong>{{ stats.totalUsers }}</strong>
            </article>
            <article>
              <span>Chefs atelier</span>
              <strong>{{ stats.totalChefsAtelier }}</strong>
            </article>
            <article>
              <span>Techniciens labo</span>
              <strong>{{ stats.totalTechniciensLabo }}</strong>
            </article>
          </div>

          <button class="primary-button no-margin" type="button" @click="openUserModal">
            Ajouter user
          </button>
        </div>

        <div class="users-layout">
        <div class="table-card user-list-card">
          <div class="section-title">
            <div>
              <p class="eyebrow">Affectation atelier</p>
              <h2>Liste des users</h2>
            </div>

            <button class="soft-button no-margin" type="button" @click="loadUsers">
              Reload
            </button>
          </div>

          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Role</th>
                  <th>Atelier</th>
                  <th>Email</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                <tr v-if="users.length === 0">
                  <td class="empty-cell" colspan="5">Aucun utilisateur trouve</td>
                </tr>

                <tr v-for="item in users" :key="item.id">
                  <td>
                    <div class="cell-detail">
                      <strong>{{ item.username }}</strong>
                      <small>Utilisateur systeme</small>
                    </div>
                  </td>
                  <td><span class="mini-pill">{{ item.role }}</span></td>
                  <td>
                    <div class="cell-detail">
                      <strong>{{ item.atelierName || 'Aucun atelier' }}</strong>
                      <small>{{ item.atelierId ? 'Affecte a un atelier' : 'Non affecte' }}</small>
                    </div>
                  </td>
                  <td>{{ item.email }}</td>
                  <td class="actions">
                    <button type="button" @click="editUser(item)">Modifier</button>
                    <button type="button" class="danger" @click="removeUser(item.id)">
                      Supprimer
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        </div>
      </section>

      <section v-if="activeTab === 'ateliers'" class="panel ateliers-panel">
        <div class="users-header">
          <div>
            <p class="eyebrow">Structure industrielle</p>
            <h2>Ateliers</h2>
            <p>Chaque atelier doit avoir un chef atelier responsable.</p>
          </div>

          <div class="detail-cards">
            <article>
              <span>Total ateliers</span>
              <strong>{{ stats.totalAteliers }}</strong>
            </article>
            <article>
              <span>Chefs libres</span>
              <strong>{{ availableChefUsers.length }}</strong>
            </article>
            <article>
              <span>Equipements</span>
              <strong>{{ stats.totalEquipements }}</strong>
            </article>
          </div>

          <button class="primary-button no-margin" type="button" @click="openAtelierModal">
            Ajouter atelier
          </button>
        </div>

        <div class="users-layout ateliers-layout">
        <div class="table-card user-list-card">
          <div class="section-title">
            <div>
              <p class="eyebrow">Responsabilites</p>
              <h2>Liste des ateliers</h2>
            </div>

            <button class="soft-button no-margin" type="button" @click="loadAteliers">
              Reload
            </button>
          </div>

          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Nom</th>
                  <th>Chef atelier</th>
                  <th>Equipe</th>
                  <th>Etat</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                <tr v-if="ateliers.length === 0">
                  <td class="empty-cell" colspan="6">Aucun atelier trouve</td>
                </tr>

                <tr v-for="atelier in ateliers" :key="atelier.id">
                  <td class="strong-cell">{{ atelier.codeAtelier }}</td>
                  <td>{{ atelier.nomAtelier }}</td>
                  <td>
                    <div class="cell-detail">
                      <strong>{{ atelier.chefAtelierName || 'Non assigne' }}</strong>
                      <small>{{ atelier.chefAtelierEmail || 'Aucun email' }}</small>
                    </div>
                  </td>
                  <td>
                    <div class="cell-detail">
                      <strong>{{ atelier.nombreUsers }} users</strong>
                      <small>{{ atelier.nombreTechniciens }} techniciens labo</small>
                    </div>
                  </td>
                  <td>
                    <span class="mini-pill" :class="{ inactive: !atelier.estActif }">
                      {{ atelier.estActif ? 'Actif' : 'Inactif' }}
                    </span>
                  </td>
                  <td class="actions">
                    <button type="button" @click="editAtelier(atelier)">Modifier</button>
                    <button type="button" class="danger" @click="removeAtelier(atelier.id)">
                      Supprimer
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        </div>
      </section>

      <div v-if="showUserModal" class="modal-backdrop">
        <div class="modal-card">
          <div class="modal-header">
            <div>
              <p class="eyebrow">{{ userForm.id ? 'Modification' : 'Creation' }}</p>
              <h2>{{ userForm.id ? 'Modifier utilisateur' : 'Nouvel utilisateur' }}</h2>
            </div>
            <button class="modal-close" type="button" @click="closeUserModal">Fermer</button>
          </div>

          <form class="admin-form modal-form" @submit.prevent="submitUser">
            <div class="form-grid">
              <div>
                <label>Username</label>
                <input v-model.trim="userForm.username" required placeholder="ex: chef" />
              </div>

              <div>
                <label>Email</label>
                <input v-model.trim="userForm.email" required type="email" placeholder="user@ocp.local" />
              </div>

              <div>
                <label>Mot de passe</label>
                <input
                  v-model="userForm.password"
                  :required="!userForm.id"
                  type="password"
                  :placeholder="userForm.id ? 'Laisser vide pour garder ancien mot de passe' : 'Mot de passe'"
                />
              </div>

              <div>
                <label>Role</label>
                <select v-model="userForm.role">
                  <option v-for="role in roles" :key="role.value" :value="role.value">
                    {{ role.label }}
                  </option>
                </select>
              </div>

              <div v-if="userForm.role === 'TECHNICIEN_LABO'" class="full-field">
                <label>Atelier</label>
                <select v-model.number="userForm.atelierId" required>
                  <option disabled value="">Choisir un atelier</option>
                  <option v-for="atelier in ateliers" :key="atelier.id" :value="atelier.id">
                    {{ atelier.codeAtelier }} - {{ atelier.nomAtelier }}
                  </option>
                </select>
              </div>

              <p v-if="userForm.role === 'CHEF_ATELIER'" class="form-help full-field">
                Le chef atelier sera affecte depuis Gestion ateliers lors de la creation ou modification d'un atelier.
              </p>
            </div>

            <div class="modal-actions">
              <button class="soft-button no-margin" type="button" @click="closeUserModal">
                Annuler
              </button>
              <button class="primary-button no-margin" type="submit">
                {{ userForm.id ? 'Modifier' : 'Creer' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <div v-if="showAtelierModal" class="modal-backdrop">
        <div class="modal-card">
          <div class="modal-header">
            <div>
              <p class="eyebrow">{{ atelierForm.id ? 'Modification' : 'Creation' }}</p>
              <h2>{{ atelierForm.id ? 'Modifier atelier' : 'Nouvel atelier' }}</h2>
            </div>
            <button class="modal-close" type="button" @click="closeAtelierModal">Fermer</button>
          </div>

          <form class="admin-form modal-form" @submit.prevent="submitAtelier">
            <div class="form-grid">
              <div>
                <label>Code atelier</label>
                <input v-model.trim="atelierForm.codeAtelier" required placeholder="ATL-02" />
              </div>

              <div>
                <label>Nom atelier</label>
                <input v-model.trim="atelierForm.nomAtelier" required placeholder="Atelier broyage" />
              </div>

              <div class="full-field">
                <label>Chef atelier unique</label>
                <select v-model.number="atelierForm.chefAtelierId" required>
                  <option disabled value="">Choisir un chef disponible</option>
                  <option v-for="chef in availableChefUsers" :key="chef.id" :value="chef.id">
                    {{ chef.username }} - {{ chef.email }}
                  </option>
                </select>
                <p v-if="availableChefUsers.length === 0" class="form-help">
                  Aucun chef libre. Un chef ne peut pas etre responsable de deux ateliers.
                </p>
              </div>

              <label class="checkbox-line full-field">
                <input v-model="atelierForm.estActif" type="checkbox" />
                Atelier actif
              </label>
            </div>

            <div class="modal-actions">
              <button class="soft-button no-margin" type="button" @click="closeAtelierModal">
                Annuler
              </button>
              <button class="primary-button no-margin" type="submit">
                {{ atelierForm.id ? 'Modifier' : 'Creer' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
.admin-page {
  --green: #0f7a3f;
  --green-dark: #07582d;
  --green-light: #e8f5ee;
  --gray-page: #f2f4f7;
  --gray-border: #dfe3e8;
  --gray-text: #5f6b7a;
  --title: #1f2937;

  display: grid;
  height: 100vh;
  grid-template-columns: 272px 1fr;
  color: var(--title);
  background: var(--gray-page);
}

.admin-sidebar {
  display: flex;
  position: sticky;
  top: 0;
  height: 100vh;
  padding: 18px 14px;
  flex-direction: column;
  color: #ffffff;
  background: #0b3d2b;
  border-right: 1px solid #0a3325;
}

.admin-brand {
  display: flex;
  padding: 12px 10px 18px;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgb(255 255 255 / 12%);
}

.brand-logo {
  width: 64px;
  height: 46px;
  object-fit: contain;
}

.admin-brand strong,
.admin-brand small {
  display: block;
}

.admin-brand strong {
  color: #ffffff;
  font-size: 17px;
}

.admin-brand small {
  margin-top: 2px;
  color: rgb(255 255 255 / 65%);
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
}

.admin-menu {
  display: grid;
  gap: 8px;
  margin-top: 22px;
}

.admin-menu button {
  position: relative;
  display: flex;
  width: 100%;
  padding: 11px 10px;
  align-items: center;
  gap: 10px;
  color: rgb(255 255 255 / 82%);
  font: inherit;
  font-size: 14px;
  font-weight: 700;
  text-align: left;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 10px;
  cursor: pointer;
}

.admin-menu button:hover {
  background: rgb(255 255 255 / 8%);
  border-color: rgb(255 255 255 / 10%);
}

.admin-menu button.active {
  color: #0b3d2b;
  background: #ffffff;
  border-color: #ffffff;
}

.admin-menu button.active::before {
  position: absolute;
  width: 4px;
  top: 10px;
  bottom: 10px;
  left: -1px;
  content: "";
  background: var(--green);
  border-radius: 0 999px 999px 0;
}

.menu-icon {
  display: grid;
  width: 28px;
  height: 28px;
  place-items: center;
  color: #ffffff;
  font-size: 11px;
  font-weight: 800;
  background: rgb(255 255 255 / 14%);
  border-radius: 7px;
}

.admin-menu button.active .menu-icon {
  color: #ffffff;
  background: var(--green);
}

.sidebar-card {
  display: flex;
  margin-top: auto;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: rgb(255 255 255 / 8%);
  border: 1px solid rgb(255 255 255 / 12%);
  border-radius: 12px;
}

.status-dot {
  width: 10px;
  height: 10px;
  flex: 0 0 auto;
  background: #4ade80;
  border-radius: 50%;
}

.sidebar-card strong,
.sidebar-card small {
  display: block;
}

.sidebar-card strong {
  color: #ffffff;
  font-size: 13px;
}

.sidebar-card small {
  color: rgb(255 255 255 / 65%);
  font-size: 12px;
}

.logout-link {
  margin-top: 12px;
  padding: 10px;
  color: #ffffff;
  font: inherit;
  font-size: 14px;
  font-weight: 700;
  background: rgb(255 255 255 / 8%);
  border: 1px solid rgb(255 255 255 / 12%);
  border-radius: 8px;
  cursor: pointer;
}

.logout-link:hover {
  background: rgb(255 255 255 / 14%);
}

.admin-content {
  padding: 24px;
  overflow: auto;
  height: 100vh;
}

@media (max-width: 820px) {
  .admin-page {
    height: auto;
  }

  .admin-sidebar {
    position: static;
    height: auto;
  }

  .admin-content {
    height: auto;
    overflow: visible;
  }
}

.admin-topbar,
.panel {
  background: #ffffff;
  border: 1px solid var(--gray-border);
  border-radius: 10px;
}

.admin-topbar {
  display: flex;
  padding: 18px 20px;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.eyebrow {
  color: var(--green);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.admin-topbar h1,
.panel h2,
.admin-form h2 {
  margin-top: 4px;
  color: var(--title);
}

.admin-topbar h1 {
  font-size: 26px;
  line-height: 1.2;
}

.topbar-subtitle {
  margin-top: 6px;
  color: var(--gray-text);
  font-size: 14px;
}

.topbar-actions,
.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.section-title p:last-child {
  margin-top: 6px;
  color: var(--gray-text);
  font-size: 14px;
}

.role-pill,
.mini-pill {
  display: inline-flex;
  padding: 5px 9px;
  color: var(--green-dark);
  font-size: 12px;
  font-weight: 800;
  background: var(--green-light);
  border: 1px solid #b9e4ca;
  border-radius: 999px;
}

.mini-pill.inactive {
  color: #8a5b11;
  background: #fff7df;
  border-color: #f2dda0;
}

.panel {
  margin-top: 18px;
  padding: 18px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(150px, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.stat-card {
  padding: 14px;
  background: #ffffff;
  border: 1px solid var(--gray-border);
  border-left: 3px solid var(--green);
  border-radius: 8px;
}

.stat-card span,
.stat-card strong {
  display: block;
}

.stat-card span {
  color: var(--gray-text);
  font-size: 13px;
  font-weight: 700;
}

.stat-card strong {
  margin-top: 6px;
  color: var(--title);
  font-size: 28px;
}

.dashboard-bottom {
  display: grid;
  grid-template-columns: 1fr 310px;
  gap: 16px;
  margin-top: 22px;
}

.recent-section,
.calendar-card {
  padding: 16px;
  background: #ffffff;
  border: 1px solid var(--gray-border);
  border-radius: 8px;
}

.recent-section .table-wrapper {
  margin-top: 12px;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.calendar-header strong {
  color: var(--title);
  font-size: 15px;
}

.calendar-header button {
  padding: 6px 9px;
  color: var(--green-dark);
  font: inherit;
  font-size: 12px;
  font-weight: 700;
  background: var(--green-light);
  border: 1px solid #b9e4ca;
  border-radius: 6px;
  cursor: pointer;
}

.calendar-weekdays,
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}

.calendar-weekdays {
  margin-top: 14px;
}

.calendar-weekdays span {
  color: var(--gray-text);
  font-size: 12px;
  font-weight: 800;
  text-align: center;
}

.calendar-grid {
  margin-top: 8px;
}

.calendar-grid span {
  display: grid;
  min-height: 34px;
  place-items: center;
  color: #344054;
  font-size: 13px;
  background: #f8fafb;
  border: 1px solid #edf0f3;
  border-radius: 6px;
}

.calendar-grid span.empty {
  background: transparent;
  border-color: transparent;
}

.calendar-grid span.today {
  color: #ffffff;
  font-weight: 900;
  background: var(--green);
  border-color: var(--green);
}

.two-columns {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
  align-items: start;
}

.users-panel {
  padding: 0;
  overflow: hidden;
}

.ateliers-panel {
  padding: 0;
  overflow: hidden;
}

.users-header {
  display: flex;
  padding: 18px;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  background: #f8fafb;
  border-bottom: 1px solid var(--gray-border);
}

.users-header p:last-child {
  margin-top: 6px;
  color: var(--gray-text);
  font-size: 14px;
}

.detail-cards {
  display: grid;
  min-width: 420px;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.detail-cards article {
  padding: 12px;
  background: #ffffff;
  border: 1px solid var(--gray-border);
  border-radius: 8px;
}

.detail-cards span,
.detail-cards strong {
  display: block;
}

.detail-cards span {
  color: var(--gray-text);
  font-size: 12px;
  font-weight: 700;
}

.detail-cards strong {
  margin-top: 4px;
  color: var(--green-dark);
  font-size: 22px;
  line-height: 1;
}

.users-layout {
  display: grid;
  padding: 18px;
  grid-template-columns: 1fr;
  gap: 18px;
  align-items: start;
}

.ateliers-layout {
  grid-template-columns: 1fr;
}

.admin-form,
.table-card {
  padding: 16px;
  background: #ffffff;
  border: 1px solid var(--gray-border);
  border-radius: 8px;
}

.user-form-card {
  background: #fbfcfd;
}

.user-list-card {
  min-width: 0;
}

.form-help {
  margin-top: 8px;
  color: #8a5b11;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.5;
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 30;
  display: grid;
  padding: 20px;
  place-items: center;
  background: rgb(15 23 42 / 48%);
}

.modal-card {
  width: min(760px, 100%);
  max-height: calc(100vh - 40px);
  overflow-y: auto;
  background: #ffffff;
  border: 1px solid var(--gray-border);
  border-radius: 12px;
  box-shadow: 0 22px 70px rgb(15 23 42 / 22%);
}

.modal-header {
  display: flex;
  padding: 18px 20px;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background: #f8fafb;
  border-bottom: 1px solid var(--gray-border);
}

.modal-close {
  padding: 8px 10px;
  color: #344054;
  font: inherit;
  font-size: 13px;
  font-weight: 700;
  background: #ffffff;
  border: 1px solid var(--gray-border);
  border-radius: 6px;
  cursor: pointer;
}

.modal-form {
  padding: 20px;
  background: #ffffff;
  border: 0;
  border-radius: 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px 16px;
}

.full-field {
  grid-column: 1 / -1;
}

.modal-actions {
  display: flex;
  margin-top: 20px;
  justify-content: flex-end;
  gap: 10px;
}

.admin-form label {
  display: block;
  margin: 13px 0 6px;
  color: #344054;
  font-size: 13px;
  font-weight: 700;
}

.admin-form input,
.admin-form select {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  color: var(--title);
  background: #ffffff;
  border: 1px solid #cfd6df;
  border-radius: 6px;
  outline: none;
}

.admin-form input:focus,
.admin-form select:focus {
  border-color: var(--green);
  box-shadow: 0 0 0 3px rgb(15 122 63 / 12%);
}

.checkbox-line {
  display: flex !important;
  align-items: center;
  gap: 8px;
}

.checkbox-line input {
  width: 18px;
  height: 18px;
}

.primary-button,
.soft-button {
  margin-top: 14px;
  padding: 9px 13px;
  font: inherit;
  font-size: 14px;
  font-weight: 700;
  border-radius: 6px;
  cursor: pointer;
}

.primary-button {
  color: #ffffff;
  background: var(--green);
  border: 1px solid var(--green);
}

.primary-button:hover {
  background: var(--green-dark);
}

.soft-button {
  color: var(--green-dark);
  background: #ffffff;
  border: 1px solid #b9e4ca;
}

.soft-button:hover {
  background: var(--green-light);
}

.no-margin {
  margin-top: 0;
}

.table-wrapper {
  margin-top: 12px;
  overflow-x: auto;
  background: #ffffff;
  border: 1px solid var(--gray-border);
  border-radius: 8px;
}

table {
  width: 100%;
  min-width: 760px;
  border-collapse: collapse;
}

.users-panel table {
  min-width: 860px;
}

.ateliers-panel table {
  min-width: 980px;
}

th,
td {
  padding: 11px 12px;
  text-align: left;
  border-bottom: 1px solid #edf0f3;
}

th {
  color: var(--gray-text);
  font-size: 12px;
  font-weight: 800;
  background: #f8fafc;
  text-transform: uppercase;
}

tbody tr:hover {
  background: #f9fafb;
}

tbody tr:last-child td {
  border-bottom: 0;
}

.strong-cell {
  color: var(--title);
  font-weight: 800;
}

.cell-detail strong,
.cell-detail small {
  display: block;
}

.cell-detail strong {
  color: var(--title);
  font-size: 13px;
}

.cell-detail small {
  margin-top: 2px;
  color: var(--gray-text);
  font-size: 12px;
}

.empty-cell {
  padding: 24px;
  color: var(--gray-text);
  font-weight: 700;
  text-align: center;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.actions button {
  padding: 6px 9px;
  color: var(--green-dark);
  font-weight: 700;
  background: var(--green-light);
  border: 1px solid #b9e4ca;
  border-radius: 6px;
  cursor: pointer;
}

.actions .danger {
  color: #9f1239;
  background: #fff5f6;
  border-color: #f8cfd5;
}

.alert {
  margin-top: 16px;
  padding: 10px 12px;
  font-weight: 700;
  border-radius: 8px;
}

.alert-error {
  color: #9f1239;
  background: #fff5f6;
  border: 1px solid #f8cfd5;
}

.alert-success {
  color: var(--green-dark);
  background: var(--green-light);
  border: 1px solid #b9e4ca;
}

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .two-columns {
    grid-template-columns: 1fr;
  }

  .dashboard-bottom {
    grid-template-columns: 1fr;
  }

  .users-layout {
    grid-template-columns: 1fr;
  }

  .users-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .detail-cards {
    width: 100%;
    min-width: 0;
  }
}

@media (max-width: 820px) {
  .admin-page {
    grid-template-columns: 1fr;
  }

  .admin-sidebar {
    height: auto;
  }

  .admin-topbar,
  .topbar-actions,
  .section-title {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (max-width: 560px) {
  .admin-content {
    padding: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .panel,
  .admin-topbar {
    padding: 16px;
  }

  .users-header,
  .users-layout {
    padding: 16px;
  }

  .detail-cards {
    grid-template-columns: 1fr;
  }
}
</style>
