<script setup>
import { useRouter, useRoute } from 'vue-router'
import { computed, onMounted, ref, watch } from 'vue'
import ocpLogo from '../assets/ocp-logo.png'
import { getChefDashboard, getEquipements, createEquipement, updateEquipement, deleteEquipement } from '../services/chefService'

const savedUser = localStorage.getItem('currentUser')
const user = ref(savedUser ? JSON.parse(savedUser) : null)

const router = useRouter()
const route = useRoute()

const activeTab = ref('dashboard')
const operationMonthFilter = ref('all')
const selectedOperation = ref(null)
const dashboardError = ref('')

const chefDashboard = ref({
  totalEquipements: 0,
  totalTechniciensLabo: 0,
  totalOperations: 0,
  totalLots: 0,
  activeOperation: null,
  recentOperations: [],
})

const menuItems = [
  { key: 'dashboard', label: 'Dashboard', icon: 'D' },
  { key: 'equipements', label: 'Equipements', icon: 'E' },
  { key: 'labo', label: 'Techniciens labo', icon: 'L' },
  { key: 'operations', label: 'Operations', icon: 'O' },
  { key: 'lots', label: 'Lots production', icon: 'P' },
]

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

const equipements = ref([
  {
    id: 1,
    tagIndustriel: 'EQ-101',
    nomEquipement: 'Convoyeur alimentation',
    typeEquipement: 'Convoyeur',
  },
  {
    id: 2,
    tagIndustriel: 'EQ-204',
    nomEquipement: 'Broyeur principal',
    typeEquipement: 'Broyeur',
  },
  {
    id: 3,
    tagIndustriel: 'EQ-318',
    nomEquipement: 'Pompe acide',
    typeEquipement: 'Pompe',
  },
])

const showEquipementModal = ref(false)
const equipementForm = ref(emptyEquipement())

function emptyEquipement() {
  return {
    id: null,
    tagIndustriel: '',
    nomEquipement: '',
    typeEquipement: '',
  }
}

async function loadEquipements() {
  try {
    const data = await getEquipements(user.value ? user.value.atelierId : null)
    // expect data.equipements or array
    equipements.value = data.equipements || data || []
  } catch (err) {
    console.error('loadEquipements', err)
  }
}

function openCreateEquipement() {
  equipementForm.value = emptyEquipement()
  showEquipementModal.value = true
}

function openEditEquipement(item) {
  equipementForm.value = { ...item }
  showEquipementModal.value = true
}

async function saveEquipement() {
  try {
    if (equipementForm.value.id) {
      const updated = await updateEquipement(equipementForm.value.id, equipementForm.value)
      const idx = equipements.value.findIndex((e) => e.id === updated.id)
      if (idx >= 0) equipements.value.splice(idx, 1, updated)
    } else {
      const created = await createEquipement({ ...equipementForm.value, atelierId: user.value ? user.value.atelierId : null })
      equipements.value.unshift(created)
    }
    showEquipementModal.value = false
  } catch (err) {
    console.error('saveEquipement', err)
    alert(err.message || 'Erreur')
  }
}

async function removeEquipement(id) {
  if (!confirm('Supprimer cet equipement ?')) return
  try {
    await deleteEquipement(id)
    const idx = equipements.value.findIndex((e) => e.id === id)
    if (idx >= 0) equipements.value.splice(idx, 1)
  } catch (err) {
    console.error('removeEquipement', err)
    alert(err.message || 'Erreur')
  }
}

const techniciensLabo = ref([
  {
    id: 1,
    nom: 'Technicien labo 1',
    email: 'labo1@ocp.local',
    poste: 'Analyse qualite',
    statut: 'Disponible',
  },
  {
    id: 2,
    nom: 'Technicien labo 2',
    email: 'labo2@ocp.local',
    poste: 'Controle P2O5',
    statut: 'En analyse',
  },
])

const operations = ref([])

const lots = ref([
  {
    id: 1,
    codeLot: 'LOT-001',
    date: '2026-06-18',
    article: 'Phosphate traite',
    statutQualite: 'Conforme',
  },
  {
    id: 2,
    codeLot: 'LOT-002',
    date: '2026-06-18',
    article: 'Matiere humide',
    statutQualite: 'En attente labo',
  },
  {
    id: 3,
    codeLot: 'LOT-003',
    date: '2026-06-17',
    article: 'Produit fini',
    statutQualite: 'Conforme',
  },
])

const analyses = ref([
  {
    id: 1,
    dateAnalyse: '2026-06-18',
    tauxP2O5: '29.4%',
    cadmium: '18 ppm',
    statut: 'Validee',
  },
  {
    id: 2,
    dateAnalyse: '2026-06-18',
    tauxP2O5: '28.9%',
    cadmium: '20 ppm',
    statut: 'En cours',
  },
])

const stats = computed(() => [
  { label: 'Equipements', value: chefDashboard.value.totalEquipements },
  { label: 'Techniciens labo', value: chefDashboard.value.totalTechniciensLabo },
  { label: 'Operations', value: chefDashboard.value.totalOperations },
  { label: 'Lots', value: chefDashboard.value.totalLots },
])

const activeMenu = computed(() => {
  if (activeTab.value === 'operation-details') {
    return { label: 'Details operation' }
  }

  return menuItems.find((item) => item.key === activeTab.value) || menuItems[0]
})

const atelierName = computed(() => {
  return (user && user.value && user.value.atelierName) || 'Atelier non assigne'
})

const activeOperation = computed(() => {
  return chefDashboard.value.activeOperation || operations.value[0] || null
})

const operationMonths = computed(() => {
  const months = operations.value.map((operation) => operation.dateDebut.slice(0, 7))
  const uniqueMonths = [...new Set(months)]

  return uniqueMonths.map((month) => ({
    value: month,
    label: formatMonthLabel(month),
  }))
})

const filteredRecentOperations = computed(() => {
  if (operationMonthFilter.value === 'all') {
    return operations.value
  }

  return operations.value.filter((operation) => {
    return operation.dateDebut.startsWith(operationMonthFilter.value)
  })
})

function formatMonthLabel(monthValue) {
  const [year, month] = monthValue.split('-')
  const monthIndex = Number(month) - 1

  return `${monthNames[monthIndex]} ${year}`
}

function viewOperationDetails(operation) {
  selectedOperation.value = operation
  activeTab.value = 'operation-details'
}

function backToDashboard() {
  selectedOperation.value = null
  activeTab.value = 'dashboard'
}

async function loadChefDashboard() {
  dashboardError.value = ''

  try {
    const data = await getChefDashboard(user.value ? user.value.atelierId : null)

    chefDashboard.value = {
      totalEquipements: data.totalEquipements || 0,
      totalTechniciensLabo: data.totalTechniciensLabo || 0,
      totalOperations: data.totalOperations || 0,
      totalLots: data.totalLots || 0,
      activeOperation: data.activeOperation || null,
      recentOperations: data.recentOperations || [],
    }

    operations.value = chefDashboard.value.recentOperations

    if (selectedOperation.value) {
      selectedOperation.value = operations.value.find((operation) => {
        return operation.id === selectedOperation.value.id
      }) || selectedOperation.value
    }
  } catch (error) {
    dashboardError.value = error.message
  }
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

function logout() {
  localStorage.removeItem('currentUser')
  router.push({ name: 'Login' })
}

onMounted(async () => {
  buildCalendar()
  // initialize active tab from query param `page` if present
  const q = route.query.page
  if (q) activeTab.value = q
  await loadChefDashboard()
  await loadEquipements()
})

// keep activeTab in sync with URL query so refresh preserves tab
watch(activeTab, (val) => {
  const query = val && val !== 'dashboard' ? { page: val } : {}
  router.replace({ name: 'Chef', query })
})
</script>

<template>
  <main class="chef-page">
    <aside class="chef-sidebar">
      <div class="chef-brand">
        <img :src="ocpLogo" alt="Logo OCP" />
        <div>
          <strong>OCP System</strong>
          <small>Chef atelier</small>
        </div>
      </div>

      <nav class="chef-menu">
        <button
          v-for="item in menuItems"
          :key="item.key"
          type="button"
          :class="{ active: activeTab === item.key }"
          @click="activeTab = item.key"
        >
          <span>{{ item.icon }}</span>
          {{ item.label }}
        </button>
      </nav>

      <div class="chef-user-card">
        <span class="status-dot"></span>
        <div>
          <strong>{{ user.username }}</strong>
          <small>{{ atelierName }}</small>
        </div>
      </div>

      <button class="logout-button" type="button" @click="logout">
        Se deconnecter
      </button>
    </aside>

    <section class="chef-content">
      <header class="chef-topbar">
        <div>
          <p class="eyebrow">Espace chef atelier</p>
          <h1>{{ activeMenu.label }}</h1>
          <p>
            Suivi de {{ atelierName }}. Gestion des equipements, operations,
            techniciens labo et lots de production.
          </p>
        </div>

        <div class="topbar-actions">
          <button class="refresh-button" type="button" @click="loadChefDashboard">
            Actualiser
          </button>
          <span class="role-pill">{{ user.role }}</span>
        </div>
      </header>

      <p v-if="dashboardError" class="alert-error">{{ dashboardError }}</p>

      <section v-if="activeTab === 'dashboard'" class="panel">
        <div class="stats-grid">
          <article v-for="item in stats" :key="item.label" class="stat-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </article>
        </div>

        <div class="dashboard-grid">
          <article class="info-card">
            <p class="eyebrow">Atelier</p>
            <h2>{{ atelierName }}</h2>
            <p>
              Cette page donne une vue simple sur les elements importants
              pour le chef atelier.
            </p>
          </article>

          <article class="info-card">
            <p class="eyebrow">Operation active</p>
            <template v-if="activeOperation">
              <h2>{{ activeOperation.numOrdreFab }}</h2>
              <p>{{ activeOperation.typeOperation }} - {{ activeOperation.statutOperation }}</p>
            </template>
            <template v-else>
              <h2>Aucune operation</h2>
              <p>Aucune operation active trouvee dans la base.</p>
            </template>
          </article>

          <article class="calendar-card">
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
          </article>
        </div>

        <div class="table-card">
          <div class="section-title">
            <div>
              <p class="eyebrow">Suivi rapide</p>
              <h2>Operations recentes</h2>
            </div>

            <label class="month-filter">
              Mois
              <select v-model="operationMonthFilter">
                <option value="all">Tous les mois</option>
                <option v-for="month in operationMonths" :key="month.value" :value="month.value">
                  {{ month.label }}
                </option>
              </select>
            </label>
          </div>

          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Ordre fab</th>
                  <th>Type</th>
                  <th>Date debut</th>
                  <th>Date fin</th>
                  <th>Details</th>
                </tr>
              </thead>

              <tbody>
                <tr v-if="filteredRecentOperations.length === 0">
                  <td class="empty-cell" colspan="5">Aucune operation pour ce mois</td>
                </tr>

                <tr v-for="operation in filteredRecentOperations" :key="operation.id">
                  <td class="strong-cell">{{ operation.numOrdreFab }}</td>
                  <td>{{ operation.typeOperation }}</td>
                  <td>{{ operation.dateDebut }}</td>
                  <td>{{ operation.dateFin || 'Non terminee' }}</td>
                  <td>
                    <button class="details-button" type="button" @click="viewOperationDetails(operation)">
                      Voir details
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <div v-if="showEquipementModal" class="modal-backdrop">
        <div class="modal-card">
          <div class="modal-header">
            <div>
              <p class="eyebrow">{{ equipementForm.id ? 'Modifier' : 'Nouveau' }}</p>
              <h2>{{ equipementForm.id ? 'Modifier equipement' : 'Nouvel equipement' }}</h2>
            </div>
            <button class="modal-close" type="button" @click="showEquipementModal = false">Fermer</button>
          </div>

          <form class="admin-form modal-form" @submit.prevent="saveEquipement">
            <div class="form-grid">
              <div>
                <label>Tag industriel</label>
                <input v-model.trim="equipementForm.tagIndustriel" required placeholder="EQ-001" />
              </div>

              <div>
                <label>Nom equipement</label>
                <input v-model.trim="equipementForm.nomEquipement" required placeholder="Broyeur" />
              </div>

              <div>
                <label>Type</label>
                <input v-model.trim="equipementForm.typeEquipement" required placeholder="Broyeur" />
              </div>

              <!-- etat removed: model no longer contains etat field -->
            </div>

            <div class="modal-actions">
              <button class="soft-button no-margin" type="button" @click="showEquipementModal = false">Annuler</button>
              <button class="primary-button no-margin" type="submit">{{ equipementForm.id ? 'Modifier' : 'Creer' }}</button>
            </div>
          </form>
        </div>
      </div>

      <section v-if="activeTab === 'operation-details'" class="panel">
        <div class="details-header">
          <div>
            <p class="eyebrow">Operation process</p>
            <h2>Details operation</h2>
            <p>Informations principales de l'operation selectionnee.</p>
          </div>

          <button class="details-button" type="button" @click="backToDashboard">
            Retour dashboard
          </button>
        </div>

        <div v-if="selectedOperation" class="details-grid">
          <article>
            <span>Ordre fabrication</span>
            <strong>{{ selectedOperation.numOrdreFab }}</strong>
          </article>
          <article>
            <span>Type operation</span>
            <strong>{{ selectedOperation.typeOperation }}</strong>
          </article>
          <article>
            <span>Statut</span>
            <strong>{{ selectedOperation.statutOperation }}</strong>
          </article>
          <article>
            <span>Date debut</span>
            <strong>{{ selectedOperation.dateDebut }}</strong>
          </article>
          <article>
            <span>Date fin</span>
            <strong>{{ selectedOperation.dateFin || 'Non terminee' }}</strong>
          </article>
          <article>
            <span>Atelier</span>
            <strong>{{ atelierName }}</strong>
          </article>
        </div>

        <p v-else class="empty-details">
          Aucune operation selectionnee.
        </p>
      </section>

      <section v-if="activeTab === 'equipements'" class="panel">
        <div class="section-title">
          <div>
            <p class="eyebrow">Patrimoine atelier</p>
            <h2>Equipements</h2>
            <p>Liste simple des equipements suivis par le chef atelier.</p>
          </div>
        </div>

        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Tag industriel</th>
                <th>Nom equipement</th>
                <th>Type</th>
                <th>Action</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="equipement in equipements" :key="equipement.id">
                <td class="strong-cell">{{ equipement.tagIndustriel }}</td>
                <td>{{ equipement.nomEquipement }}</td>
                <td>{{ equipement.typeEquipement }}</td>
                <td class="actions">
                  <button type="button" @click="openEditEquipement(equipement)">Modifier</button>
                  <button type="button" class="danger" @click="removeEquipement(equipement.id)">Supprimer</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
          <div style="margin-top:12px">
            <button class="primary-button" type="button" @click="openCreateEquipement">Ajouter equipement</button>
          </div>
      </section>

      <section v-if="activeTab === 'labo'" class="panel">
        <div class="section-title">
          <div>
            <p class="eyebrow">Laboratoire</p>
            <h2>Techniciens labo</h2>
            <p>Suivi des techniciens labo affectes a l'atelier.</p>
          </div>
        </div>

        <div class="labo-grid">
          <article class="table-card">
            <h3>Equipe labo</h3>
            <div class="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Nom</th>
                    <th>Email</th>
                    <th>Poste</th>
                    <th>Statut</th>
                  </tr>
                </thead>

                <tbody>
                  <tr v-for="technicien in techniciensLabo" :key="technicien.id">
                    <td class="strong-cell">{{ technicien.nom }}</td>
                    <td>{{ technicien.email }}</td>
                    <td>{{ technicien.poste }}</td>
                    <td><span class="mini-pill">{{ technicien.statut }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>

          <article class="table-card">
            <h3>Analyses recentes</h3>
            <div class="analysis-list">
              <div v-for="analyse in analyses" :key="analyse.id" class="analysis-card">
                <strong>{{ analyse.dateAnalyse }}</strong>
                <span>P2O5 : {{ analyse.tauxP2O5 }}</span>
                <span>Cadmium : {{ analyse.cadmium }}</span>
                <em>{{ analyse.statut }}</em>
              </div>
            </div>
          </article>
        </div>
      </section>

      <section v-if="activeTab === 'operations'" class="panel">
        <div class="section-title">
          <div>
            <p class="eyebrow">Process</p>
            <h2>Operations process</h2>
            <p>Suivi des operations de fabrication de l'atelier.</p>
          </div>
        </div>

        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Ordre fab</th>
                <th>Type operation</th>
                <th>Statut</th>
                <th>Date debut</th>
                <th>Date fin</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="operation in operations" :key="operation.id">
                <td class="strong-cell">{{ operation.numOrdreFab }}</td>
                <td>{{ operation.typeOperation }}</td>
                <td><span class="mini-pill">{{ operation.statutOperation }}</span></td>
                <td>{{ operation.dateDebut }}</td>
                <td>{{ operation.dateFin || 'Non terminee' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-if="activeTab === 'lots'" class="panel">
        <div class="section-title">
          <div>
            <p class="eyebrow">Production</p>
            <h2>Lots de production</h2>
            <p>Controle simple des lots et de leur statut qualite.</p>
          </div>
        </div>

        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Code lot</th>
                <th>Date</th>
                <th>Article</th>
                <th>Statut qualite</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="lot in lots" :key="lot.id">
                <td class="strong-cell">{{ lot.codeLot }}</td>
                <td>{{ lot.date }}</td>
                <td>{{ lot.article }}</td>
                <td><span class="mini-pill">{{ lot.statutQualite }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </section>
  </main>
</template>

<style scoped>
.chef-page {
  --green: #0f7a3f;
  --green-dark: #07582d;
  --green-light: #e8f5ee;
  --page-bg: #f3f5f4;
  --border: #dde5df;
  --text: #5f6f66;
  --title: #1b3327;

  display: grid;
  height: 100vh;
  grid-template-columns: 268px 1fr;
  color: var(--title);
  background: var(--page-bg);
}

.chef-sidebar {
  display: flex;
  position: sticky;
  top: 0;
  height: 100vh;
  padding: 18px 14px;
  flex-direction: column;
  color: #fff;
  background: #0b3d2b;
  border-right: 1px solid #0a3325;
}

.chef-brand {
  display: flex;
  padding: 12px 10px 18px;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgb(255 255 255 / 12%);
}

.chef-brand img {
  width: 58px;
  height: 42px;
  object-fit: contain;
}

.chef-brand strong,
.chef-brand small {
  display: block;
}

.chef-brand strong {
  font-size: 16px;
}

.chef-brand small {
  margin-top: 2px;
  color: rgb(255 255 255 / 66%);
  font-size: 11px;
  font-weight: 800;
  text-transform: uppercase;
}

.chef-menu {
  display: grid;
  gap: 8px;
  margin-top: 22px;
}

.chef-menu button {
  display: flex;
  width: 100%;
  padding: 11px 10px;
  align-items: center;
  gap: 10px;
  color: rgb(255 255 255 / 82%);
  font: inherit;
  font-size: 14px;
  font-weight: 750;
  text-align: left;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 10px;
  cursor: pointer;
}

.chef-menu button:hover {
  background: rgb(255 255 255 / 8%);
}

.chef-menu button.active {
  color: #0b3d2b;
  background: #fff;
}

.chef-menu span {
  display: grid;
  width: 28px;
  height: 28px;
  place-items: center;
  color: #fff;
  font-size: 11px;
  font-weight: 900;
  background: rgb(255 255 255 / 14%);
  border-radius: 7px;
}

.chef-menu button.active span {
  background: var(--green);
}

.chef-user-card {
  display: flex;
  margin-top: auto;
  padding: 12px;
  align-items: center;
  gap: 10px;
  background: rgb(255 255 255 / 8%);
  border: 1px solid rgb(255 255 255 / 12%);
  border-radius: 12px;
}

.status-dot {
  width: 10px;
  height: 10px;
  background: #4ade80;
  border-radius: 50%;
}

.chef-user-card strong,
.chef-user-card small {
  display: block;
}

.chef-user-card small {
  color: rgb(255 255 255 / 68%);
  font-size: 12px;
}

.logout-button {
  margin-top: 12px;
  padding: 10px;
  color: #fff;
  font: inherit;
  font-weight: 750;
  background: rgb(255 255 255 / 8%);
  border: 1px solid rgb(255 255 255 / 12%);
  border-radius: 8px;
  cursor: pointer;
}

.logout-button:hover {
  background: rgb(255 255 255 / 14%);
}

.chef-content {
  padding: 24px;
  overflow: auto;
  height: 100vh;
}

/* Modal/form styles (keep similar to Admin view) */
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
  width: min(680px, 100%);
  max-height: calc(100vh - 40px);
  overflow-y: auto;
  background: #ffffff;
  border: 1px solid #e6ece6;
  border-radius: 12px;
  box-shadow: 0 22px 70px rgb(15 23 42 / 22%);
}

.modal-header {
  display: flex;
  padding: 18px 20px;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background: #f8faf9;
  border-bottom: 1px solid #e6ece6;
}

.modal-close {
  padding: 8px 10px;
  color: #344054;
  font: inherit;
  font-size: 13px;
  font-weight: 700;
  background: #ffffff;
  border: 1px solid #e6ece6;
  border-radius: 6px;
  cursor: pointer;
}

.modal-form {
  padding: 20px;
  background: #ffffff;
  border: 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px 16px;
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
  color: #17382a;
  background: #ffffff;
  border: 1px solid #cfd6df;
  border-radius: 6px;
  outline: none;
}

.modal-actions {
  display: flex;
  margin-top: 20px;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 820px) {
  .chef-page {
    height: auto;
  }

  .chef-sidebar {
    position: static;
    height: auto;
  }

  .chef-content {
    height: auto;
    overflow: visible;
  }
}

.chef-topbar,
.panel {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 12px;
}

.chef-topbar {
  display: flex;
  padding: 20px;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.refresh-button {
  padding: 8px 11px;
  color: var(--green-dark);
  font: inherit;
  font-size: 13px;
  font-weight: 800;
  background: var(--green-light);
  border: 1px solid #b9e4ca;
  border-radius: 8px;
  cursor: pointer;
}

.refresh-button:hover {
  background: #dff0e7;
}

.alert-error {
  margin-top: 16px;
  padding: 11px 13px;
  color: #9f1239;
  font-weight: 750;
  background: #fff5f6;
  border: 1px solid #f8cfd5;
  border-radius: 10px;
}

.eyebrow {
  color: var(--green);
  font-size: 11px;
  font-weight: 850;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.chef-topbar h1 {
  margin-top: 4px;
  font-size: 28px;
  line-height: 1.2;
}

.chef-topbar p:last-child,
.section-title p,
.info-card p {
  margin-top: 6px;
  color: var(--text);
  font-size: 14px;
}

.role-pill,
.mini-pill {
  display: inline-flex;
  padding: 5px 9px;
  color: var(--green-dark);
  font-size: 12px;
  font-weight: 850;
  background: var(--green-light);
  border: 1px solid #b9e4ca;
  border-radius: 999px;
}

.panel {
  margin-top: 18px;
  padding: 18px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(150px, 1fr));
  gap: 12px;
}

.stat-card,
.info-card,
.table-card,
.calendar-card {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 10px;
}

.stat-card {
  padding: 16px;
  border-left: 4px solid var(--green);
}

.stat-card span,
.stat-card strong {
  display: block;
}

.stat-card span {
  color: var(--text);
  font-size: 13px;
  font-weight: 750;
}

.stat-card strong {
  margin-top: 6px;
  color: var(--title);
  font-size: 30px;
  line-height: 1;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.labo-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.info-card,
.table-card,
.calendar-card {
  padding: 16px;
}

.info-card h2,
.section-title h2,
.table-card h3 {
  margin-top: 4px;
  color: var(--title);
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.month-filter {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text);
  font-size: 13px;
  font-weight: 800;
}

.month-filter select {
  height: 36px;
  padding: 0 10px;
  color: var(--title);
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 8px;
  outline: none;
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

.calendar-header button,
.details-button {
  padding: 7px 10px;
  color: var(--green-dark);
  font: inherit;
  font-size: 12px;
  font-weight: 800;
  background: var(--green-light);
  border: 1px solid #b9e4ca;
  border-radius: 8px;
  cursor: pointer;
}

.calendar-header button:hover,
.details-button:hover {
  background: #dff0e7;
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
  color: var(--text);
  font-size: 11px;
  font-weight: 850;
  text-align: center;
}

.calendar-grid {
  margin-top: 8px;
}

.calendar-grid span {
  display: grid;
  min-height: 32px;
  place-items: center;
  color: #344054;
  font-size: 12px;
  background: #f8faf9;
  border: 1px solid #edf0f3;
  border-radius: 7px;
}

.calendar-grid span.empty {
  background: transparent;
  border-color: transparent;
}

.calendar-grid span.today {
  color: #fff;
  font-weight: 900;
  background: var(--green);
  border-color: var(--green);
}

.table-wrapper {
  margin-top: 14px;
  overflow-x: auto;
  border: 1px solid var(--border);
  border-radius: 10px;
}

table {
  width: 100%;
  min-width: 720px;
  border-collapse: collapse;
}

th,
td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #edf0f3;
}

th {
  color: var(--text);
  font-size: 12px;
  font-weight: 850;
  background: #f8faf9;
  text-transform: uppercase;
}

tbody tr:last-child td {
  border-bottom: 0;
}

tbody tr:hover {
  background: #f9fbfa;
}

.strong-cell {
  color: var(--title);
  font-weight: 850;
}

.empty-cell,
.empty-details {
  padding: 22px;
  color: var(--text);
  font-weight: 750;
  text-align: center;
}

.details-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.details-header p:last-child {
  margin-top: 6px;
  color: var(--text);
  font-size: 14px;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.details-grid article {
  padding: 16px;
  background: #f8faf9;
  border: 1px solid var(--border);
  border-radius: 10px;
}

.details-grid span,
.details-grid strong {
  display: block;
}

.details-grid span {
  color: var(--text);
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}

.details-grid strong {
  margin-top: 7px;
  color: var(--title);
  font-size: 16px;
}

.analysis-list {
  display: grid;
  gap: 10px;
  margin-top: 14px;
}

.analysis-card {
  display: grid;
  gap: 5px;
  padding: 12px;
  background: #f8faf9;
  border: 1px solid var(--border);
  border-radius: 10px;
}

.analysis-card strong {
  color: var(--title);
}

.analysis-card span {
  color: var(--text);
  font-size: 13px;
}

.analysis-card em {
  width: fit-content;
  padding: 4px 8px;
  color: var(--green-dark);
  font-size: 12px;
  font-style: normal;
  font-weight: 800;
  background: var(--green-light);
  border-radius: 999px;
}

.actions {
  display: flex;
  gap: 8px;
}

.danger {
  padding: 6px 10px;
  color: #fff;
  background: #d64545;
  border: 0;
  border-radius: 6px;
  cursor: pointer;
}

.danger:hover {
  background: #b93a3a;
}

.primary-button {
  padding: 8px 12px;
}

/* ensure modal inputs stretch on small screens */
@media (max-width: 680px) {
  .form-grid { grid-template-columns: 1fr; }
}

@media (max-width: 1100px) {
  .chef-page {
    grid-template-columns: 1fr;
  }

  .chef-sidebar {
    height: auto;
  }

  .stats-grid,
  .dashboard-grid,
  .labo-grid,
  .details-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .chef-content {
    padding: 16px;
  }

  .chef-topbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .stats-grid,
  .dashboard-grid,
  .labo-grid,
  .details-grid {
    grid-template-columns: 1fr;
  }

  .details-header,
  .section-title {
    align-items: flex-start;
    flex-direction: column;
  }

  .panel,
  .chef-topbar {
    padding: 16px;
  }
}
</style>
