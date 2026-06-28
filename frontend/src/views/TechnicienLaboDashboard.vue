<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ocpLogo from '../assets/ocp-logo.png'
import {
  getLaboAnalyses,
  getLaboOperations,
  getLaboOperationDetails,
  saveLaboAnalyse,
} from '../services/laboService'

const savedUser = localStorage.getItem('currentUser')
const user = ref(savedUser ? JSON.parse(savedUser) : null)
const router = useRouter()
const route = useRoute()

const operations = ref([])
const analysesHistory = ref([])
const selectedOperation = ref(null)
const operationDetails = ref(emptyDetails())
const savedTab = localStorage.getItem('laboActiveTab')
const initialTab = route.query.page === 'analyse' && !route.query.id ? 'operations' : route.query.page
const activeTab = ref(initialTab || savedTab || 'dashboard')
const operationFilter = ref('sans-analyse')
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const analyseForm = ref(emptyAnalyseForm())

function emptyDetails() {
  return {
    operation: null,
    flux: [],
    bilanMassique: null,
    lots: [],
    analyses: [],
  }
}

function emptyAnalyseForm() {
  return {
    lotProductionId: '',
    tauxP2O5: '',
    tauxCadmiumPpm: '',
    solidesSuspendu: '',
    dateAnalyse: new Date().toISOString().slice(0, 10),
  }
}

const atelierName = computed(() => user.value?.atelierName || 'Atelier non affecte')
const atelierId = computed(() => Number(user.value?.atelierId || 0))

const operationsTerminees = computed(() => {
  return operations.value.filter((operation) => {
    return operation.dateFin || operation.statutOperation === 'TERMINEE'
  }).length
})

const operationsEnCours = computed(() => operations.value.length - operationsTerminees.value)

const operationLots = computed(() => operationDetails.value.lots || [])
const operationAnalyses = computed(() => operationDetails.value.analyses || [])
const operationsAvecAnalyse = computed(() => {
  const ids = new Set(analysesHistory.value.map((analyse) => Number(analyse.operationProcessId)))
  return ids.size
})

const operationsSansAnalyse = computed(() => {
  return operations.value.filter((operation) => {
    return isOperationTerminee(operation) && !hasOperationAnalyse(operation)
  }).length
})

const operationsNonTerminees = computed(() => {
  return operations.value.filter((operation) => !isOperationTerminee(operation)).length
})

const recentAnalyses = computed(() => analysesHistory.value.slice(0, 5))

const filteredOperations = computed(() => {
  if (operationFilter.value === 'analysees') {
    return operations.value.filter((operation) => isOperationTerminee(operation) && hasOperationAnalyse(operation))
  }

  if (operationFilter.value === 'non-terminees') {
    return operations.value.filter((operation) => !isOperationTerminee(operation))
  }

  return operations.value.filter((operation) => isOperationTerminee(operation) && !hasOperationAnalyse(operation))
})

const pageTitle = computed(() => {
  if (activeTab.value === 'operations') return 'Operations a analyser'
  if (activeTab.value === 'analyse') return 'Saisie analyse'
  if (activeTab.value === 'historique') return 'Historique des analyses'
  return `Dashboard laboratoire`
})

const pageDescription = computed(() => {
  if (activeTab.value === 'operations') {
    return 'Les operations sont classees selon leur etat analytique pour aller vite.'
  }

  if (activeTab.value === 'analyse') {
    return 'Complete les mesures laboratoire du lot selectionne.'
  }

  if (activeTab.value === 'historique') {
    return 'Consulte toutes les analyses deja saisies pour ton atelier.'
  }

  return 'Vue rapide des operations, lots et analyses laboratoire de ton atelier.'
})

const selectedLotAnalyse = computed(() => {
  return operationAnalyses.value.find((analyse) => {
    return Number(analyse.lotProductionId) === Number(analyseForm.value.lotProductionId)
  })
})

async function loadOperations() {
  errorMessage.value = ''
  loading.value = true

  try {
    const data = await getLaboOperations(atelierId.value)
    operations.value = data.operations || []
    await loadHistory()

    const queryOperationId = Number(route.query.id || 0)
    const savedOperationId = queryOperationId || Number(localStorage.getItem('laboSelectedOperationId') || 0)
    const savedOperation = operations.value.find((operation) => Number(operation.id) === savedOperationId)

    if (!selectedOperation.value && savedOperation) {
      await selectOperation(savedOperation)
    } else if (!selectedOperation.value && operations.value.length > 0) {
      await selectOperation(filteredOperations.value[0] || operations.value[0])
    }

    if (activeTab.value === 'analyse' && selectedOperation.value && !route.query.id) {
      router.replace({ name: 'Labo', query: { page: 'analyse', id: selectedOperation.value.id } })
    }
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}

async function loadHistory() {
  const data = await getLaboAnalyses(atelierId.value)
  analysesHistory.value = data.analyses || []
}

async function selectOperation(operation) {
  errorMessage.value = ''
  successMessage.value = ''
  selectedOperation.value = operation
  localStorage.setItem('laboSelectedOperationId', String(operation.id))
  operationDetails.value = emptyDetails()
  analyseForm.value = emptyAnalyseForm()

  try {
    const details = await getLaboOperationDetails(operation.id)
    operationDetails.value = details
    selectedOperation.value = details.operation || operation

    if (operationDetails.value.lots.length > 0) {
      analyseForm.value.lotProductionId = operationDetails.value.lots[0].id
      fillAnalyseFromLot()
    }
  } catch (error) {
    errorMessage.value = error.message
  }
}

async function openAnalyse(operation) {
  await selectOperation(operation)
  activeTab.value = 'analyse'
  localStorage.setItem('laboActiveTab', 'analyse')
  router.replace({ name: 'Labo', query: { page: 'analyse', id: operation.id } })
}

function isOperationTerminee(operation) {
  return Boolean(operation.dateFin || operation.statutOperation === 'TERMINEE')
}

function hasOperationAnalyse(operation) {
  return analysesHistory.value.some((analyse) => Number(analyse.operationProcessId) === Number(operation.id))
}

function getOperationCategoryLabel(operation) {
  if (!isOperationTerminee(operation)) return 'Operation non terminee'
  if (hasOperationAnalyse(operation)) return 'Analyse faite'
  return 'Sans analyse'
}

function getOperationCategoryClass(operation) {
  if (!isOperationTerminee(operation)) return 'category-waiting'
  if (hasOperationAnalyse(operation)) return 'category-done'
  return 'category-pending'
}

function fillAnalyseFromLot() {
  const current = selectedLotAnalyse.value

  if (!current) {
    analyseForm.value = {
      ...analyseForm.value,
      tauxP2O5: '',
      tauxCadmiumPpm: '',
      solidesSuspendu: '',
      dateAnalyse: new Date().toISOString().slice(0, 10),
    }
    return
  }

  analyseForm.value = {
    lotProductionId: current.lotProductionId,
    tauxP2O5: current.tauxP2O5,
    tauxCadmiumPpm: current.tauxCadmiumPpm,
    solidesSuspendu: current.solidesSuspendu,
    dateAnalyse: current.dateAnalyse,
  }
}

async function submitAnalyse() {
  successMessage.value = ''
  errorMessage.value = ''

  if (!analyseForm.value.lotProductionId) {
    errorMessage.value = 'Choisis un lot avant de saisir une analyse.'
    return
  }

  saving.value = true

  try {
    await saveLaboAnalyse({
      atelierId: atelierId.value,
      lotProductionId: Number(analyseForm.value.lotProductionId),
      tauxP2O5: Number(analyseForm.value.tauxP2O5),
      tauxCadmiumPpm: Number(analyseForm.value.tauxCadmiumPpm),
      solidesSuspendu: Number(analyseForm.value.solidesSuspendu),
      dateAnalyse: analyseForm.value.dateAnalyse,
    })

    successMessage.value = 'Analyse labo sauvegardee.'
    await loadHistory()
    await selectOperation(selectedOperation.value)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    saving.value = false
  }
}

function switchTab(tab) {
  activeTab.value = tab
}

function logout() {
  localStorage.removeItem('currentUser')
  router.push({ name: 'Login' })
}

watch(activeTab, (tab) => {
  localStorage.setItem('laboActiveTab', tab)
  if (tab === 'analyse') {
    if (!selectedOperation.value) {
      activeTab.value = 'operations'
      return
    }

    router.replace({ name: 'Labo', query: { page: 'analyse', id: selectedOperation.value.id } })
    return
  }

  router.replace({ name: 'Labo', query: tab === 'dashboard' ? {} : { page: tab } })
})

onMounted(loadOperations)
</script>

<template>
  <main class="labo-page">
    <aside class="labo-sidebar">
      <div class="labo-brand">
        <img :src="ocpLogo" alt="Logo OCP" />
        <div>
          <strong>OCP System</strong>
          <small>Technicien labo</small>
        </div>
      </div>

      <div class="labo-user">
        <strong>{{ user.username }}</strong>
        <small>{{ atelierName }}</small>
      </div>

      <nav class="labo-menu">
        <button type="button" :class="{ active: activeTab === 'dashboard' }" @click="switchTab('dashboard')">
          Dashboard
        </button>
        <button type="button" :class="{ active: activeTab === 'operations' }" @click="switchTab('operations')">
          Operations
        </button>
        <button type="button" :class="{ active: activeTab === 'historique' }" @click="switchTab('historique')">
          Historique
        </button>
      </nav>

      <button class="logout-button" type="button" @click="logout">
        Se deconnecter
      </button>
    </aside>

    <section class="labo-content">
      <header class="labo-topbar">
        <div>
          <p class="eyebrow">Espace laboratoire</p>
          <h1>{{ pageTitle }}</h1>
          <p>{{ pageDescription }}</p>
        </div>

        <button class="primary-button" type="button" @click="loadOperations">
          Actualiser
        </button>
      </header>

      <p v-if="errorMessage" class="alert-error">{{ errorMessage }}</p>
      <p v-if="successMessage" class="alert-success">{{ successMessage }}</p>

      <section v-if="activeTab === 'dashboard'" class="dashboard-panel">
        <div class="metrics-grid">
          <article>
            <span>Total operations</span>
            <strong>{{ operations.length }}</strong>
          </article>
          <article>
            <span>En cours</span>
            <strong>{{ operationsEnCours }}</strong>
          </article>
          <article>
            <span>Sans analyse</span>
            <strong>{{ operationsSansAnalyse }}</strong>
          </article>
          <article>
            <span>Non terminees</span>
            <strong>{{ operationsNonTerminees }}</strong>
          </article>
          <article>
            <span>Analyses saisies</span>
            <strong>{{ analysesHistory.length }}</strong>
          </article>
        </div>

        <div class="dashboard-grid">
          <article class="panel-card dashboard-focus-card">
            <p class="eyebrow">Priorite</p>
            <h2>{{ operationsSansAnalyse }} operation(s) a verifier</h2>
            <p>
              Commence par les operations sans analyse, puis complete les mesures
              P2O5, cadmium et solides suspendu.
            </p>
            <button class="primary-button" type="button" @click="switchTab('operations')">
              Ouvrir operations
            </button>
          </article>

          <article class="panel-card">
            <div class="section-title">
              <div>
                <p class="eyebrow">Dernieres analyses</p>
                <h2>Historique recent</h2>
              </div>
              <button class="soft-button" type="button" @click="switchTab('historique')">
                Voir tout
              </button>
            </div>

            <div class="history-list">
              <p v-if="recentAnalyses.length === 0" class="empty-note">
                Aucune analyse saisie pour le moment.
              </p>
              <article v-for="analyse in recentAnalyses" :key="analyse.id">
                <div>
                  <strong>{{ analyse.numOrdreFab }}</strong>
                  <small>{{ analyse.nomArticle }} - {{ analyse.dateAnalyse }}</small>
                </div>
                <span class="status-pill">{{ analyse.tauxP2O5 }} P2O5</span>
              </article>
            </div>
          </article>
        </div>
      </section>

      <section v-if="activeTab === 'operations'" class="work-grid">
        <article class="panel-card operations-card">
          <div class="section-title">
            <div>
              <p class="eyebrow">Operations atelier</p>
              <h2>Liste organisee</h2>
            </div>
          </div>

          <div class="category-tabs">
            <button
              type="button"
              :class="{ active: operationFilter === 'sans-analyse' }"
              @click="operationFilter = 'sans-analyse'"
            >
              Sans analyse
              <span>{{ operationsSansAnalyse }}</span>
            </button>
            <button
              type="button"
              :class="{ active: operationFilter === 'analysees' }"
              @click="operationFilter = 'analysees'"
            >
              Deja analysees
              <span>{{ operationsAvecAnalyse }}</span>
            </button>
            <button
              type="button"
              :class="{ active: operationFilter === 'non-terminees' }"
              @click="operationFilter = 'non-terminees'"
            >
              Non terminees
              <span>{{ operationsNonTerminees }}</span>
            </button>
          </div>

          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Ordre fab</th>
                  <th>Type</th>
                  <th>Equipement</th>
                  <th>Statut</th>
                  <th>Categorie</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="loading">
                  <td class="empty-cell" colspan="6">Chargement...</td>
                </tr>
                <tr v-else-if="filteredOperations.length === 0">
                  <td class="empty-cell" colspan="6">Aucune operation dans cette categorie.</td>
                </tr>
                <tr
                  v-for="operation in filteredOperations"
                  v-else
                  :key="operation.id"
                  :class="{ selected: selectedOperation && selectedOperation.id === operation.id }"
                  @click="selectOperation(operation)"
                >
                  <td>
                    <strong>{{ operation.numOrdreFab }}</strong>
                    <small>{{ operation.dateDebut }}</small>
                  </td>
                  <td>{{ operation.typeOperation }}</td>
                  <td>{{ operation.equipementName || 'Non precise' }}</td>
                  <td><span class="status-pill">{{ operation.statutOperation }}</span></td>
                  <td>
                    <span class="category-pill" :class="getOperationCategoryClass(operation)">
                      {{ getOperationCategoryLabel(operation) }}
                    </span>
                  </td>
                  <td>
                    <button
                      class="soft-button compact-button"
                      type="button"
                      :disabled="!isOperationTerminee(operation)"
                      @click.stop="openAnalyse(operation)"
                    >
                      Analytique
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>
      </section>

      <section v-if="activeTab === 'analyse'" class="analyse-page">
        <article class="panel-card details-card">
          <div class="section-title">
            <div>
              <p class="eyebrow">Operation selectionnee</p>
              <h2>{{ selectedOperation ? selectedOperation.numOrdreFab : 'Aucune operation' }}</h2>
            </div>
            <span v-if="selectedOperation" class="status-pill">
              {{ selectedOperation.statutOperation }}
            </span>
          </div>

          <template v-if="selectedOperation">
            <div class="summary-grid">
              <article>
                <span>Equipement</span>
                <strong>{{ selectedOperation.equipementName || 'Non precise' }}</strong>
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
                <span>Lots</span>
                <strong>{{ operationLots.length }}</strong>
              </article>
            </div>

            <form class="analyse-form" @submit.prevent="submitAnalyse">
              <div class="form-header">
                <div>
                  <p class="eyebrow">Analyse laboratoire</p>
                  <h3>{{ selectedLotAnalyse ? 'Modifier analyse' : 'Nouvelle analyse' }}</h3>
                </div>
              </div>

              <p v-if="operationLots.length === 0" class="empty-note">
                Aucun lot disponible. L'analyse sera possible apres la cloture de l'operation.
              </p>

              <div v-else class="form-grid">
                <div class="full-field">
                  <label>Lot a analyser</label>
                  <select v-model.number="analyseForm.lotProductionId" required @change="fillAnalyseFromLot">
                    <option v-for="lot in operationLots" :key="lot.id" :value="lot.id">
                      {{ lot.nomArticle }} - {{ lot.date }} - {{ lot.statutQualite }}
                    </option>
                  </select>
                </div>

                <div>
                  <label>P2O5</label>
                  <input v-model.number="analyseForm.tauxP2O5" min="0" step="0.01" type="number" required />
                </div>

                <div>
                  <label>Cadmium ppm</label>
                  <input v-model.number="analyseForm.tauxCadmiumPpm" min="0" step="0.01" type="number" required />
                </div>

                <div>
                  <label>Solides suspendu</label>
                  <input v-model.number="analyseForm.solidesSuspendu" min="0" step="0.01" type="number" required />
                </div>

                <div>
                  <label>Date analyse</label>
                  <input v-model="analyseForm.dateAnalyse" type="date" required />
                </div>

                <button class="primary-button full-field" type="submit" :disabled="saving">
                  {{ saving ? 'Sauvegarde...' : 'Sauvegarder analyse' }}
                </button>
              </div>
            </form>

            <div class="analyses-list">
              <div class="section-title">
                <div>
                  <p class="eyebrow">Historique</p>
                  <h3>Analyses saisies</h3>
                </div>
              </div>

              <div class="table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>Lot</th>
                      <th>P2O5</th>
                      <th>Cadmium</th>
                      <th>Solides</th>
                      <th>Date</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-if="operationAnalyses.length === 0">
                      <td class="empty-cell" colspan="5">Aucune analyse saisie pour cette operation.</td>
                    </tr>
                    <tr v-for="analyse in operationAnalyses" :key="analyse.id">
                      <td>Lot analyse</td>
                      <td>{{ analyse.tauxP2O5 }}</td>
                      <td>{{ analyse.tauxCadmiumPpm }}</td>
                      <td>{{ analyse.solidesSuspendu }}</td>
                      <td>{{ analyse.dateAnalyse }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </template>

          <p v-else class="empty-note">
            Selectionne une operation depuis la liste, puis clique sur Analytique.
          </p>
        </article>
      </section>

      <section v-if="activeTab === 'historique'" class="panel-card history-page">
        <div class="section-title">
          <div>
            <p class="eyebrow">Historique atelier</p>
            <h2>Analyses laboratoire</h2>
          </div>
          <button class="soft-button" type="button" @click="loadHistory">
            Actualiser
          </button>
        </div>

        <div class="history-summary">
          <article>
            <span>Total analyses</span>
            <strong>{{ analysesHistory.length }}</strong>
          </article>
          <article>
            <span>Operations analysees</span>
            <strong>{{ operationsAvecAnalyse }}</strong>
          </article>
          <article>
            <span>Operations restantes</span>
            <strong>{{ operationsSansAnalyse }}</strong>
          </article>
        </div>

        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Operation</th>
                <th>Article</th>
                <th>P2O5</th>
                <th>Cadmium ppm</th>
                <th>Solides</th>
                <th>Date analyse</th>
                <th>Qualite</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="analysesHistory.length === 0">
                <td class="empty-cell" colspan="7">Aucune analyse trouvee pour cet atelier.</td>
              </tr>
              <tr v-for="analyse in analysesHistory" :key="analyse.id">
                <td>
                  <strong>{{ analyse.numOrdreFab }}</strong>
                  <small>Operation source</small>
                </td>
                <td>{{ analyse.nomArticle }}</td>
                <td>{{ analyse.tauxP2O5 }}</td>
                <td>{{ analyse.tauxCadmiumPpm }}</td>
                <td>{{ analyse.solidesSuspendu }}</td>
                <td>{{ analyse.dateAnalyse }}</td>
                <td><span class="status-pill">{{ analyse.statutQualite }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </section>
  </main>
</template>

<style scoped>
.labo-page {
  --green: #0f7a3f;
  --green-dark: #07582d;
  --green-light: #e8f5ee;
  --page-bg: #f3f5f4;
  --border: #dde5df;
  --text: #5f6f66;
  --title: #1b3327;

  display: grid;
  min-height: 100vh;
  grid-template-columns: 268px 1fr;
  color: var(--title);
  background: var(--page-bg);
}

.labo-page,
.labo-page * {
  box-sizing: border-box;
}

.labo-sidebar {
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

.labo-brand {
  display: flex;
  padding: 12px 10px 18px;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgb(255 255 255 / 12%);
}

.labo-brand img {
  width: 58px;
  height: 42px;
  object-fit: contain;
}

.labo-brand strong,
.labo-brand small,
.labo-user strong,
.labo-user small {
  display: block;
}

.labo-brand small,
.labo-user small {
  color: rgb(255 255 255 / 66%);
  font-size: 12px;
  font-weight: 800;
}

.labo-user {
  margin-top: 18px;
  padding: 14px;
  background: rgb(255 255 255 / 8%);
  border: 1px solid rgb(255 255 255 / 12%);
  border-radius: 12px;
}

.labo-menu {
  display: grid;
  gap: 8px;
  margin-top: 18px;
}

.labo-menu button {
  width: 100%;
  padding: 11px 12px;
  color: rgb(255 255 255 / 82%);
  font: inherit;
  font-size: 14px;
  font-weight: 800;
  text-align: left;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 10px;
  cursor: pointer;
}

.labo-menu button:hover {
  background: rgb(255 255 255 / 8%);
}

.labo-menu button.active {
  color: #0b3d2b;
  background: #fff;
}

.logout-button {
  margin-top: auto;
  padding: 11px 14px;
  color: #fff;
  font: inherit;
  font-weight: 850;
  background: #d64545;
  border: 0;
  border-radius: 9px;
  cursor: pointer;
}

.labo-content {
  width: 100%;
  min-width: 0;
  max-width: 1520px;
  margin: 0 auto;
  padding: 20px;
}

.labo-topbar {
  display: flex;
  padding: 22px;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 16px;
  box-shadow: 0 16px 36px rgb(16 24 40 / 5%);
}

.labo-topbar > div,
.section-title > div {
  min-width: 0;
}

.eyebrow {
  color: var(--green);
  font-size: 11px;
  font-weight: 850;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.labo-topbar h1 {
  margin-top: 4px;
  font-size: 28px;
}

.labo-topbar p:last-child {
  margin-top: 6px;
  color: var(--text);
}

.primary-button {
  display: inline-flex;
  min-height: 40px;
  padding: 10px 14px;
  align-items: center;
  justify-content: center;
  color: #fff;
  font: inherit;
  font-size: 13px;
  font-weight: 850;
  background: var(--green);
  border: 1px solid var(--green);
  border-radius: 9px;
  box-shadow: 0 10px 22px rgb(15 122 63 / 16%);
  cursor: pointer;
  white-space: nowrap;
}

.primary-button:disabled {
  opacity: 0.7;
  cursor: wait;
}

.soft-button {
  display: inline-flex;
  min-height: 38px;
  padding: 9px 13px;
  align-items: center;
  justify-content: center;
  color: var(--green-dark);
  font: inherit;
  font-size: 13px;
  font-weight: 850;
  background: #fff;
  border: 1px solid #b9e4ca;
  border-radius: 9px;
  cursor: pointer;
  white-space: nowrap;
}

.soft-button:hover {
  background: var(--green-light);
}

.soft-button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.compact-button {
  min-height: 34px;
  padding: 7px 11px;
  white-space: nowrap;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(175px, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.metrics-grid article,
.summary-grid article {
  padding: 16px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 14px;
  box-shadow: 0 12px 26px rgb(16 24 40 / 5%);
}

.metrics-grid span,
.metrics-grid strong,
.summary-grid span,
.summary-grid strong {
  display: block;
}

.metrics-grid span,
.summary-grid span {
  color: var(--text);
  font-size: 12px;
  font-weight: 850;
  text-transform: uppercase;
}

.metrics-grid strong,
.summary-grid strong {
  margin-top: 7px;
  color: var(--title);
  font-size: 20px;
}

.work-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
  margin-top: 18px;
}

.operations-card {
  min-width: 0;
}

.operations-card table {
  min-width: 920px;
}

.category-tabs {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: 16px;
  padding: 6px;
  background: #f2f6f4;
  border: 1px solid var(--border);
  border-radius: 14px;
}

.category-tabs button {
  display: flex;
  min-width: 0;
  min-height: 42px;
  padding: 8px 11px;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  color: var(--text);
  font: inherit;
  font-size: 13px;
  font-weight: 900;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 10px;
  cursor: pointer;
}

.category-tabs button.active {
  color: var(--green-dark);
  background: #fff;
  border-color: #b9e4ca;
  box-shadow: 0 10px 22px rgb(15 122 63 / 10%);
}

.category-tabs span {
  display: grid;
  flex: 0 0 auto;
  min-width: 28px;
  height: 24px;
  place-items: center;
  color: var(--green-dark);
  background: var(--green-light);
  border-radius: 999px;
}

.category-pill {
  display: inline-flex;
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 900;
  border: 1px solid;
  border-radius: 999px;
}

.category-done {
  color: var(--green-dark);
  background: var(--green-light);
  border-color: #b9e4ca;
}

.category-pending {
  color: #b54708;
  background: #fff7ed;
  border-color: #fed7aa;
}

.category-waiting {
  color: #475467;
  background: #f2f4f7;
  border-color: #e4e7ec;
}

.analyse-page {
  margin-top: 18px;
}

.analyse-page .details-card {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
}

.dashboard-panel {
  margin-top: 18px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(320px, 0.9fr) minmax(0, 1.1fr);
  gap: 16px;
  margin-top: 16px;
  align-items: stretch;
}

.dashboard-focus-card {
  background:
    radial-gradient(circle at top right, rgb(15 122 63 / 13%), transparent 34%),
    linear-gradient(135deg, #fff, #f4fbf7);
}

.dashboard-focus-card h2 {
  margin-top: 7px;
  color: var(--title);
}

.dashboard-focus-card p {
  margin: 10px 0 16px;
  color: var(--text);
  line-height: 1.55;
}

.history-list {
  display: grid;
  gap: 10px;
  margin-top: 14px;
}

.history-list article {
  display: flex;
  padding: 12px;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background: #f8faf9;
  border: 1px solid var(--border);
  border-radius: 12px;
}

.history-list article > div {
  min-width: 0;
}

.history-list strong,
.history-list small {
  display: block;
}

.history-list small {
  margin-top: 4px;
  color: var(--text);
  font-size: 12px;
  font-weight: 750;
}

.history-page {
  margin-top: 18px;
}

.history-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.history-summary article {
  padding: 15px;
  background: #f8faf9;
  border: 1px solid var(--border);
  border-radius: 14px;
}

.history-summary span,
.history-summary strong {
  display: block;
}

.history-summary span {
  color: var(--text);
  font-size: 12px;
  font-weight: 850;
  text-transform: uppercase;
}

.history-summary strong {
  margin-top: 7px;
  font-size: 20px;
}

.panel-card {
  min-width: 0;
  padding: 18px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 16px;
  box-shadow: 0 16px 36px rgb(16 24 40 / 5%);
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.section-title h2,
.section-title h3 {
  margin-top: 4px;
}

.table-wrapper {
  margin-top: 14px;
  overflow-x: auto;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 12px;
}

table {
  width: 100%;
  min-width: 620px;
  border-collapse: collapse;
}

th,
td {
  padding: 12px;
  text-align: left;
  vertical-align: middle;
  border-bottom: 1px solid #edf0f3;
}

th {
  color: var(--text);
  font-size: 12px;
  font-weight: 850;
  background: #f8faf9;
  text-transform: uppercase;
}

tbody tr {
  transition: background 0.18s ease;
}

.operations-card tbody tr {
  cursor: pointer;
}

tbody tr:hover,
tbody tr.selected {
  background: #f0fbf4;
}

td strong,
td small {
  display: block;
}

td small {
  margin-top: 3px;
  color: var(--text);
  font-size: 12px;
  font-weight: 750;
}

.status-pill {
  display: inline-flex;
  padding: 6px 10px;
  color: var(--green-dark);
  font-size: 12px;
  font-weight: 900;
  background: var(--green-light);
  border: 1px solid #b9e4ca;
  border-radius: 999px;
}

.analyse-form,
.analyses-list {
  margin-top: 18px;
  padding: 16px;
  background: #f8faf9;
  border: 1px solid var(--border);
  border-radius: 14px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 14px;
}

.primary-button.full-field {
  width: 100%;
}

.full-field {
  grid-column: 1 / -1;
}

label {
  display: block;
  margin-bottom: 6px;
  color: var(--text);
  font-size: 12px;
  font-weight: 850;
  text-transform: uppercase;
}

input,
select {
  width: 100%;
  min-height: 40px;
  padding: 9px 10px;
  color: var(--title);
  font: inherit;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 9px;
  outline: none;
}

input:focus,
select:focus {
  border-color: #9bd3b0;
  box-shadow: 0 0 0 3px rgb(15 122 63 / 10%);
}

.empty-cell,
.empty-note,
.alert-error,
.alert-success {
  padding: 14px;
  font-weight: 750;
  text-align: center;
}

.empty-note {
  margin-top: 14px;
  color: var(--text);
  background: #fff;
  border: 1px dashed #cbd5d0;
  border-radius: 12px;
}

.alert-error,
.alert-success {
  margin-top: 14px;
  border-radius: 12px;
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
  .labo-page,
  .work-grid,
  .dashboard-grid,
  .history-summary {
    grid-template-columns: 1fr;
  }

  .labo-sidebar {
    position: relative;
    height: auto;
  }

  .logout-button {
    margin-top: 14px;
  }
}

@media (max-width: 680px) {
  .labo-content {
    padding: 16px;
  }

  .labo-topbar,
  .section-title {
    align-items: flex-start;
    flex-direction: column;
  }

  .labo-topbar .primary-button,
  .section-title .soft-button {
    width: 100%;
  }

  .category-tabs,
  .form-grid {
    grid-template-columns: 1fr;
  }

  .history-list article {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
