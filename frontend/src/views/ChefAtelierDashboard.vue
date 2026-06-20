<script setup>
import { useRouter, useRoute } from 'vue-router'
import { computed, onMounted, ref, watch } from 'vue'
import ocpLogo from '../assets/ocp-logo.png'
import {
  getChefDashboard,
  getEquipements,
  createEquipement,
  updateEquipement,
  deleteEquipement,
  getTechniciensLabo,
  createTechnicienLabo,
  updateTechnicienLabo,
  deleteTechnicienLabo,
  getAtelierByChef,
  getOperations,
  getOperationDetails,
  createOperation,
  updateOperation,
  deleteOperation,
  getLots,
  getArticles,
  createArticle,
  updateArticle,
  deleteArticle,
} from '../services/chefService'

const savedUser = localStorage.getItem('currentUser')
const user = ref(savedUser ? JSON.parse(savedUser) : null)

const router = useRouter()
const route = useRoute()

const activeTab = ref('dashboard')
const operationMonthFilter = ref('all')
const selectedOperation = ref(null)
const operationDetails = ref(emptyOperationDetails())
const dashboardError = ref('')
const chefAtelierId = ref(Number(user.value?.atelierId || 0))
const chefAtelierName = ref(user.value?.atelierName || '')

const chefDashboard = ref({
  totalEquipements: 0,
  totalTechniciensLabo: 0,
  totalOperations: 0,
  totalLots: 0,
  activeOperation: null,
  recentOperations: [],
})

function emptyOperationDetails() {
  return {
    operation: null,
    flux: [],
    bilanMassique: null,
    lots: [],
    analyses: [],
  }
}

const menuItems = [
  { key: 'dashboard', label: 'Dashboard', icon: 'D' },
  { key: 'equipements', label: 'Equipements', icon: 'E' },
  { key: 'labo', label: 'Techniciens labo', icon: 'L' },
  { key: 'operations', label: 'Operations', icon: 'O' },
  { key: 'articles', label: 'Articles matiere', icon: 'A' },
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
    const data = await getEquipements(getCurrentAtelierId())
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
      const created = await createEquipement({ ...equipementForm.value, atelierId: getCurrentAtelierId() })
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

const techniciensLabo = ref([])
const showTechnicienModal = ref(false)
const technicienForm = ref(emptyTechnicienForm())

function emptyTechnicienForm() {
  return {
    id: null,
    username: '',
    email: '',
    password: '',
  }
}

async function loadTechniciensLabo() {
  try {
    techniciensLabo.value = await getTechniciensLabo(getCurrentAtelierId())
  } catch (err) {
    console.error('loadTechniciensLabo', err)
  }
}

function openCreateTechnicien() {
  technicienForm.value = emptyTechnicienForm()
  showTechnicienModal.value = true
}

function openEditTechnicien(technicien) {
  technicienForm.value = {
    id: technicien.id,
    username: technicien.username,
    email: technicien.email,
    password: '',
  }
  showTechnicienModal.value = true
}

function closeTechnicienModal() {
  showTechnicienModal.value = false
  technicienForm.value = emptyTechnicienForm()
}

async function saveTechnicien() {
  const atelierId = getCurrentAtelierId()

  if (!atelierId) {
    alert('Impossible de creer le technicien : ce chef atelier n est pas assigne a un atelier.')
    return
  }

  if (!technicienForm.value.id && !technicienForm.value.password) {
    alert('Le mot de passe est obligatoire pour creer un technicien labo.')
    return
  }

  const payload = {
    username: technicienForm.value.username,
    email: technicienForm.value.email,
    password: technicienForm.value.password,
    atelierId,
  }

  try {
    if (technicienForm.value.id) {
      await updateTechnicienLabo(technicienForm.value.id, payload)
    } else {
      await createTechnicienLabo(payload)
    }

    closeTechnicienModal()
    await loadTechniciensLabo()
    await loadChefDashboard()
  } catch (err) {
    console.error('saveTechnicien', err)
    alert(err.message || 'Erreur')
  }
}

async function removeTechnicien(id) {
  if (!confirm('Supprimer ce technicien labo ?')) return

  try {
    await deleteTechnicienLabo(id)
    await loadTechniciensLabo()
    await loadChefDashboard()
  } catch (err) {
    console.error('removeTechnicien', err)
    alert(err.message || 'Erreur')
  }
}

const operations = ref([])
const articles = ref([])
const showOperationModal = ref(false)
const operationForm = ref(emptyOperationForm())

function todayDate() {
  return new Date().toISOString().slice(0, 10)
}

function emptyOperationForm() {
  return {
    id: null,
    numOrdreFab: '',
    typeOperation: '',
    statutOperation: 'EN_COURS',
    dateDebut: todayDate(),
    dateFin: '',
    operateur: '',
    equipementId: '',
    dureeEstimee: 60,
    entreeFlux: [emptyEntreeFlux()],
    sortieArticleMatiereId: '',
    sortieMesureCapteur: '',
    sortieMesureDiametre: '',
  }
}

function emptyEntreeFlux() {
  return {
    articleMatiereId: articles.value[0]?.id || '',
    mesureCapteur: '',
    mesureDiametre: '',
  }
}

function addEntreeFlux() {
  operationForm.value.entreeFlux.push(emptyEntreeFlux())
}

function removeEntreeFlux(index) {
  if (operationForm.value.entreeFlux.length === 1) {
    alert('Il faut garder au moins un flux d entree.')
    return
  }

  operationForm.value.entreeFlux.splice(index, 1)
}

async function loadOperations() {
  try {
    const data = await getOperations(getCurrentAtelierId())
    operations.value = data.operations || data || []

    if (selectedOperation.value) {
      selectedOperation.value = operations.value.find((operation) => {
        return operation.id === selectedOperation.value.id
      }) || selectedOperation.value
    }
  } catch (err) {
    console.error('loadOperations', err)
  }
}

function openCreateOperation() {
  if (equipements.value.length === 0) {
    alert('Ajoute d abord un equipement pour creer une operation.')
    return
  }

  if (articles.value.length === 0) {
    alert('Aucun article matiere trouve dans la base de donnees.')
    return
  }

  operationForm.value = {
    ...emptyOperationForm(),
    operateur: user.value?.username || '',
    equipementId: equipements.value[0].id,
    entreeFlux: [
      {
        ...emptyEntreeFlux(),
        articleMatiereId: articles.value[0].id,
      },
    ],
    sortieArticleMatiereId: articles.value[0].id,
  }
  showOperationModal.value = true
}

async function openEditOperation(operation) {
  let details = emptyOperationDetails()

  try {
    details = await getOperationDetails(operation.id)
  } catch (err) {
    console.error('openEditOperation details', err)
  }

  const entreeFlux = (details.flux || []).filter((flux) => flux.typeFlux === 'ENTREE')
  const sortieFlux = (details.flux || []).find((flux) => flux.typeFlux === 'SORTIE') || {}

  operationForm.value = {
    id: operation.id,
    numOrdreFab: operation.numOrdreFab,
    typeOperation: operation.typeOperation,
    statutOperation: operation.statutOperation,
    dateDebut: operation.dateDebut,
    dateFin: operation.dateFin || '',
    operateur: operation.operateur || '',
    equipementId: operation.equipementId || '',
    dureeEstimee: operation.dureeEstimee || 60,
    entreeFlux: entreeFlux.length > 0
      ? entreeFlux.map((flux) => ({
          articleMatiereId: flux.articleMatiereId || '',
          mesureCapteur: flux.mesureCapteur || '',
          mesureDiametre: flux.mesureDiametre || '',
        }))
      : [emptyEntreeFlux()],
    sortieArticleMatiereId: sortieFlux.articleMatiereId || '',
    sortieMesureCapteur: sortieFlux.mesureCapteur || '',
    sortieMesureDiametre: sortieFlux.mesureDiametre || '',
  }
  showOperationModal.value = true
}

function closeOperationModal() {
  showOperationModal.value = false
  operationForm.value = emptyOperationForm()
}

async function saveOperation() {
  const isClosing = Boolean(operationForm.value.dateFin || operationForm.value.statutOperation === 'TERMINEE')
  const validationError = getOperationValidationError(isClosing)

  if (validationError) {
    alert(validationError)
    return
  }

  const equipementId = toSafeNumber(operationForm.value.equipementId)
  const dureeEstimee = toSafeNumber(operationForm.value.dureeEstimee)
  const entreeFlux = operationForm.value.entreeFlux.map((flux) => ({
    articleMatiereId: toSafeNumber(flux.articleMatiereId),
    mesureCapteur: toSafeNumber(flux.mesureCapteur),
    mesureDiametre: toSafeNumber(flux.mesureDiametre),
  }))
  const sortieArticleMatiereId = toSafeNumber(operationForm.value.sortieArticleMatiereId)
  const sortieMesureCapteur = toSafeNumber(operationForm.value.sortieMesureCapteur)
  const sortieMesureDiametre = toSafeNumber(operationForm.value.sortieMesureDiametre)

  const payload = {
    numOrdreFab: operationForm.value.numOrdreFab,
    typeOperation: operationForm.value.typeOperation,
    statutOperation: operationForm.value.statutOperation,
    dateDebut: operationForm.value.dateDebut,
    dateFin: operationForm.value.dateFin,
    operateur: operationForm.value.operateur,
    equipementId,
    dureeEstimee,
    entreeFlux,
    sortieArticleMatiereId: isClosing ? sortieArticleMatiereId : null,
    sortieMesureCapteur: isClosing ? sortieMesureCapteur : null,
    sortieMesureDiametre: isClosing ? sortieMesureDiametre : null,
  }

  try {
    if (operationForm.value.id) {
      await updateOperation(operationForm.value.id, payload)
    } else {
      await createOperation(payload)
    }

    closeOperationModal()
    await loadChefDashboard()
    await loadOperations()
  } catch (err) {
    console.error('saveOperation', err)
    alert(err.message || 'Erreur')
  }
}

function getOperationValidationError(isClosing) {
  if (!operationForm.value.numOrdreFab) return 'Ordre fabrication est obligatoire.'
  if (!operationForm.value.typeOperation) return 'Type operation est obligatoire.'
  if (!operationForm.value.statutOperation) return 'Statut operation est obligatoire.'
  if (!operationForm.value.dateDebut) return 'Date debut est obligatoire.'
  if (!operationForm.value.operateur) return 'Operateur est obligatoire.'
  if (!isPositiveNumber(operationForm.value.equipementId)) return 'Choisis un equipement.'
  if (!isPositiveNumber(operationForm.value.dureeEstimee)) return 'Duree estimee doit etre superieure a 0.'

  if (!operationForm.value.entreeFlux.length) {
    return 'Ajoute au moins un flux d entree.'
  }

  for (let index = 0; index < operationForm.value.entreeFlux.length; index++) {
    const flux = operationForm.value.entreeFlux[index]
    const lineNumber = index + 1

    if (!isPositiveNumber(flux.articleMatiereId)) return `Flux entree ${lineNumber} : choisis article.`
    if (!isPositiveNumber(flux.mesureCapteur)) return `Flux entree ${lineNumber} : mesure capteur doit etre superieure a 0.`
    if (!isValidNumber(flux.mesureDiametre)) return `Flux entree ${lineNumber} : mesure diametre est obligatoire.`
  }

  if (!isClosing) {
    return ''
  }

  if (!operationForm.value.dateFin) return 'La date fin est obligatoire pour terminer une operation.'
  if (!isPositiveNumber(operationForm.value.sortieArticleMatiereId)) return 'Choisis article sortie.'
  if (!isValidNumber(operationForm.value.sortieMesureCapteur)) return 'Mesure capteur sortie est obligatoire.'
  if (!isValidNumber(operationForm.value.sortieMesureDiametre)) return 'Mesure diametre sortie est obligatoire.'

  return ''
}

function toSafeNumber(value) {
  if (value === '' || value === null || value === undefined) {
    return null
  }

  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : null
}

function isValidNumber(value) {
  return toSafeNumber(value) !== null
}

function isPositiveNumber(value) {
  const numberValue = toSafeNumber(value)
  return numberValue !== null && numberValue > 0
}

async function removeOperation(id) {
  if (!confirm('Supprimer cette operation ? Les lots lies seront aussi supprimes.')) return

  try {
    await deleteOperation(id)
    if (selectedOperation.value && selectedOperation.value.id === id) {
      selectedOperation.value = null
    }
    await loadChefDashboard()
    await loadOperations()
    await loadLots()
  } catch (err) {
    console.error('removeOperation', err)
    alert(err.message || 'Erreur')
  }
}

const lots = ref([])
const showArticleModal = ref(false)
const articleForm = ref(emptyArticleForm())

function emptyArticleForm() {
  return {
    id: null,
    nomArticle: '',
    categorie: '',
    uniteStandard: 'Tonne',
    densiteStandard: 1,
  }
}

async function loadLots() {
  try {
    const data = await getLots(getCurrentAtelierId())
    lots.value = data.lots || data || []
  } catch (err) {
    console.error('loadLots', err)
  }
}

async function loadArticles() {
  try {
    const data = await getArticles()
    articles.value = data.articles || data || []
  } catch (err) {
    console.error('loadArticles', err)
  }
}

function openCreateArticle() {
  articleForm.value = emptyArticleForm()
  showArticleModal.value = true
}

function openEditArticle(article) {
  articleForm.value = { ...article }
  showArticleModal.value = true
}

function closeArticleModal() {
  showArticleModal.value = false
  articleForm.value = emptyArticleForm()
}

async function saveArticle() {
  if (!articleForm.value.nomArticle || !articleForm.value.categorie || !articleForm.value.uniteStandard) {
    alert('Nom, categorie et unite sont obligatoires.')
    return
  }

  if (!isPositiveNumber(articleForm.value.densiteStandard)) {
    alert('Densite standard doit etre superieure a 0.')
    return
  }

  const payload = {
    nomArticle: articleForm.value.nomArticle,
    categorie: articleForm.value.categorie,
    uniteStandard: articleForm.value.uniteStandard,
    densiteStandard: Number(articleForm.value.densiteStandard),
  }

  try {
    if (articleForm.value.id) {
      await updateArticle(articleForm.value.id, payload)
    } else {
      await createArticle(payload)
    }

    closeArticleModal()
    await loadArticles()
  } catch (err) {
    console.error('saveArticle', err)
    alert(err.message || 'Erreur')
  }
}

async function removeArticle(id) {
  if (!confirm('Supprimer cet article matiere ?')) return

  try {
    await deleteArticle(id)
    await loadArticles()
  } catch (err) {
    console.error('removeArticle', err)
    alert(err.message || 'Erreur')
  }
}

const stats = computed(() => [
  { label: 'Equipements', value: chefDashboard.value.totalEquipements },
  { label: 'Techniciens labo', value: chefDashboard.value.totalTechniciensLabo },
  { label: 'Operations', value: chefDashboard.value.totalOperations },
  { label: 'Lots', value: chefDashboard.value.totalLots },
])

const operationsEnCours = computed(() => {
  return operations.value.filter((operation) => {
    return !operation.dateFin && operation.statutOperation !== 'TERMINEE'
  }).length
})

const operationsTerminees = computed(() => {
  return operations.value.filter((operation) => {
    return operation.dateFin || operation.statutOperation === 'TERMINEE'
  }).length
})

const lotsConformes = computed(() => {
  return lots.value.filter((lot) => lot.statutQualite === 'CONFORME').length
})

const activeMenu = computed(() => {
  if (activeTab.value === 'operation-details') {
    return { label: 'Details operation' }
  }

  return menuItems.find((item) => item.key === activeTab.value) || menuItems[0]
})

const atelierName = computed(() => {
  return chefAtelierName.value || (user && user.value && user.value.atelierName) || 'Atelier non assigne'
})

function getCurrentAtelierId() {
  return Number(chefAtelierId.value || user.value?.atelierId || 0)
}

async function loadChefAtelier() {
  if (getCurrentAtelierId() > 0 || !user.value?.id) {
    return
  }

  try {
    const atelier = await getAtelierByChef(user.value.id)

    if (!atelier) {
      return
    }

    chefAtelierId.value = atelier.id
    chefAtelierName.value = atelier.nomAtelier

    user.value = {
      ...user.value,
      atelierId: atelier.id,
      atelierName: atelier.nomAtelier,
    }

    localStorage.setItem('currentUser', JSON.stringify(user.value))
  } catch (err) {
    console.error('loadChefAtelier', err)
  }
}

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

async function viewOperationDetails(operation) {
  try {
    const details = await getOperationDetails(operation.id)
    operationDetails.value = details
    selectedOperation.value = details.operation || operation
  } catch (err) {
    console.error('viewOperationDetails', err)
    operationDetails.value = emptyOperationDetails()
    selectedOperation.value = operation
  }

  activeTab.value = 'operation-details'
}

function backToDashboard() {
  selectedOperation.value = null
  operationDetails.value = emptyOperationDetails()
  activeTab.value = 'dashboard'
}

async function loadChefDashboard() {
  dashboardError.value = ''

  try {
    const data = await getChefDashboard(getCurrentAtelierId())

    chefDashboard.value = {
      totalEquipements: data.totalEquipements || 0,
      totalTechniciensLabo: data.totalTechniciensLabo || 0,
      totalOperations: data.totalOperations || 0,
      totalLots: data.totalLots || 0,
      activeOperation: data.activeOperation || null,
      recentOperations: data.recentOperations || [],
    }

    if (operations.value.length === 0) {
      operations.value = chefDashboard.value.recentOperations
    }

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
  await loadChefAtelier()
  await loadChefDashboard()
  await loadEquipements()
  await loadTechniciensLabo()
  await loadOperations()
  await loadLots()
  await loadArticles()
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

      <div v-if="showTechnicienModal" class="modal-backdrop">
        <div class="modal-card">
          <div class="modal-header">
            <div>
              <p class="eyebrow">{{ technicienForm.id ? 'Modifier' : 'Nouveau' }}</p>
              <h2>{{ technicienForm.id ? 'Modifier technicien labo' : 'Nouveau technicien labo' }}</h2>
            </div>
            <button class="modal-close" type="button" @click="closeTechnicienModal">Fermer</button>
          </div>

          <form class="admin-form modal-form" @submit.prevent="saveTechnicien">
            <div class="form-grid">
              <div>
                <label>Username</label>
                <input v-model.trim="technicienForm.username" required placeholder="technicien1" />
              </div>

              <div>
                <label>Email</label>
                <input v-model.trim="technicienForm.email" required type="email" placeholder="tech@ocp.local" />
              </div>

              <div>
                <label>Mot de passe</label>
                <input
                  v-model.trim="technicienForm.password"
                  :required="!technicienForm.id"
                  type="password"
                  placeholder="Laisser vide si pas de changement"
                />
              </div>

              <p class="form-help full-field">
                Le role est fixe : TECHNICIEN_LABO. Le technicien sera ajoute directement dans
                {{ atelierName }}.
              </p>
            </div>

            <div class="modal-actions">
              <button class="soft-button no-margin" type="button" @click="closeTechnicienModal">
                Annuler
              </button>
              <button class="primary-button no-margin" type="submit">
                {{ technicienForm.id ? 'Modifier' : 'Creer' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <div v-if="showArticleModal" class="modal-backdrop">
        <div class="modal-card">
          <div class="modal-header">
            <div>
              <p class="eyebrow">{{ articleForm.id ? 'Modifier' : 'Nouveau' }}</p>
              <h2>{{ articleForm.id ? 'Modifier article' : 'Nouvel article matiere' }}</h2>
            </div>
            <button class="modal-close" type="button" @click="closeArticleModal">Fermer</button>
          </div>

          <form class="admin-form modal-form" @submit.prevent="saveArticle">
            <div class="form-grid">
              <div>
                <label>Nom article</label>
                <input v-model.trim="articleForm.nomArticle" required placeholder="Phosphate" />
              </div>

              <div>
                <label>Categorie</label>
                <input v-model.trim="articleForm.categorie" required placeholder="Matiere premiere" />
              </div>

              <div>
                <label>Unite standard</label>
                <input v-model.trim="articleForm.uniteStandard" required placeholder="Tonne" />
              </div>

              <div>
                <label>Densite standard</label>
                <input v-model.number="articleForm.densiteStandard" required min="0.01" step="0.01" type="number" />
              </div>
            </div>

            <div class="modal-actions">
              <button class="soft-button no-margin" type="button" @click="closeArticleModal">
                Annuler
              </button>
              <button class="primary-button no-margin" type="submit">
                {{ articleForm.id ? 'Modifier' : 'Creer' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <div v-if="showOperationModal" class="modal-backdrop">
        <div class="modal-card">
          <div class="modal-header">
            <div>
              <p class="eyebrow">{{ operationForm.id ? 'Modifier' : 'Nouvelle' }}</p>
              <h2>{{ operationForm.id ? 'Modifier operation' : 'Nouvelle operation' }}</h2>
            </div>
            <button class="modal-close" type="button" @click="closeOperationModal">Fermer</button>
          </div>

          <form class="admin-form modal-form" @submit.prevent="saveOperation">
            <div class="form-grid">
              <div>
                <label>Ordre fabrication</label>
                <input v-model.trim="operationForm.numOrdreFab" required placeholder="OF-2026-001" />
              </div>

              <div>
                <label>Type operation</label>
                <input v-model.trim="operationForm.typeOperation" required placeholder="Broyage" />
              </div>

              <div>
                <label>Statut</label>
                <select v-model="operationForm.statutOperation" required>
                  <option value="EN_COURS">En cours</option>
                  <option value="EN_ATTENTE">En attente</option>
                  <option value="TERMINEE">Terminee</option>
                </select>
              </div>

              <div>
                <label>Equipement</label>
                <select v-model.number="operationForm.equipementId" required>
                  <option value="">Choisir un equipement</option>
                  <option v-for="equipement in equipements" :key="equipement.id" :value="equipement.id">
                    {{ equipement.tagIndustriel }} - {{ equipement.nomEquipement }}
                  </option>
                </select>
              </div>

              <div>
                <label>Date debut</label>
                <input v-model="operationForm.dateDebut" required type="date" />
              </div>

              <div>
                <label>Date fin</label>
                <input v-model="operationForm.dateFin" type="date" />
              </div>

              <div>
                <label>Operateur</label>
                <input v-model.trim="operationForm.operateur" required placeholder="Operateur process" />
              </div>

              <div>
                <label>Duree estimee (min)</label>
                <input v-model.number="operationForm.dureeEstimee" required min="1" type="number" />
              </div>

              <div class="form-section full-field">
                <div class="form-section-header">
                  <div>
                    <h3>Flux d'entree</h3>
                    <p>Ajoute toutes les matieres qui entrent dans cette operation.</p>
                  </div>

                  <button class="soft-button no-margin" type="button" @click="addEntreeFlux">
                    Ajouter flux
                  </button>
                </div>
              </div>

              <div
                v-for="(flux, index) in operationForm.entreeFlux"
                :key="index"
                class="flux-row full-field"
              >
                <div>
                  <label>Article entree {{ index + 1 }}</label>
                  <select v-model.number="flux.articleMatiereId" required>
                    <option value="">Choisir un article</option>
                    <option v-for="article in articles" :key="article.id" :value="article.id">
                      {{ article.nomArticle }}
                    </option>
                  </select>
                </div>

                <div>
                  <label>Mesure capteur (t)</label>
                  <input v-model.number="flux.mesureCapteur" required min="0.01" step="0.01" type="number" />
                </div>

                <div>
                  <label>Mesure diametre</label>
                  <input v-model.number="flux.mesureDiametre" required min="0" step="0.01" type="number" />
                </div>

                <button class="action-button danger-button flux-remove" type="button" @click="removeEntreeFlux(index)">
                  Enlever
                </button>
              </div>

              <div
                v-if="operationForm.dateFin || operationForm.statutOperation === 'TERMINEE'"
                class="form-section full-field"
              >
                <h3>Flux de sortie</h3>
                <p>Ces informations sont obligatoires quand l'operation est terminee.</p>
              </div>

              <div v-if="operationForm.dateFin || operationForm.statutOperation === 'TERMINEE'">
                <label>Article sortie</label>
                <select v-model.number="operationForm.sortieArticleMatiereId" required>
                  <option value="">Choisir un article</option>
                  <option v-for="article in articles" :key="article.id" :value="article.id">
                    {{ article.nomArticle }}
                  </option>
                </select>
              </div>

              <div v-if="operationForm.dateFin || operationForm.statutOperation === 'TERMINEE'">
                <label>Mesure capteur sortie (t)</label>
                <input v-model.number="operationForm.sortieMesureCapteur" required min="0" step="0.01" type="number" />
              </div>

              <div v-if="operationForm.dateFin || operationForm.statutOperation === 'TERMINEE'">
                <label>Mesure diametre sortie</label>
                <input v-model.number="operationForm.sortieMesureDiametre" required min="0" step="0.01" type="number" />
              </div>
            </div>

            <div class="modal-actions">
              <button class="soft-button no-margin" type="button" @click="closeOperationModal">
                Annuler
              </button>
              <button class="primary-button no-margin" type="submit">
                {{ operationForm.id ? 'Modifier' : 'Creer' }}
              </button>
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
          <article>
            <span>Equipement</span>
            <strong>{{ selectedOperation.equipementName || 'Non precise' }}</strong>
          </article>
          <article>
            <span>Duree estimee</span>
            <strong>{{ selectedOperation.dureeEstimee || '-' }} min</strong>
          </article>
          <article>
            <span>Operateur</span>
            <strong>{{ selectedOperation.operateur || '-' }}</strong>
          </article>
        </div>

        <div v-if="selectedOperation" class="operation-details-sections">
          <article class="table-card">
            <div class="section-title">
              <div>
                <p class="eyebrow">Flux matiere</p>
                <h2>Entrees et sorties</h2>
              </div>
            </div>

            <div class="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Type flux</th>
                    <th>Article</th>
                    <th>Mesure capteur</th>
                    <th>Mesure diametre</th>
                    <th>Date mesure</th>
                  </tr>
                </thead>

                <tbody>
                  <tr v-if="operationDetails.flux.length === 0">
                    <td class="empty-cell" colspan="5">Aucun flux trouve pour cette operation.</td>
                  </tr>

                  <tr v-for="flux in operationDetails.flux" :key="flux.id">
                    <td><span class="mini-pill">{{ flux.typeFlux }}</span></td>
                    <td>{{ flux.nomArticle }}</td>
                    <td>{{ flux.mesureCapteur }} t</td>
                    <td>{{ flux.mesureDiametre }}</td>
                    <td>{{ flux.dateMesure }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>

          <article class="table-card">
            <div class="section-title">
              <div>
                <p class="eyebrow">Bilan massique</p>
                <h2>Calcul automatique</h2>
              </div>
            </div>

            <div v-if="operationDetails.bilanMassique" class="details-grid compact-grid">
              <article>
                <span>Total entrees</span>
                <strong>{{ operationDetails.bilanMassique.totalEntreesT }} t</strong>
              </article>
              <article>
                <span>Total sorties</span>
                <strong>{{ operationDetails.bilanMassique.totalSortiesT }} t</strong>
              </article>
              <article>
                <span>Ecart pertes</span>
                <strong>{{ operationDetails.bilanMassique.ecartPertesT }} t</strong>
              </article>
              <article>
                <span>Rendement</span>
                <strong>{{ operationDetails.bilanMassique.rendementVal }} %</strong>
              </article>
              <article>
                <span>Date calcul</span>
                <strong>{{ operationDetails.bilanMassique.dateCalcul }}</strong>
              </article>
            </div>

            <p v-else class="empty-details">
              Le bilan sera calcule quand le flux de sortie sera saisi.
            </p>
          </article>

          <article class="table-card">
            <div class="section-title">
              <div>
                <p class="eyebrow">Production</p>
                <h2>Lots de production</h2>
              </div>
            </div>

            <div class="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Lot</th>
                    <th>Article</th>
                    <th>Date</th>
                    <th>Statut qualite</th>
                  </tr>
                </thead>

                <tbody>
                  <tr v-if="operationDetails.lots.length === 0">
                    <td class="empty-cell" colspan="4">Aucun lot genere pour cette operation.</td>
                  </tr>

                  <tr v-for="lot in operationDetails.lots" :key="lot.id">
                    <td class="strong-cell">LOT-{{ lot.id }}</td>
                    <td>{{ lot.nomArticle }}</td>
                    <td>{{ lot.date }}</td>
                    <td><span class="mini-pill">{{ lot.statutQualite }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>

          <article class="table-card">
            <div class="section-title">
              <div>
                <p class="eyebrow">Laboratoire</p>
                <h2>Analyses labo</h2>
              </div>
            </div>

            <div class="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Lot</th>
                    <th>P2O5</th>
                    <th>Cadmium ppm</th>
                    <th>Solides suspendu</th>
                    <th>Date analyse</th>
                  </tr>
                </thead>

                <tbody>
                  <tr v-if="operationDetails.analyses.length === 0">
                    <td class="empty-cell" colspan="5">Aucune analyse labo pour cette operation.</td>
                  </tr>

                  <tr v-for="analyse in operationDetails.analyses" :key="analyse.id">
                    <td class="strong-cell">LOT-{{ analyse.lotProductionId }}</td>
                    <td>{{ analyse.tauxP2O5 }}</td>
                    <td>{{ analyse.tauxCadmiumPpm }}</td>
                    <td>{{ analyse.solidesSuspendu }}</td>
                    <td>{{ analyse.dateAnalyse }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </article>
        </div>

        <p v-else class="empty-details">
          Aucune operation selectionnee.
        </p>
      </section>

      <section v-if="activeTab === 'equipements'" class="panel equipements-panel">
        <div class="equipements-hero">
          <div class="equipements-title">
            <p class="eyebrow">Patrimoine atelier</p>
            <h2>Equipements</h2>
            <p>
              Gestion simple des equipements industriels affectes a
              {{ atelierName }}.
            </p>
          </div>

          <button class="primary-button" type="button" @click="openCreateEquipement">
            Ajouter equipement
          </button>
        </div>

        <div class="equipements-metrics">
          <article>
            <span>Total equipements</span>
            <strong>{{ equipements.length }}</strong>
          </article>

          <article>
            <span>Atelier</span>
            <strong>{{ atelierName }}</strong>
          </article>

          <article>
            <span>Suivi</span>
            <strong>Chef atelier</strong>
          </article>
        </div>

        <div class="table-card equipement-table-card">
          <div class="section-title">
            <div>
              <p class="eyebrow">Liste technique</p>
              <h2>Equipements de l'atelier</h2>
            </div>
          </div>

          <div class="table-wrapper equipement-table-wrapper">
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
                <tr v-if="equipements.length === 0">
                  <td class="empty-cell" colspan="4">
                    Aucun equipement trouve pour cet atelier.
                  </td>
                </tr>

                <tr v-for="equipement in equipements" :key="equipement.id">
                  <td>
                    <span class="equipement-code">
                      {{ equipement.tagIndustriel }}
                    </span>
                  </td>

                  <td>
                    <div class="equipement-name">
                      <strong>{{ equipement.nomEquipement }}</strong>
                      <small>ID : {{ equipement.id }}</small>
                    </div>
                  </td>

                  <td>
                    <span class="type-pill">{{ equipement.typeEquipement }}</span>
                  </td>

                  <td class="actions">
                    <button
                      class="action-button"
                      type="button"
                      @click="openEditEquipement(equipement)"
                    >
                      Modifier
                    </button>
                    <button
                      class="action-button danger-button"
                      type="button"
                      @click="removeEquipement(equipement.id)"
                    >
                      Supprimer
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section v-if="activeTab === 'labo'" class="panel labo-panel">
        <div class="section-title labo-header">
          <div>
            <p class="eyebrow">Laboratoire</p>
            <h2>Techniciens labo</h2>
            <p>CRUD simple des users avec role technicien labo pour {{ atelierName }}.</p>
          </div>

          <button class="primary-button no-margin" type="button" @click="openCreateTechnicien">
            Ajouter technicien
          </button>
        </div>

        <div class="labo-metrics">
          <article>
            <span>Total techniciens</span>
            <strong>{{ techniciensLabo.length }}</strong>
          </article>

          <article>
            <span>Atelier</span>
            <strong>{{ atelierName }}</strong>
          </article>

          <article>
            <span>Role autorise</span>
            <strong>Technicien labo</strong>
          </article>
        </div>

        <article class="table-card labo-table-card">
          <div class="section-title">
            <div>
              <p class="eyebrow">Users laboratoire</p>
              <h2>Liste des techniciens</h2>
            </div>

            <button class="soft-button no-margin" type="button" @click="loadTechniciensLabo">
              Actualiser
            </button>
          </div>

          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Email</th>
                  <th>Atelier</th>
                  <th>Role</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                <tr v-if="techniciensLabo.length === 0">
                  <td class="empty-cell" colspan="5">
                    Aucun technicien labo trouve pour cet atelier.
                  </td>
                </tr>

                <tr v-for="technicien in techniciensLabo" :key="technicien.id">
                  <td>
                    <div class="technicien-name">
                      <strong>{{ technicien.username }}</strong>
                      <small>ID : {{ technicien.id }}</small>
                    </div>
                  </td>
                  <td>{{ technicien.email }}</td>
                  <td>{{ technicien.atelierName || atelierName }}</td>
                  <td><span class="mini-pill">Technicien labo</span></td>
                  <td class="actions">
                    <button
                      class="action-button"
                      type="button"
                      @click="openEditTechnicien(technicien)"
                    >
                      Modifier
                    </button>
                    <button
                      class="action-button danger-button"
                      type="button"
                      @click="removeTechnicien(technicien.id)"
                    >
                      Supprimer
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>
      </section>

      <section v-if="activeTab === 'articles'" class="panel crud-panel">
        <div class="section-title">
          <div>
            <p class="eyebrow">Matiere</p>
            <h2>Articles matiere</h2>
            <p>Gestion des articles utilises dans les flux d'entree, de sortie et les lots.</p>
          </div>

          <button class="primary-button no-margin" type="button" @click="openCreateArticle">
            Ajouter article
          </button>
        </div>

        <div class="labo-metrics crud-metrics">
          <article>
            <span>Total articles</span>
            <strong>{{ articles.length }}</strong>
          </article>

          <article>
            <span>Unite principale</span>
            <strong>Tonne</strong>
          </article>

          <article>
            <span>Utilisation</span>
            <strong>Flux et lots</strong>
          </article>
        </div>

        <article class="table-card labo-table-card">
          <div class="section-title">
            <div>
              <p class="eyebrow">Referentiel</p>
              <h2>Liste des articles</h2>
            </div>

            <button class="soft-button no-margin" type="button" @click="loadArticles">
              Actualiser
            </button>
          </div>

          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Article</th>
                  <th>Categorie</th>
                  <th>Unite</th>
                  <th>Densite</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                <tr v-if="articles.length === 0">
                  <td class="empty-cell" colspan="5">
                    Aucun article matiere trouve.
                  </td>
                </tr>

                <tr v-for="article in articles" :key="article.id">
                  <td>
                    <div class="technicien-name">
                      <strong>{{ article.nomArticle }}</strong>
                      <small>ID : {{ article.id }}</small>
                    </div>
                  </td>
                  <td>{{ article.categorie }}</td>
                  <td><span class="type-pill">{{ article.uniteStandard }}</span></td>
                  <td>{{ article.densiteStandard }}</td>
                  <td class="actions">
                    <button class="action-button" type="button" @click="openEditArticle(article)">
                      Modifier
                    </button>
                    <button
                      class="action-button danger-button"
                      type="button"
                      @click="removeArticle(article.id)"
                    >
                      Supprimer
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>
      </section>

      <section v-if="activeTab === 'operations'" class="panel crud-panel">
        <div class="section-title">
          <div>
            <p class="eyebrow">Process</p>
            <h2>Operations process</h2>
            <p>CRUD simple des operations de fabrication de {{ atelierName }}.</p>
          </div>

          <button class="primary-button no-margin" type="button" @click="openCreateOperation">
            Ajouter operation
          </button>
        </div>

        <div class="labo-metrics crud-metrics">
          <article>
            <span>Total operations</span>
            <strong>{{ operations.length }}</strong>
          </article>

          <article>
            <span>En cours</span>
            <strong>{{ operationsEnCours }}</strong>
          </article>

          <article>
            <span>Terminees</span>
            <strong>{{ operationsTerminees }}</strong>
          </article>
        </div>

        <article class="table-card labo-table-card">
          <div class="section-title">
            <div>
              <p class="eyebrow">Liste process</p>
              <h2>Operations de l'atelier</h2>
            </div>

            <button class="soft-button no-margin" type="button" @click="loadOperations">
              Actualiser
            </button>
          </div>

          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Ordre fab</th>
                  <th>Type</th>
                  <th>Equipement</th>
                  <th>Date debut</th>
                  <th>Date fin</th>
                  <th>Statut</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                <tr v-if="operations.length === 0">
                  <td class="empty-cell" colspan="7">
                    Aucune operation trouvee pour cet atelier.
                  </td>
                </tr>

                <tr v-for="operation in operations" :key="operation.id">
                  <td class="strong-cell">{{ operation.numOrdreFab }}</td>
                  <td>{{ operation.typeOperation }}</td>
                  <td>{{ operation.equipementName || 'Non precise' }}</td>
                  <td>{{ operation.dateDebut }}</td>
                  <td>{{ operation.dateFin || 'Non terminee' }}</td>
                  <td><span class="mini-pill">{{ operation.statutOperation }}</span></td>
                  <td class="actions">
                    <button class="action-button" type="button" @click="viewOperationDetails(operation)">
                      Details
                    </button>
                    <button class="action-button" type="button" @click="openEditOperation(operation)">
                      Modifier
                    </button>
                    <button
                      class="action-button danger-button"
                      type="button"
                      @click="removeOperation(operation.id)"
                    >
                      Supprimer
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>
      </section>

      <section v-if="activeTab === 'lots'" class="panel crud-panel">
        <div class="section-title">
          <div>
            <p class="eyebrow">Production</p>
            <h2>Lots de production</h2>
            <p>Affichage des lots generes automatiquement apres la sortie operation.</p>
          </div>
        </div>

        <div class="labo-metrics crud-metrics">
          <article>
            <span>Total lots</span>
            <strong>{{ lots.length }}</strong>
          </article>

          <article>
            <span>Conformes</span>
            <strong>{{ lotsConformes }}</strong>
          </article>

          <article>
            <span>Articles</span>
            <strong>{{ articles.length }}</strong>
          </article>
        </div>

        <article class="table-card labo-table-card">
          <div class="section-title">
            <div>
              <p class="eyebrow">Suivi production</p>
              <h2>Liste des lots</h2>
            </div>

            <button class="soft-button no-margin" type="button" @click="loadLots">
              Actualiser
            </button>
          </div>

          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Lot</th>
                  <th>Operation</th>
                  <th>Article</th>
                  <th>Date</th>
                  <th>Statut qualite</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                <tr v-if="lots.length === 0">
                  <td class="empty-cell" colspan="6">
                    Aucun lot trouve pour cet atelier.
                  </td>
                </tr>

                <tr v-for="lot in lots" :key="lot.id">
                  <td class="strong-cell">LOT-{{ lot.id }}</td>
                  <td>{{ lot.numOrdreFab }}</td>
                  <td>{{ lot.nomArticle }}</td>
                  <td>{{ lot.date }}</td>
                  <td><span class="mini-pill">{{ lot.statutQualite }}</span></td>
                  <td class="actions">
                    <button
                      class="action-button"
                      type="button"
                      @click="viewOperationDetails({ id: lot.operationProcessId })"
                    >
                      Voir details
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>
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
  width: min(860px, 100%);
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

.admin-form input:focus,
.admin-form select:focus {
  border-color: var(--green);
  box-shadow: 0 0 0 3px rgb(15 122 63 / 12%);
}

.form-help {
  margin-top: 8px;
  color: #7a5a18;
  font-size: 12px;
  font-weight: 750;
  line-height: 1.5;
}

.full-field {
  grid-column: 1 / -1;
}

.form-section {
  margin-top: 8px;
  padding: 12px;
  background: #f8faf9;
  border: 1px solid var(--border);
  border-radius: 10px;
}

.form-section h3 {
  color: var(--title);
  font-size: 15px;
}

.form-section p {
  margin-top: 4px;
  color: var(--text);
  font-size: 13px;
}

.form-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.flux-row {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr auto;
  gap: 12px;
  padding: 12px;
  align-items: end;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 10px;
}

.flux-remove {
  height: 40px;
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

.labo-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.labo-metrics article {
  padding: 15px;
  background: #f8faf9;
  border: 1px solid var(--border);
  border-radius: 12px;
}

.labo-metrics span,
.labo-metrics strong {
  display: block;
}

.labo-metrics span {
  color: var(--text);
  font-size: 12px;
  font-weight: 850;
  text-transform: uppercase;
}

.labo-metrics strong {
  margin-top: 7px;
  color: var(--title);
  font-size: 18px;
  line-height: 1.25;
}

.labo-table-card {
  margin-top: 16px;
}

.technicien-name {
  display: grid;
  gap: 4px;
}

.technicien-name strong {
  color: var(--title);
  font-size: 14px;
}

.technicien-name small {
  color: var(--text);
  font-size: 12px;
  font-weight: 750;
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

.equipements-panel {
  padding: 0;
  overflow: hidden;
  background: #f8faf9;
}

.equipements-hero {
  display: flex;
  padding: 22px;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  background:
    linear-gradient(135deg, rgb(255 255 255 / 96%), rgb(235 247 241 / 92%)),
    #fff;
  border-bottom: 1px solid var(--border);
}

.equipements-title h2 {
  margin-top: 4px;
  color: var(--title);
  font-size: 27px;
  line-height: 1.15;
}

.equipements-title p:last-child {
  max-width: 620px;
  margin-top: 7px;
  color: var(--text);
  font-size: 14px;
}

.equipements-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  padding: 18px;
}

.equipements-metrics article {
  padding: 15px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 12px;
  box-shadow: 0 10px 24px rgb(15 23 42 / 4%);
}

.equipements-metrics span,
.equipements-metrics strong {
  display: block;
}

.equipements-metrics span {
  color: var(--text);
  font-size: 12px;
  font-weight: 850;
  text-transform: uppercase;
}

.equipements-metrics strong {
  margin-top: 7px;
  color: var(--title);
  font-size: 18px;
  line-height: 1.25;
}

.equipement-table-card {
  margin: 0 18px 18px;
}

.equipement-table-wrapper table {
  min-width: 780px;
}

.equipement-code,
.type-pill {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  border-radius: 999px;
}

.equipement-code {
  padding: 6px 10px;
  color: var(--green-dark);
  font-size: 12px;
  font-weight: 900;
  background: var(--green-light);
  border: 1px solid #b9e4ca;
}

.equipement-name {
  display: grid;
  gap: 4px;
}

.equipement-name strong {
  color: var(--title);
  font-size: 14px;
}

.equipement-name small {
  color: var(--text);
  font-size: 12px;
  font-weight: 750;
}

.type-pill {
  padding: 5px 9px;
  color: #475467;
  font-size: 12px;
  font-weight: 850;
  background: #f2f4f7;
  border: 1px solid #e4e7ec;
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

.operation-details-sections {
  display: grid;
  gap: 16px;
  margin-top: 18px;
}

.compact-grid {
  grid-template-columns: repeat(5, minmax(0, 1fr));
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

.actions {
  display: flex;
  gap: 8px;
}

.action-button {
  padding: 7px 10px;
  color: var(--green-dark);
  font: inherit;
  font-size: 12px;
  font-weight: 850;
  white-space: nowrap;
  background: #fff;
  border: 1px solid #b9e4ca;
  border-radius: 8px;
  cursor: pointer;
}

.action-button:hover {
  background: var(--green-light);
}

.danger-button {
  color: #b42318;
  border-color: #fecdca;
}

.danger-button:hover {
  background: #fff1f0;
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
  display: inline-flex;
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
}

.primary-button:hover {
  background: var(--green-dark);
  border-color: var(--green-dark);
}

.soft-button {
  display: inline-flex;
  padding: 10px 14px;
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
}

.soft-button:hover {
  background: var(--green-light);
}

.no-margin {
  margin-top: 0;
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
  .labo-metrics,
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
  .labo-metrics,
  .details-grid {
    grid-template-columns: 1fr;
  }

  .details-header,
  .section-title {
    align-items: flex-start;
    flex-direction: column;
  }

  .form-section-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .flux-row {
    grid-template-columns: 1fr;
  }

  .equipements-hero {
    flex-direction: column;
    padding: 18px;
  }

  .equipements-hero .primary-button {
    width: 100%;
  }

  .equipements-metrics {
    grid-template-columns: 1fr;
    padding: 14px;
  }

  .equipement-table-card {
    margin: 0 14px 14px;
  }

  .panel,
  .chef-topbar {
    padding: 16px;
  }
}
</style>
