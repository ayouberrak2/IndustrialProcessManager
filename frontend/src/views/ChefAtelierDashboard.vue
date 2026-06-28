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
  updateLotStatus,
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
  equipementsEnPanne: 0,
  operationsEnCours: 0,
  lotsNonConformes: 0,
  totalFluxEntree: 0,
  totalFluxSortie: 0,
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
    statutEquipement: 'OPERATIONNEL',
  },
  {
    id: 2,
    tagIndustriel: 'EQ-204',
    nomEquipement: 'Broyeur principal',
    typeEquipement: 'Broyeur',
    statutEquipement: 'OPERATIONNEL',
  },
  {
    id: 3,
    tagIndustriel: 'EQ-318',
    nomEquipement: 'Pompe acide',
    typeEquipement: 'Pompe',
    statutEquipement: 'EN_PANNE',
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
    statutEquipement: 'OPERATIONNEL',
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
    await loadChefDashboard()
  } catch (err) {
    console.error('saveEquipement', err)
    alert(err.message || 'Erreur')
  }
}

async function toggleEquipementPanne(equipement) {
  const nextStatut = isEquipementEnPanne(equipement) ? 'OPERATIONNEL' : 'EN_PANNE'
  const oldStatut = equipement.statutEquipement

  equipement.statutEquipement = nextStatut

  try {
    const updated = await updateEquipement(equipement.id, {
      ...equipement,
      statutEquipement: nextStatut,
    })
    Object.assign(equipement, updated)
    await loadChefDashboard()
  } catch (err) {
    equipement.statutEquipement = oldStatut
    console.error('toggleEquipementPanne', err)
    alert(err.message || 'Erreur changement statut equipement')
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

function formatNumber(value) {
  const numberValue = Number(value || 0)

  if (!Number.isFinite(numberValue)) {
    return '0'
  }

  return new Intl.NumberFormat('fr-FR', {
    maximumFractionDigits: 2,
  }).format(numberValue)
}

function isEquipementEnPanne(equipement) {
  return equipement.statutEquipement === 'EN_PANNE'
}

function getEquipementStatutLabel(equipement) {
  return isEquipementEnPanne(equipement) ? 'En panne' : 'Operationnel'
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
const lotStatusSavingId = ref(null)
const showLotAnalysesModal = ref(false)
const selectedLotForAnalyses = ref(null)
const selectedLotAnalyses = ref([])
const lotAnalysesLoading = ref(false)
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

async function changeLotStatus(lot, newStatus) {
  const oldStatus = lot.statutQualite

  if (oldStatus === newStatus) {
    return
  }

  lotStatusSavingId.value = lot.id

  try {
    const updatedLot = await updateLotStatus(lot, newStatus)
    Object.assign(lot, updatedLot)
    await loadChefDashboard()
  } catch (err) {
    lot.statutQualite = oldStatus
    console.error('changeLotStatus', err)
    alert(err.message || 'Erreur changement statut lot')
  } finally {
    lotStatusSavingId.value = null
  }
}

async function openLotAnalyses(lot) {
  selectedLotForAnalyses.value = lot
  selectedLotAnalyses.value = []
  showLotAnalysesModal.value = true
  lotAnalysesLoading.value = true

  try {
    const details = await getOperationDetails(lot.operationProcessId)
    const analyses = details.analyses || []

    selectedLotAnalyses.value = analyses.filter((analyse) => {
      return Number(analyse.lotProductionId) === Number(lot.id)
    })
  } catch (err) {
    console.error('openLotAnalyses', err)
    alert(err.message || 'Erreur chargement analyses labo')
  } finally {
    lotAnalysesLoading.value = false
  }
}

function closeLotAnalysesModal() {
  showLotAnalysesModal.value = false
  selectedLotForAnalyses.value = null
  selectedLotAnalyses.value = []
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
  { label: 'Equipements en panne', value: chefDashboard.value.equipementsEnPanne, tone: 'danger', icon: '!' },
  { label: 'Operations en cours', value: chefDashboard.value.operationsEnCours, tone: 'warning', icon: 'O' },
  { label: 'Lots non conformes', value: chefDashboard.value.lotsNonConformes, tone: 'danger', icon: 'L' },
  { label: 'Flux entree total', value: `${formatNumber(chefDashboard.value.totalFluxEntree)} t`, tone: 'success', icon: 'E' },
  { label: 'Flux sortie total', value: `${formatNumber(chefDashboard.value.totalFluxSortie)} t`, tone: 'success', icon: 'S' },
  { label: 'Total equipements', value: chefDashboard.value.totalEquipements, tone: 'neutral', icon: 'T' },
  { label: 'Lots production', value: chefDashboard.value.totalLots, tone: 'neutral', icon: 'P' },
  { label: 'Techniciens labo', value: chefDashboard.value.totalTechniciensLabo, tone: 'neutral', icon: 'N' },
])

const equipementsEnPanne = computed(() => {
  return equipements.value.filter(isEquipementEnPanne).length
})

const equipementsOperationnels = computed(() => {
  return equipements.value.length - equipementsEnPanne.value
})

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

const lotsNonConformes = computed(() => {
  return lots.value.filter((lot) => lot.statutQualite === 'NON_CONFORME').length
})

const tauxLotsConformes = computed(() => {
  if (lots.value.length === 0) {
    return '0%'
  }

  return `${Math.round((lotsConformes.value / lots.value.length) * 100)}%`
})

const operationEntreeTotal = computed(() => {
  return operationForm.value.entreeFlux.reduce((total, flux) => {
    return total + (toSafeNumber(flux.mesureCapteur) || 0)
  }, 0)
})

const operationFluxEntrees = computed(() => {
  return operationDetails.value.flux.filter((flux) => flux.typeFlux === 'ENTREE')
})

const operationFluxSorties = computed(() => {
  return operationDetails.value.flux.filter((flux) => flux.typeFlux === 'SORTIE')
})

const operationDetailsEntreeTotal = computed(() => {
  return operationFluxEntrees.value.reduce((total, flux) => {
    return total + (toSafeNumber(flux.mesureCapteur) || 0)
  }, 0)
})

const operationDetailsSortieTotal = computed(() => {
  return operationFluxSorties.value.reduce((total, flux) => {
    return total + (toSafeNumber(flux.mesureCapteur) || 0)
  }, 0)
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
  activeTab.value = 'operations'
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
      equipementsEnPanne: data.equipementsEnPanne || 0,
      operationsEnCours: data.operationsEnCours || 0,
      lotsNonConformes: data.lotsNonConformes || 0,
      totalFluxEntree: data.totalFluxEntree || 0,
      totalFluxSortie: data.totalFluxSortie || 0,
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

async function refreshDashboardData() {
  await Promise.all([
    loadChefDashboard(),
    loadEquipements(),
    loadOperations(),
    loadLots(),
  ])
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
          <button class="primary-button no-margin topbar-button" type="button" @click="refreshDashboardData">
            Actualiser
          </button>
          <span class="primary-button no-margin topbar-button chef-role-pill">
            {{ user.role }}
          </span>
        </div>
      </header>

      <p v-if="dashboardError" class="alert-error">{{ dashboardError }}</p>

      <section v-if="activeTab === 'dashboard'" class="panel dashboard-panel">
        <div class="dashboard-welcome">
          <div>
            <p class="eyebrow">Pilotage atelier</p>
            <h2>Bonjour {{ user.username }}</h2>
            <p>
              Suivi clair de {{ atelierName }} avec les operations, les lots,
              les techniciens labo et les equipements essentiels.
            </p>
          </div>

          <div class="dashboard-date-card">
            <span>Mois affiche</span>
            <strong>{{ calendarTitle }}</strong>
          </div>
        </div>

        <div class="stats-grid">
          <article v-for="item in stats" :key="item.label" class="stat-card" :class="`stat-card-${item.tone}`">
            <div class="stat-card-head">
              <span>{{ item.label }}</span>
              <small>{{ item.icon }}</small>
            </div>
            <strong>{{ item.value }}</strong>
          </article>
        </div>

        <div class="dashboard-grid">
          <article class="info-card dashboard-info-card">
            <p class="eyebrow">Atelier</p>
            <h2>{{ atelierName }}</h2>
            <p>
              Cette page donne une vue simple sur les elements importants
              pour le chef atelier.
            </p>
          </article>

          <article class="info-card dashboard-info-card active-operation-card">
            <p class="eyebrow">Operation active</p>
            <template v-if="activeOperation">
              <h2>{{ activeOperation.numOrdreFab }}</h2>
              <p>{{ activeOperation.typeOperation }} - {{ activeOperation.statutOperation }}</p>
              <button class="details-button" type="button" @click="viewOperationDetails(activeOperation)">
                Voir details
              </button>
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

        <div class="table-card dashboard-table-card">
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

              <div>
                <label>Statut</label>
                <select v-model="equipementForm.statutEquipement" required>
                  <option value="OPERATIONNEL">Operationnel</option>
                  <option value="EN_PANNE">En panne</option>
                </select>
              </div>
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
                    {{ equipement.tagIndustriel }} - {{ equipement.nomEquipement }}{{ isEquipementEnPanne(equipement) ? ' (en panne)' : '' }}
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
                    <small>Total entree : {{ operationEntreeTotal }} t</small>
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

      <section v-if="activeTab === 'operation-details'" class="panel operation-details-panel">
        <div class="operation-hero">
          <div class="operation-hero-main">
            <p class="eyebrow">Fiche operation</p>
            <h2>{{ selectedOperation ? selectedOperation.numOrdreFab : 'Details operation' }}</h2>
            <p v-if="selectedOperation">
              Informations organisees pour suivre le statut, les flux, le bilan,
              les lots et les analyses labo sans afficher les identifiants internes.
            </p>
            <p v-else>
              Selectionnez une operation depuis la liste pour voir ses details.
            </p>
          </div>

          <div class="operation-hero-actions">
            <span v-if="selectedOperation" class="operation-status-pill">
              {{ selectedOperation.statutOperation || 'EN_COURS' }}
            </span>
            <button class="details-button" type="button" @click="backToDashboard">
              Retour operations
            </button>
          </div>
        </div>

        <template v-if="selectedOperation">
          <div class="operation-quick-steps">
            <article class="operation-step is-done">
              <span>01</span>
              <div>
                <strong>Operation demarree</strong>
                <p>{{ selectedOperation.dateDebut }}</p>
              </div>
            </article>

            <article class="operation-step is-done">
              <span>02</span>
              <div>
                <strong>Flux d'entree</strong>
                <p>{{ operationFluxEntrees.length }} ligne(s) saisie(s)</p>
              </div>
            </article>

            <article class="operation-step" :class="{ 'is-done': operationFluxSorties.length > 0 }">
              <span>03</span>
              <div>
                <strong>Flux de sortie</strong>
                <p>{{ operationFluxSorties.length > 0 ? 'Sortie saisie' : 'En attente' }}</p>
              </div>
            </article>

            <article class="operation-step" :class="{ 'is-done': operationDetails.bilanMassique }">
              <span>04</span>
              <div>
                <strong>Bilan massique</strong>
                <p>{{ operationDetails.bilanMassique ? 'Calcule' : 'En attente' }}</p>
              </div>
            </article>
          </div>

          <div class="operation-summary-grid">
            <article>
              <span>Type operation</span>
              <strong>{{ selectedOperation.typeOperation }}</strong>
            </article>
            <article>
              <span>Equipement</span>
              <strong>{{ selectedOperation.equipementName || 'Non precise' }}</strong>
            </article>
            <article>
              <span>Operateur</span>
              <strong>{{ selectedOperation.operateur || '-' }}</strong>
            </article>
            <article>
              <span>Total entree</span>
              <strong>{{ formatNumber(operationDetailsEntreeTotal) }} t</strong>
            </article>
            <article>
              <span>Total sortie</span>
              <strong>{{ formatNumber(operationDetailsSortieTotal) }} t</strong>
            </article>
            <article>
              <span>Date fin</span>
              <strong>{{ selectedOperation.dateFin || 'Non terminee' }}</strong>
            </article>
            <article>
              <span>Duree estimee</span>
              <strong>{{ selectedOperation.dureeEstimee || '-' }} min</strong>
            </article>
            <article>
              <span>Atelier</span>
              <strong>{{ atelierName }}</strong>
            </article>
          </div>

          <div class="operation-details-layout">
            <article class="detail-card detail-card-large">
              <div class="section-title">
                <div>
                  <p class="eyebrow">Flux matiere</p>
                  <h2>Entree et sortie separees</h2>
                </div>
                <span class="mini-pill">{{ operationDetails.flux.length }} flux</span>
              </div>

              <div class="flux-split-grid">
                <div class="flux-panel">
                  <div class="flux-panel-title">
                    <strong>Flux d'entree</strong>
                    <span>{{ operationFluxEntrees.length }} ligne(s)</span>
                  </div>

                  <div class="table-wrapper detail-table-wrapper">
                    <table>
                      <thead>
                        <tr>
                          <th>Article</th>
                          <th>Mesure capteur</th>
                          <th>Diametre</th>
                          <th>Date</th>
                        </tr>
                      </thead>

                      <tbody>
                        <tr v-if="operationFluxEntrees.length === 0">
                          <td class="empty-cell" colspan="4">Aucun flux d'entree saisi.</td>
                        </tr>

                        <tr v-for="flux in operationFluxEntrees" :key="flux.id">
                          <td class="strong-cell">{{ flux.nomArticle }}</td>
                          <td>{{ flux.mesureCapteur }} t</td>
                          <td>{{ flux.mesureDiametre || '-' }}</td>
                          <td>{{ flux.dateMesure }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>

                <div class="flux-panel">
                  <div class="flux-panel-title">
                    <strong>Flux de sortie</strong>
                    <span>{{ operationFluxSorties.length }} ligne(s)</span>
                  </div>

                  <div class="table-wrapper detail-table-wrapper">
                    <table>
                      <thead>
                        <tr>
                          <th>Article</th>
                          <th>Mesure capteur</th>
                          <th>Diametre</th>
                          <th>Date</th>
                        </tr>
                      </thead>

                      <tbody>
                        <tr v-if="operationFluxSorties.length === 0">
                          <td class="empty-cell" colspan="4">La sortie sera visible apres la fin d'operation.</td>
                        </tr>

                        <tr v-for="flux in operationFluxSorties" :key="flux.id">
                          <td class="strong-cell">{{ flux.nomArticle }}</td>
                          <td>{{ flux.mesureCapteur }} t</td>
                          <td>{{ flux.mesureDiametre || '-' }}</td>
                          <td>{{ flux.dateMesure }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </article>

            <article class="detail-card">
              <div class="section-title">
                <div>
                  <p class="eyebrow">Bilan massique</p>
                  <h2>Resume calcul</h2>
                </div>
              </div>

              <div v-if="operationDetails.bilanMassique" class="bilan-grid">
                <article class="bilan-metric main-metric">
                  <span>Rendement</span>
                  <strong>{{ operationDetails.bilanMassique.rendementVal }} %</strong>
                </article>
                <article class="bilan-metric">
                  <span>Total entrees</span>
                  <strong>{{ operationDetails.bilanMassique.totalEntreesT }} t</strong>
                </article>
                <article class="bilan-metric">
                  <span>Total sorties</span>
                  <strong>{{ operationDetails.bilanMassique.totalSortiesT }} t</strong>
                </article>
                <article class="bilan-metric">
                  <span>Ecart pertes</span>
                  <strong>{{ operationDetails.bilanMassique.ecartPertesT }} t</strong>
                </article>
                <article class="bilan-metric">
                  <span>Date calcul</span>
                  <strong>{{ operationDetails.bilanMassique.dateCalcul }}</strong>
                </article>
              </div>

              <p v-else class="operation-empty-note">
                Le bilan sera calcule automatiquement quand le flux de sortie sera saisi.
              </p>
            </article>

            <article class="detail-card">
              <div class="section-title">
                <div>
                  <p class="eyebrow">Production</p>
                  <h2>Lots generes</h2>
                </div>
                <span class="mini-pill">{{ operationDetails.lots.length }} lot(s)</span>
              </div>

              <div class="table-wrapper detail-table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>Lot</th>
                      <th>Article</th>
                      <th>Date</th>
                      <th>Qualite</th>
                    </tr>
                  </thead>

                  <tbody>
                    <tr v-if="operationDetails.lots.length === 0">
                      <td class="empty-cell" colspan="4">Aucun lot genere pour cette operation.</td>
                    </tr>

                    <tr v-for="lot in operationDetails.lots" :key="lot.id">
                      <td class="strong-cell">Lot production</td>
                      <td>{{ lot.nomArticle }}</td>
                      <td>{{ lot.date }}</td>
                      <td><span class="mini-pill">{{ lot.statutQualite }}</span></td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </article>

            <article class="detail-card">
              <div class="section-title">
                <div>
                  <p class="eyebrow">Laboratoire</p>
                  <h2>Analyses liees</h2>
                </div>
                <span class="mini-pill">{{ operationDetails.analyses.length }} analyse(s)</span>
              </div>

              <div class="table-wrapper detail-table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>Lot</th>
                      <th>P2O5</th>
                      <th>Cadmium ppm</th>
                      <th>Solides</th>
                      <th>Date analyse</th>
                    </tr>
                  </thead>

                  <tbody>
                    <tr v-if="operationDetails.analyses.length === 0">
                      <td class="empty-cell" colspan="5">Aucune analyse labo pour cette operation.</td>
                    </tr>

                    <tr v-for="analyse in operationDetails.analyses" :key="analyse.id">
                      <td class="strong-cell">Lot analyse</td>
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
        </template>

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
            <span class="button-plus">+</span>
            Ajouter equipement
          </button>
        </div>

        <div class="equipements-metrics">
          <article>
            <span>Total equipements</span>
            <strong>{{ equipements.length }}</strong>
          </article>

          <article>
            <span>Operationnels</span>
            <strong>{{ equipementsOperationnels }}</strong>
          </article>

          <article>
            <span>En panne</span>
            <strong>{{ equipementsEnPanne }}</strong>
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
                  <th>Statut</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                <tr v-if="equipements.length === 0">
                  <td class="empty-cell" colspan="5">
                    Aucun equipement trouve pour cet atelier.
                  </td>
                </tr>

                <tr
                  v-for="equipement in equipements"
                  :key="equipement.id"
                  :class="{ 'equipement-row-alert': isEquipementEnPanne(equipement) }"
                >
                  <td>
                    <span class="equipement-code">
                      {{ equipement.tagIndustriel }}
                    </span>
                  </td>

                  <td>
                    <div class="equipement-name">
                      <strong>{{ equipement.nomEquipement }}</strong>
                      <small>{{ getEquipementStatutLabel(equipement) }}</small>
                    </div>
                  </td>

                  <td>
                    <span class="type-pill">{{ equipement.typeEquipement }}</span>
                  </td>

                  <td>
                    <span
                      class="equipement-status-pill"
                      :class="{ 'is-panne': isEquipementEnPanne(equipement) }"
                    >
                      {{ getEquipementStatutLabel(equipement) }}
                    </span>
                  </td>

                  <td class="actions">
                    <button
                      class="action-button status-action-button"
                      :class="{ 'danger-button': !isEquipementEnPanne(equipement), 'restore-button': isEquipementEnPanne(equipement) }"
                      type="button"
                      @click="toggleEquipementPanne(equipement)"
                    >
                      {{ isEquipementEnPanne(equipement) ? 'Operationnel' : 'En panne' }}
                    </button>
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

      <section v-if="activeTab === 'labo'" class="panel crud-page labo-panel">
        <div class="page-hero page-hero-labo">
          <div class="page-hero-text">
            <p class="eyebrow">Laboratoire</p>
            <h2>Techniciens labo</h2>
            <p>
              Gestion des techniciens affectes a {{ atelierName }}.
              Chaque technicien garde un acces simple pour le suivi labo.
            </p>
          </div>

          <div class="page-hero-actions">
            <button class="primary-button no-margin" type="button" @click="openCreateTechnicien">
              Ajouter technicien
            </button>
          </div>
        </div>

        <div class="labo-metrics crud-metrics page-metrics">
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

        <article class="table-card labo-table-card modern-table-card">
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
                    <div class="person-cell">
                      <span class="person-avatar">
                        {{ technicien.username ? technicien.username.charAt(0).toUpperCase() : 'T' }}
                      </span>
                      <div class="technicien-name">
                        <strong>{{ technicien.username }}</strong>
                        <small>Technicien atelier</small>
                      </div>
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

      <section v-if="activeTab === 'articles'" class="panel crud-page articles-panel">
        <div class="page-hero page-hero-articles">
          <div class="page-hero-text">
            <p class="eyebrow">Matiere</p>
            <h2>Articles matiere</h2>
            <p>
              Referentiel des matieres utilisees dans les flux d'entree,
              les flux de sortie et les lots de production.
            </p>
          </div>

          <div class="page-hero-actions">
            <button class="primary-button no-margin" type="button" @click="openCreateArticle">
              Ajouter article
            </button>
          </div>
        </div>

        <div class="labo-metrics crud-metrics page-metrics">
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

        <article class="table-card labo-table-card modern-table-card">
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
                    <div class="material-cell">
                      <span class="material-dot"></span>
                      <div class="technicien-name">
                        <strong>{{ article.nomArticle }}</strong>
                        <small>{{ article.categorie }}</small>
                      </div>
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

      <section v-if="activeTab === 'operations'" class="panel crud-page operations-panel">
        <div class="page-hero page-hero-operations">
          <div class="page-hero-text">
            <p class="eyebrow">Process</p>
            <h2>Operations process</h2>
            <p>
              Creation, suivi et cloture des operations de fabrication de
              {{ atelierName }} avec les flux matiere.
            </p>
          </div>

          <div class="page-hero-actions">
            <button class="primary-button no-margin" type="button" @click="openCreateOperation">
              Ajouter operation
            </button>
          </div>
        </div>

        <div class="labo-metrics crud-metrics page-metrics">
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

        <article class="table-card labo-table-card modern-table-card">
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
                  <td>
                    <div class="operation-code-cell">
                      <strong>{{ operation.numOrdreFab }}</strong>
                      <small>Operation process</small>
                    </div>
                  </td>
                  <td>{{ operation.typeOperation }}</td>
                  <td>{{ operation.equipementName || 'Non precise' }}</td>
                  <td>{{ operation.dateDebut }}</td>
                  <td>{{ operation.dateFin || 'Non terminee' }}</td>
                  <td><span class="mini-pill operation-status-mini">{{ operation.statutOperation }}</span></td>
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

      <section v-if="activeTab === 'lots'" class="panel crud-page lots-panel">
        <div class="page-hero page-hero-lots">
          <div class="page-hero-text">
            <p class="eyebrow">Production</p>
            <h2>Lots de production</h2>
            <p>
              Suivi qualite des lots generes automatiquement apres la sortie
              operation. Le chef atelier peut marquer chaque lot conforme ou non conforme.
            </p>
          </div>

          <div class="page-hero-actions">
            <button class="soft-button no-margin" type="button" @click="loadLots">
              Actualiser
            </button>
          </div>
        </div>

        <div class="labo-metrics crud-metrics page-metrics lot-metrics">
          <article>
            <span>Total lots</span>
            <strong>{{ lots.length }}</strong>
          </article>

          <article>
            <span>Conformes</span>
            <strong>{{ lotsConformes }}</strong>
          </article>

          <article>
            <span>Non conformes</span>
            <strong>{{ lotsNonConformes }}</strong>
          </article>

          <article>
            <span>Taux conformite</span>
            <strong>{{ tauxLotsConformes }}</strong>
          </article>
        </div>

        <article class="table-card labo-table-card modern-table-card lots-table-card">
          <div class="section-title">
            <div>
              <p class="eyebrow">Suivi production</p>
              <h2>Controle qualite des lots</h2>
            </div>
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

                <tr
                  v-for="lot in lots"
                  :key="lot.id"
                  :class="{ 'lot-row-alert': lot.statutQualite === 'NON_CONFORME' }"
                >
                  <td>
                    <div class="lot-code-cell">
                      <strong>Lot production</strong>
                      <small>{{ lot.statutQualite === 'CONFORME' ? 'Controle OK' : 'A verifier' }}</small>
                    </div>
                  </td>
                  <td>
                    <div class="operation-code-cell">
                      <strong>{{ lot.numOrdreFab }}</strong>
                      <small>Operation source</small>
                    </div>
                  </td>
                  <td>
                    <div class="material-cell">
                      <span class="material-dot"></span>
                      <div class="technicien-name">
                        <strong>{{ lot.nomArticle }}</strong>
                        <small>Article produit</small>
                      </div>
                    </div>
                  </td>
                  <td>{{ lot.date }}</td>
                  <td>
                    <div
                      class="lot-status-control"
                      :class="{
                        'is-conforme': lot.statutQualite === 'CONFORME',
                        'is-non-conforme': lot.statutQualite === 'NON_CONFORME'
                      }"
                    >
                      <div class="lot-status-buttons">
                        <button
                          class="lot-status-button conforme"
                          :class="{ active: lot.statutQualite === 'CONFORME' }"
                          :disabled="lotStatusSavingId === lot.id"
                          type="button"
                          @click="changeLotStatus(lot, 'CONFORME')"
                        >
                          Conforme
                        </button>
                        <button
                          class="lot-status-button non-conforme"
                          :class="{ active: lot.statutQualite === 'NON_CONFORME' }"
                          :disabled="lotStatusSavingId === lot.id"
                          type="button"
                          @click="changeLotStatus(lot, 'NON_CONFORME')"
                        >
                          Non conforme
                        </button>
                      </div>
                      <small>
                        {{ lotStatusSavingId === lot.id ? 'Sauvegarde...' : 'Statut qualite' }}
                      </small>
                    </div>
                  </td>
                  <td class="actions">
                    <button
                      class="action-button"
                      type="button"
                      @click="openLotAnalyses(lot)"
                    >
                      Voir analyses labo
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>
      </section>

      <div v-if="showLotAnalysesModal" class="modal-backdrop">
        <div class="modal-card lot-analyses-modal">
          <div class="modal-header">
            <div>
              <p class="eyebrow">Analyses laboratoire</p>
              <h2>
                Analyses du lot
              </h2>
              <p v-if="selectedLotForAnalyses">
                {{ selectedLotForAnalyses.numOrdreFab }} -
                {{ selectedLotForAnalyses.nomArticle }}
              </p>
            </div>
            <button class="modal-close" type="button" @click="closeLotAnalysesModal">
              Fermer
            </button>
          </div>

          <div class="lot-analyses-body">
            <p v-if="lotAnalysesLoading" class="empty-details">
              Chargement des analyses labo...
            </p>

            <div v-else class="table-wrapper detail-table-wrapper">
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
                  <tr v-if="selectedLotAnalyses.length === 0">
                    <td class="empty-cell" colspan="5">
                      Aucune analyse labo trouvee pour ce lot.
                    </td>
                  </tr>

                  <tr v-for="analyse in selectedLotAnalyses" :key="analyse.id">
                    <td class="strong-cell">Lot analyse</td>
                    <td>{{ analyse.tauxP2O5 }}</td>
                    <td>{{ analyse.tauxCadmiumPpm }}</td>
                    <td>{{ analyse.solidesSuspendu }}</td>
                    <td>{{ analyse.dateAnalyse }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
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

.form-section small {
  display: inline-flex;
  margin-top: 8px;
  padding: 5px 9px;
  color: var(--green-dark);
  font-size: 12px;
  font-weight: 850;
  background: var(--green-light);
  border: 1px solid #b9e4ca;
  border-radius: 999px;
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

.topbar-button {
  min-height: 40px;
}

.chef-role-pill {
  cursor: default;
}

.button-plus {
  display: grid;
  width: 22px;
  height: 22px;
  margin-right: 7px;
  place-items: center;
  color: var(--green);
  font-size: 15px;
  font-weight: 950;
  line-height: 1;
  background: #fff;
  border-radius: 7px;
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
  align-items: center;
  gap: 7px;
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

.dashboard-panel,
.crud-page {
  padding: 0;
  overflow: hidden;
  background:
    linear-gradient(180deg, #f7fbf8 0%, #f2f6f4 100%);
  border-color: #dce6df;
  box-shadow: 0 16px 36px rgb(16 24 40 / 5%);
}

.dashboard-welcome,
.page-hero {
  display: flex;
  padding: 26px;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  background:
    radial-gradient(circle at 85% 0%, rgb(18 183 106 / 14%), transparent 34%),
    linear-gradient(135deg, rgb(255 255 255 / 98%), rgb(236 248 242 / 96%)),
    #fff;
  border-bottom: 1px solid var(--border);
}

.dashboard-welcome h2,
.page-hero h2 {
  margin-top: 5px;
  color: var(--title);
  font-size: 28px;
  line-height: 1.15;
}

.dashboard-welcome p:last-child,
.page-hero p:last-child {
  max-width: 680px;
  margin-top: 8px;
  color: var(--text);
  font-size: 14px;
  line-height: 1.6;
}

.dashboard-date-card {
  min-width: 185px;
  padding: 14px 16px;
  text-align: right;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 14px;
  box-shadow: 0 12px 26px rgb(16 24 40 / 5%);
}

.dashboard-date-card span,
.dashboard-date-card strong {
  display: block;
}

.dashboard-date-card span {
  color: var(--text);
  font-size: 12px;
  font-weight: 850;
  text-transform: uppercase;
}

.dashboard-date-card strong {
  margin-top: 6px;
  color: var(--green-dark);
  font-size: 17px;
}

.page-hero {
  background:
    radial-gradient(circle at top right, rgb(15 122 63 / 12%), transparent 36%),
    linear-gradient(135deg, #fff, #f3faf6);
}

.page-hero-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-metrics,
.dashboard-panel .stats-grid {
  margin-top: 0;
  padding: 18px;
  background: #fff;
  border-bottom: 1px solid var(--border);
}

.lot-metrics {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.dashboard-panel .dashboard-grid {
  margin-top: 0;
  padding: 0 18px 18px;
}

.dashboard-info-card {
  min-height: 160px;
  border-radius: 16px;
  box-shadow: 0 12px 28px rgb(16 24 40 / 5%);
}

.active-operation-card .details-button {
  margin-top: 14px;
}

.dashboard-table-card {
  margin: 0 18px 18px;
  border-radius: 16px;
  box-shadow: 0 12px 28px rgb(16 24 40 / 5%);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
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
  min-height: 118px;
  padding: 16px;
  position: relative;
  overflow: hidden;
  border-radius: 14px;
  box-shadow: 0 12px 26px rgb(16 24 40 / 5%);
  transition: transform 0.16s ease, box-shadow 0.16s ease;
}

.stat-card::before {
  content: "";
  position: absolute;
  inset: 0 auto 0 0;
  width: 4px;
  background: var(--green);
}

.stat-card-danger::before {
  background: #d92d20;
}

.stat-card-danger strong {
  color: #b42318;
}

.stat-card-success::before {
  background: #12b76a;
}

.stat-card-warning::before {
  background: #f79009;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 34px rgb(16 24 40 / 8%);
}

.stat-card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.stat-card-head small {
  display: grid;
  width: 31px;
  height: 31px;
  flex: 0 0 31px;
  place-items: center;
  color: var(--green-dark);
  font-size: 12px;
  font-weight: 950;
  background: #eef9f2;
  border: 1px solid #c7ead4;
  border-radius: 9px;
}

.stat-card-danger .stat-card-head small {
  color: #b42318;
  background: #fff1f0;
  border-color: #fecdca;
}

.stat-card-warning .stat-card-head small {
  color: #b54708;
  background: #fff7ed;
  border-color: #fed7aa;
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
  margin-top: 18px;
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

.labo-metrics.lot-metrics {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.labo-metrics article {
  padding: 15px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 14px;
  box-shadow: 0 10px 24px rgb(16 24 40 / 4%);
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

.crud-page .labo-table-card {
  margin: 0 18px 18px;
}

.modern-table-card {
  overflow: hidden;
  border-radius: 16px;
  box-shadow: 0 14px 32px rgb(16 24 40 / 5%);
}

.modern-table-card .table-wrapper,
.dashboard-table-card .table-wrapper {
  border-radius: 12px;
}

.modern-table-card tbody tr,
.dashboard-table-card tbody tr {
  transition: background 0.16s ease;
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

.person-cell,
.material-cell,
.operation-code-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.person-avatar {
  display: grid;
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  place-items: center;
  color: #fff;
  font-size: 13px;
  font-weight: 900;
  background: var(--green);
  border-radius: 999px;
  box-shadow: 0 8px 18px rgb(15 122 63 / 18%);
}

.material-dot {
  width: 12px;
  height: 12px;
  flex: 0 0 12px;
  background: var(--green);
  border: 3px solid #dff3e8;
  border-radius: 999px;
}

.operation-code-cell {
  align-items: flex-start;
  flex-direction: column;
  gap: 3px;
}

.operation-code-cell strong {
  color: var(--title);
  font-size: 14px;
}

.operation-code-cell small {
  color: var(--text);
  font-size: 12px;
  font-weight: 750;
}

.operation-status-mini {
  background: #eef9f2;
}

.lots-table-card table {
  min-width: 900px;
}

.lot-code-cell {
  display: grid;
  gap: 4px;
}

.lot-code-cell strong {
  color: var(--title);
  font-size: 14px;
}

.lot-code-cell small {
  color: var(--text);
  font-size: 12px;
  font-weight: 800;
}

.lot-row-alert {
  background: #fffafa;
}

.lot-row-alert:hover {
  background: #fff5f5;
}

.lot-status-control {
  display: grid;
  width: 230px;
  gap: 5px;
}

.lot-status-buttons {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
  padding: 4px;
  background: #f2f5f3;
  border: 1px solid var(--border);
  border-radius: 999px;
}

.lot-status-button {
  min-height: 32px;
  padding: 0 10px;
  color: #667085;
  font: inherit;
  font-size: 12px;
  font-weight: 850;
  white-space: nowrap;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 999px;
  cursor: pointer;
}

.lot-status-button.conforme.active {
  color: var(--green-dark);
  background: #e8f7ee;
  border-color: #b9e4ca;
  box-shadow: 0 8px 18px rgb(15 122 63 / 12%);
}

.lot-status-button.non-conforme.active {
  color: #9f1239;
  background: #fff1f3;
  border-color: #fecdd3;
  box-shadow: 0 8px 18px rgb(159 18 57 / 10%);
}

.lot-status-button:disabled {
  opacity: 0.7;
  cursor: wait;
}

.lot-status-control small {
  color: var(--text);
  font-size: 11px;
  font-weight: 750;
}

.lot-analyses-modal {
  width: min(780px, 100%);
}

.lot-analyses-body {
  padding: 18px;
}

.lot-analyses-modal .modal-header p:last-child {
  margin-top: 5px;
  color: var(--text);
  font-size: 13px;
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
  background:
    linear-gradient(180deg, #f8fbf9 0%, #f2f6f4 100%);
  border-color: #dce6df;
  box-shadow: 0 16px 36px rgb(16 24 40 / 5%);
}

.equipements-hero {
  display: flex;
  padding: 26px;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  background:
    radial-gradient(circle at 90% 0%, rgb(15 122 63 / 14%), transparent 34%),
    linear-gradient(135deg, rgb(255 255 255 / 98%), rgb(232 247 239 / 95%)),
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
  background: #fff;
  border-bottom: 1px solid var(--border);
}

.equipements-metrics article {
  padding: 16px;
  position: relative;
  overflow: hidden;
  background:
    linear-gradient(135deg, #fff, #f8fbf9);
  border: 1px solid var(--border);
  border-radius: 14px;
  box-shadow: 0 12px 26px rgb(15 23 42 / 5%);
}

.equipements-metrics article::before {
  content: "";
  position: absolute;
  inset: 0 auto 0 0;
  width: 4px;
  background: var(--green);
}

.equipements-metrics article:last-child::before {
  background: #d92d20;
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
  overflow: hidden;
  border-radius: 16px;
  box-shadow: 0 14px 32px rgb(16 24 40 / 5%);
}

.equipement-table-wrapper table {
  min-width: 900px;
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
  box-shadow: 0 8px 18px rgb(15 122 63 / 10%);
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

.equipement-row-alert {
  background: #fffafa;
}

.equipement-row-alert:hover {
  background: #fff5f5;
}

.equipement-status-pill {
  display: inline-flex;
  width: fit-content;
  padding: 6px 10px;
  align-items: center;
  color: var(--green-dark);
  font-size: 12px;
  font-weight: 900;
  background: #e8f7ee;
  border: 1px solid #b9e4ca;
  border-radius: 999px;
}

.equipement-status-pill.is-panne {
  color: #b42318;
  background: #fff1f0;
  border-color: #fecdca;
}

.status-action-button {
  min-width: 112px;
  justify-content: center;
}

.restore-button {
  color: var(--green-dark);
  background: #eef9f2;
  border-color: #b9e4ca;
}

.restore-button:hover {
  background: #dff3e8;
}

.operation-details-panel {
  padding: 0;
  overflow: hidden;
  background:
    linear-gradient(180deg, #f7fbf8 0%, #eef4f1 100%);
  border-color: #dce6df;
  box-shadow: 0 16px 36px rgb(16 24 40 / 5%);
}

.operation-hero {
  display: flex;
  padding: 28px;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  background:
    radial-gradient(circle at 86% 8%, rgb(255 255 255 / 20%), transparent 30%),
    linear-gradient(135deg, rgb(7 88 45 / 97%), rgb(15 122 63 / 88%)),
    linear-gradient(120deg, #123d2a, #0f7a3f);
  border-bottom: 1px solid rgb(255 255 255 / 14%);
}

.operation-hero-main {
  max-width: 720px;
}

.operation-hero .eyebrow {
  color: #cbf5d9;
}

.operation-hero h2 {
  margin-top: 6px;
  color: #fff;
  font-size: 32px;
  line-height: 1.15;
}

.operation-hero p:last-child {
  margin-top: 8px;
  color: rgb(255 255 255 / 78%);
  font-size: 14px;
  line-height: 1.6;
}

.operation-hero-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.operation-status-pill {
  display: inline-flex;
  padding: 8px 12px;
  color: #eafff1;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  background: rgb(255 255 255 / 13%);
  border: 1px solid rgb(255 255 255 / 24%);
  border-radius: 999px;
}

.operation-hero .details-button {
  color: #0a4f2b;
  background: #fff;
  border-color: #fff;
}

.operation-hero .details-button:hover {
  background: #edf8f1;
}

.operation-quick-steps {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  padding: 18px;
  background: #fff;
  border-bottom: 1px solid var(--border);
}

.operation-step {
  display: flex;
  min-height: 86px;
  padding: 14px;
  align-items: flex-start;
  gap: 12px;
  background: linear-gradient(135deg, #fff, #f8fbf9);
  border: 1px solid var(--border);
  border-radius: 14px;
  box-shadow: 0 10px 24px rgb(16 24 40 / 4%);
}

.operation-step span {
  display: grid;
  width: 32px;
  height: 32px;
  flex: 0 0 32px;
  place-items: center;
  color: #667085;
  font-size: 12px;
  font-weight: 900;
  background: #eef2f0;
  border-radius: 999px;
}

.operation-step strong,
.operation-step p {
  display: block;
}

.operation-step strong {
  color: var(--title);
  font-size: 14px;
}

.operation-step p {
  margin-top: 5px;
  color: var(--text);
  font-size: 13px;
  font-weight: 750;
}

.operation-step.is-done {
  background: linear-gradient(135deg, #f0fbf4, #fff);
  border-color: #c7ead4;
}

.operation-step.is-done span {
  color: #fff;
  background: var(--green);
}

.operation-summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  padding: 18px;
  background: #fff;
  border-bottom: 1px solid var(--border);
}

.operation-summary-grid article {
  padding: 16px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #fff, #f8fbf9);
  border: 1px solid var(--border);
  border-radius: 14px;
  box-shadow: 0 10px 26px rgb(16 24 40 / 4%);
}

.operation-summary-grid article::before {
  content: "";
  position: absolute;
  inset: 0 auto 0 0;
  width: 4px;
  background: var(--green);
}

.operation-summary-grid span,
.operation-summary-grid strong {
  display: block;
}

.operation-summary-grid span {
  color: var(--text);
  font-size: 12px;
  font-weight: 850;
  text-transform: uppercase;
}

.operation-summary-grid strong {
  margin-top: 8px;
  color: var(--title);
  font-size: 17px;
  line-height: 1.35;
}

.operation-details-layout {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  padding: 18px;
}

.detail-card {
  padding: 18px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 16px;
  box-shadow: 0 14px 32px rgb(16 24 40 / 6%);
}

.detail-card-large {
  grid-column: 1 / -1;
}

.flux-split-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.flux-panel {
  padding: 14px;
  background: #f8faf9;
  border: 1px solid var(--border);
  border-radius: 14px;
}

.flux-panel:first-child {
  background: #f3fbf6;
  border-color: #c7ead4;
}

.flux-panel:last-child {
  background: #f8fafc;
}

.flux-panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.flux-panel-title strong {
  color: var(--title);
  font-size: 15px;
}

.flux-panel-title span {
  color: var(--text);
  font-size: 12px;
  font-weight: 850;
}

.detail-table-wrapper {
  background: #fff;
}

.detail-table-wrapper th {
  background: #f6faf8;
}

.detail-table-wrapper table {
  min-width: 560px;
}

.bilan-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.bilan-metric {
  padding: 15px;
  background: #f8faf9;
  border: 1px solid var(--border);
  border-radius: 14px;
}

.bilan-metric.main-metric {
  grid-column: 1 / -1;
  background: #eef9f2;
  border-color: #c7ead4;
}

.bilan-metric span,
.bilan-metric strong {
  display: block;
}

.bilan-metric span {
  color: var(--text);
  font-size: 12px;
  font-weight: 850;
  text-transform: uppercase;
}

.bilan-metric strong {
  margin-top: 8px;
  color: var(--title);
  font-size: 18px;
}

.bilan-metric.main-metric strong {
  color: var(--green-dark);
  font-size: 30px;
  line-height: 1;
}

.operation-empty-note {
  margin-top: 16px;
  padding: 18px;
  color: var(--text);
  font-weight: 750;
  line-height: 1.5;
  background: #f8faf9;
  border: 1px dashed #cbd5d0;
  border-radius: 14px;
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
  .operation-summary-grid,
  .operation-details-layout,
  .operation-quick-steps {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .labo-metrics.lot-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .flux-split-grid {
    grid-template-columns: 1fr;
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
  .operation-summary-grid,
  .operation-details-layout,
  .operation-quick-steps,
  .bilan-grid {
    grid-template-columns: 1fr;
  }

  .labo-metrics.lot-metrics {
    grid-template-columns: 1fr;
  }

  .operation-hero,
  .section-title {
    align-items: flex-start;
    flex-direction: column;
  }

  .operation-hero {
    padding: 18px;
  }

  .operation-hero h2 {
    font-size: 24px;
  }

  .operation-hero-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .operation-hero .details-button {
    width: 100%;
  }

  .operation-quick-steps,
  .operation-summary-grid,
  .operation-details-layout {
    padding-right: 14px;
    padding-left: 14px;
  }

  .dashboard-welcome,
  .page-hero {
    padding: 18px;
    align-items: flex-start;
    flex-direction: column;
  }

  .dashboard-welcome h2,
  .page-hero h2 {
    font-size: 24px;
  }

  .dashboard-date-card,
  .page-hero-actions,
  .page-hero .primary-button {
    width: 100%;
  }

  .dashboard-date-card {
    text-align: left;
  }

  .dashboard-panel .stats-grid,
  .dashboard-panel .dashboard-grid,
  .page-metrics {
    padding-right: 14px;
    padding-left: 14px;
  }

  .dashboard-table-card,
  .crud-page .labo-table-card {
    margin-right: 14px;
    margin-left: 14px;
  }

  .detail-card {
    padding: 14px;
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
