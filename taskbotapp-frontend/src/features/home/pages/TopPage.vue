<script setup lang="ts">
import { computed, ref, watch } from 'vue'

type CalendarCell = {
  key: string
  day: number
  inCurrentMonth: boolean
  isToday: boolean
  events: CalendarEvent[]
}

type CalendarEvent = {
  id: string
  title: string
  color: 'blue' | 'teal' | 'green' | 'orange' | 'purple' | 'gray'
}

type Priority = 'low' | 'medium' | 'high'
type TaskForm = {
  title: string
  startAt: string
  dueAt: string
  priority: Priority
  detail: string
  locationUrl: string
}

type TaskItem = {
  id: number
  userId: number
  title: string
  startAt: string
  dueAt: string
  priority: Priority
  status: 'todo' | 'doing' | 'done'
  detail: string | null
  locationUrl: string | null
  createdAt: string
  updatedAt: string
}

const labels = {
  todayTasks: '\u672c\u65e5\u306e\u30bf\u30b9\u30af',
  addTask: '\u30bf\u30b9\u30af\u8ffd\u52a0',
  editTask: '\u30bf\u30b9\u30af\u7de8\u96c6',
  goToday: '\u672c\u65e5\u3078',
  calendar: '\u30ab\u30ec\u30f3\u30c0\u30fc',
  aiRecommend: 'AI\u304a\u3059\u3059\u3081\u30bf\u30b9\u30af',
  stats: '\u7d71\u8a08',
  noTasks: '\u30bf\u30b9\u30af\u304c\u307e\u3060\u3042\u308a\u307e\u305b\u3093',
  aiPlaceholder: '\u3053\u3053\u306bAI\u63d0\u6848\u304c\u8868\u793a\u3055\u308c\u307e\u3059',
  statsPlaceholder: '\u5b8c\u4e86\u6570\u30fb\u672a\u5b8c\u4e86\u6570\u3092\u8868\u793a\u3057\u307e\u3059',
  taskModalTitle: '\u30bf\u30b9\u30af\u3092\u8ffd\u52a0',
  title: '\u30bf\u30b9\u30af\u540d',
  startAt: '\u30bf\u30b9\u30af\u958b\u59cb\u65e5\u6642',
  dueAt: '\u30bf\u30b9\u30af\u7de0\u5207\u65e5\u6642',
  priority: '\u512a\u5148\u5ea6',
  locationUrl: '\u958b\u50ac\u5834\u6240URL',
  detail: '\u30bf\u30b9\u30af\u8a73\u7d30',
  cancel: '\u30ad\u30e3\u30f3\u30bb\u30eb',
  save: '\u4fdd\u5b58',
  update: '\u66f4\u65b0',
  saving: '\u4fdd\u5b58\u4e2d...',
  updating: '\u66f4\u65b0\u4e2d...',
  priorityLow: '\u4f4e',
  priorityMedium: '\u4e2d',
  priorityHigh: '\u9ad8',
  locationPlaceholder: 'https://teams.microsoft.com/...',
  close: '\u9589\u3058\u308b',
  invalidUrl: 'URL\u306f https:// \u5f62\u5f0f\u3067\u5165\u529b\u3057\u3066\u304f\u3060\u3055\u3044',
  requiredTitle: '\u30bf\u30b9\u30af\u540d\u306f\u5fc5\u9808\u3067\u3059',
  requiredStart: '\u958b\u59cb\u65e5\u6642\u306f\u5fc5\u9808\u3067\u3059',
  requiredDue: '\u7de0\u5207\u65e5\u6642\u306f\u5fc5\u9808\u3067\u3059',
  invalidRange: '\u7de0\u5207\u65e5\u6642\u306f\u958b\u59cb\u65e5\u6642\u4ee5\u964d\u306b\u3057\u3066\u304f\u3060\u3055\u3044',
}

const weekHeaders = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY']
const calendarBaseDate = ref(new Date())

const monthLabel = computed(() => {
  const y = calendarBaseDate.value.getFullYear()
  const m = calendarBaseDate.value.getMonth() + 1
  return `${y}-${String(m).padStart(2, '0')}`
})

const toDateKey = (date: Date): string => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const selectedDateKey = ref(toDateKey(new Date()))
const selectedTaskTitle = computed(() => {
  const [yearStr, monthStr, dayStr] = selectedDateKey.value.split('-')
  const year = Number(yearStr)
  const month = Number(monthStr)
  const day = Number(dayStr)

  if (!Number.isFinite(year) || !Number.isFinite(month) || !Number.isFinite(day)) {
    return labels.todayTasks
  }

  const selected = new Date(year, month - 1, day)
  const now = new Date()
  const isToday =
    selected.getFullYear() === now.getFullYear() &&
    selected.getMonth() === now.getMonth() &&
    selected.getDate() === now.getDate()

  return isToday ? labels.todayTasks : `${month}\u6708${day}\u65e5\u306e\u30bf\u30b9\u30af`
})
const isTaskModalOpen = ref(false)
const editingTaskId = ref<number | null>(null)
const isSavingTask = ref(false)
const taskFormError = ref('')
const tasksByMonth = ref<Record<string, TaskItem[]>>({})
const isLoadingTasks = ref(false)
const taskLoadError = ref('')
const taskForm = ref<TaskForm>({
  title: '',
  startAt: '',
  dueAt: '',
  priority: 'medium',
  detail: '',
  locationUrl: '',
})
const isEditingTask = computed(() => editingTaskId.value !== null)
const taskModalTitleText = computed(() => (isEditingTask.value ? labels.editTask : labels.taskModalTitle))
const taskSubmitLabel = computed(() => {
  if (isSavingTask.value) {
    return isEditingTask.value ? labels.updating : labels.saving
  }
  return isEditingTask.value ? labels.update : labels.save
})

const toMonthKey = (year: number, month: number): string => `${year}-${String(month).padStart(2, '0')}`

const monthKeyFromDateKey = (dateKey: string): string => {
  const [year, month] = dateKey.split('-')
  if (!year || !month) return toMonthKey(new Date().getFullYear(), new Date().getMonth() + 1)
  return `${year}-${month}`
}

const currentMonthKey = computed(() =>
  toMonthKey(calendarBaseDate.value.getFullYear(), calendarBaseDate.value.getMonth() + 1)
)

const currentMonthTasks = computed<TaskItem[]>(() => tasksByMonth.value[currentMonthKey.value] ?? [])

const selectedTasks = computed<TaskItem[]>(() => {
  const monthKey = monthKeyFromDateKey(selectedDateKey.value)
  const tasks = tasksByMonth.value[monthKey] ?? []

  const [yearStr, monthStr, dayStr] = selectedDateKey.value.split('-')
  const year = Number(yearStr)
  const month = Number(monthStr)
  const day = Number(dayStr)
  if (!Number.isFinite(year) || !Number.isFinite(month) || !Number.isFinite(day)) return []

  const dayStart = new Date(year, month - 1, day, 0, 0, 0, 0).getTime()
  const dayEnd = new Date(year, month - 1, day, 23, 59, 59, 999).getTime()

  return tasks.filter((task) => {
    const start = new Date(task.startAt).getTime()
    const due = new Date(task.dueAt).getTime()
    return Number.isFinite(start) && Number.isFinite(due) && start <= dayEnd && due >= dayStart
  })
})

const priorityToChipColor = (priority: Priority): CalendarEvent['color'] => {
  if (priority === 'high') return 'orange'
  if (priority === 'medium') return 'blue'
  return 'green'
}

const formatCalendarEventTitle = (task: TaskItem, dateKey: string): string => {
  const taskStart = new Date(task.startAt)
  const taskStartKey = toDateKey(taskStart)
  const time = new Intl.DateTimeFormat('ja-JP', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(taskStart)

  return taskStartKey === dateKey ? `${time} ${task.title}` : task.title
}

const mockEventsByDate = computed<Record<string, CalendarEvent[]>>(() => {
  const map: Record<string, CalendarEvent[]> = {}

  currentMonthTasks.value.forEach((task) => {
    const start = new Date(task.startAt)
    const end = new Date(task.dueAt)
    const cursor = new Date(start.getFullYear(), start.getMonth(), start.getDate())
    const last = new Date(end.getFullYear(), end.getMonth(), end.getDate())

    while (cursor.getTime() <= last.getTime()) {
      const dateKey = toDateKey(cursor)
      if (!map[dateKey]) map[dateKey] = []

      map[dateKey].push({
        id: `${task.id}-${dateKey}`,
        title: formatCalendarEventTitle(task, dateKey),
        color: priorityToChipColor(task.priority),
      })

      cursor.setDate(cursor.getDate() + 1)
    }
  })

  Object.values(map).forEach((events) => {
    events.sort((a, b) => a.title.localeCompare(b.title, 'ja'))
  })

  return map
})

const formatTaskTimeRange = (task: TaskItem): string => {
  const fmt = new Intl.DateTimeFormat('ja-JP', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
  return `${fmt.format(new Date(task.startAt))} - ${fmt.format(new Date(task.dueAt))}`
}

const toDateTimeLocalValue = (isoString: string): string => {
  const date = new Date(isoString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hour}:${minute}`
}

const loadTasksForMonth = async (year: number, month: number, force = false): Promise<void> => {
  const monthKey = toMonthKey(year, month)
  if (!force && tasksByMonth.value[monthKey]) return

  isLoadingTasks.value = true
  taskLoadError.value = ''
  try {
    const response = await fetch(`http://localhost:8080/api/tasks?year=${year}&month=${month}`, {
      method: 'GET',
      credentials: 'include',
    })

    if (!response.ok) {
      if (response.status === 401) {
        taskLoadError.value = 'ログイン状態が切れています。再ログインしてください。'
      } else {
        taskLoadError.value = 'タスク取得に失敗しました。'
      }
      return
    }

    const data = (await response.json()) as TaskItem[]
    tasksByMonth.value[monthKey] = data
  } catch {
    taskLoadError.value = 'タスク取得に失敗しました。'
  } finally {
    isLoadingTasks.value = false
  }
}

const calendarCells = computed<CalendarCell[]>(() => {
  const year = calendarBaseDate.value.getFullYear()
  const month = calendarBaseDate.value.getMonth()
  const firstDay = new Date(year, month, 1)
  const startWeekday = firstDay.getDay()
  const daysInCurrentMonth = new Date(year, month + 1, 0).getDate()
  const total = startWeekday + daysInCurrentMonth
  const tail = (7 - (total % 7)) % 7
  const cellCount = total + tail
  const today = new Date()

  const cells: CalendarCell[] = []

  for (let i = 0; i < cellCount; i += 1) {
    const date = new Date(year, month, 1 - startWeekday + i)
    const key = toDateKey(date)
    cells.push({
      key,
      day: date.getDate(),
      inCurrentMonth: date.getMonth() === month,
      isToday:
        date.getFullYear() === today.getFullYear() &&
        date.getMonth() === today.getMonth() &&
        date.getDate() === today.getDate(),
      events: mockEventsByDate.value[key] ?? [],
    })
  }

  return cells
})

const weekCount = computed(() => Math.ceil(calendarCells.value.length / 7))

const moveMonth = (diff: number): void => {
  const next = new Date(calendarBaseDate.value)
  next.setDate(1)
  next.setMonth(next.getMonth() + diff)
  calendarBaseDate.value = next
}

const goToday = (): void => {
  const now = new Date()
  calendarBaseDate.value = now
  selectedDateKey.value = toDateKey(now)
}

const onClickDate = (key: string): void => {
  selectedDateKey.value = key
}

const resetTaskForm = (): void => {
  editingTaskId.value = null
  taskForm.value = {
    title: '',
    startAt: '',
    dueAt: '',
    priority: 'medium',
    detail: '',
    locationUrl: '',
  }
}

const openTaskModal = (task?: TaskItem): void => {
  if (task) {
    editingTaskId.value = task.id
    taskForm.value = {
      title: task.title,
      startAt: toDateTimeLocalValue(task.startAt),
      dueAt: toDateTimeLocalValue(task.dueAt),
      priority: task.priority,
      detail: task.detail ?? '',
      locationUrl: task.locationUrl ?? '',
    }
  } else {
    resetTaskForm()
  }

  isTaskModalOpen.value = true
  taskFormError.value = ''
}

const openNewTaskModal = (): void => {
  openTaskModal()
}

const onClickTodayTask = (task: TaskItem): void => {
  openTaskModal(task)
}

const closeTaskModal = (): void => {
  if (isSavingTask.value) return
  isTaskModalOpen.value = false
  taskFormError.value = ''
  resetTaskForm()
}

const onOverlayClick = (event: MouseEvent): void => {
  if (event.target !== event.currentTarget) return
  closeTaskModal()
}

const validateTaskForm = (): string => {
  if (!taskForm.value.title.trim()) return labels.requiredTitle
  if (!taskForm.value.startAt) return labels.requiredStart
  if (!taskForm.value.dueAt) return labels.requiredDue

  const start = new Date(taskForm.value.startAt).getTime()
  const due = new Date(taskForm.value.dueAt).getTime()
  if (Number.isFinite(start) && Number.isFinite(due) && due < start) return labels.invalidRange

  const url = taskForm.value.locationUrl.trim()
  if (url) {
    try {
      const parsed = new URL(url)
      if (parsed.protocol !== 'https:') return labels.invalidUrl
    } catch {
      return labels.invalidUrl
    }
  }

  return ''
}

const onSaveTask = async (): Promise<void> => {
  if (isSavingTask.value) return

  const errorMessage = validateTaskForm()
  if (errorMessage) {
    taskFormError.value = errorMessage
    return
  }

  isSavingTask.value = true
  taskFormError.value = ''

  try {
    const startAtIso = new Date(taskForm.value.startAt).toISOString()
    const dueAtIso = new Date(taskForm.value.dueAt).toISOString()

    const requestUrl = isEditingTask.value
      ? `http://localhost:8080/api/tasks/${editingTaskId.value}`
      : 'http://localhost:8080/api/tasks'
    const response = await fetch(requestUrl, {
      method: isEditingTask.value ? 'PUT' : 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        title: taskForm.value.title.trim(),
        startAt: startAtIso,
        dueAt: dueAtIso,
        priority: taskForm.value.priority,
        detail: taskForm.value.detail.trim() || null,
        locationUrl: taskForm.value.locationUrl.trim() || null,
      }),
    })

    if (!response.ok) {
      if (response.status === 401) {
        taskFormError.value = 'ログイン状態が切れています。再ログインしてください。'
        return
      }

      let message = '保存に失敗しました'
      try {
        const data = (await response.json()) as { message?: string }
        if (data?.message) message = data.message
      } catch {
        // no-op
      }
      taskFormError.value = message
      return
    }

    const selectedDate = new Date(selectedDateKey.value)
    const selectedYear = selectedDate.getFullYear()
    const selectedMonth = selectedDate.getMonth() + 1
    await loadTasksForMonth(selectedYear, selectedMonth, true)
    closeTaskModal()
  } catch {
    taskFormError.value = '\u4fdd\u5b58\u306b\u5931\u6557\u3057\u307e\u3057\u305f'
  } finally {
    isSavingTask.value = false
  }
}

watch(
  calendarBaseDate,
  async (value) => {
    await loadTasksForMonth(value.getFullYear(), value.getMonth() + 1)
  },
  { immediate: true }
)

watch(
  selectedDateKey,
  async (key) => {
    const [yearStr, monthStr] = key.split('-')
    const year = Number(yearStr)
    const month = Number(monthStr)
    if (!Number.isFinite(year) || !Number.isFinite(month)) return
    await loadTasksForMonth(year, month)
  },
  { immediate: true }
)
</script>

<template>
  <section class="top-page" aria-label="top-page">
    <div class="top-grid">
      <article class="panel panel-today">
          <header class="panel-head panel-head-split">
            <h2>{{ selectedTaskTitle }}</h2>
            <button type="button" class="add-button" :aria-label="labels.addTask" @click="openNewTaskModal">+</button>
          </header>
        <div class="panel-body panel-body-lines">
          <p v-if="taskLoadError" class="empty-text empty-text-faint error-text">{{ taskLoadError }}</p>
          <p v-else-if="isLoadingTasks" class="empty-text empty-text-faint">読み込み中...</p>

          <ul v-if="selectedTasks.length > 0" class="today-task-list">
            <li
              v-for="task in selectedTasks"
              :key="task.id"
              class="today-task-item"
              role="button"
              tabindex="0"
              @click="onClickTodayTask(task)"
              @keydown.enter="onClickTodayTask(task)"
              @keydown.space.prevent="onClickTodayTask(task)"
            >
              <p class="today-task-time">{{ formatTaskTimeRange(task) }}</p>
              <p class="today-task-title">{{ task.title }}</p>
            </li>
          </ul>

          <p v-else-if="!isLoadingTasks" class="empty-text empty-text-faint">{{ labels.noTasks }}</p>
        </div>
      </article>

      <article class="panel panel-calendar">
        <header class="panel-head panel-head-between">
          <h2>{{ labels.calendar }}</h2>
          <div class="month-nav">
            <button type="button" class="today-button" @click="goToday">{{ labels.goToday }}</button>
            <button type="button" class="month-button" @click="moveMonth(-1)">&larr;</button>
            <strong>{{ monthLabel }}</strong>
            <button type="button" class="month-button" @click="moveMonth(1)">&rarr;</button>
          </div>
        </header>

        <div class="panel-body panel-body-tall month-view">
          <div class="week-header">
            <span v-for="label in weekHeaders" :key="label">{{ label }}</span>
          </div>

          <div class="month-grid" :style="{ gridTemplateRows: `repeat(${weekCount}, minmax(84px, 1fr))` }">
            <div
              v-for="cell in calendarCells"
              :key="cell.key"
              class="day-cell"
              :class="{ 'is-outside': !cell.inCurrentMonth, 'is-selected': selectedDateKey === cell.key }"
              role="button"
              tabindex="0"
              @click="onClickDate(cell.key)"
              @keydown.enter="onClickDate(cell.key)"
              @keydown.space.prevent="onClickDate(cell.key)"
            >
              <div class="day-number" :class="{ 'is-today': cell.isToday }">{{ cell.day }}</div>

              <div class="event-stack">
                <div
                  v-for="event in cell.events.slice(0, 2)"
                  :key="event.id"
                  class="event-chip"
                  :class="`chip-${event.color}`"
                >
                  {{ event.title }}
                </div>
                <div v-if="cell.events.length > 2" class="event-more">+{{ cell.events.length - 2 }}</div>
              </div>
            </div>
          </div>
        </div>
      </article>

      <article class="panel panel-ai">
        <header class="panel-head">
          <h2>{{ labels.aiRecommend }}</h2>
        </header>
        <div class="panel-body panel-body-ai">
          <p class="empty-text">{{ labels.aiPlaceholder }}</p>
        </div>
      </article>

      <article class="panel panel-stats">
        <header class="panel-head">
          <h2>{{ labels.stats }}</h2>
        </header>
        <div class="panel-body panel-body-stats">
          <p class="empty-text">{{ labels.statsPlaceholder }}</p>
        </div>
      </article>
    </div>

    <div v-if="isTaskModalOpen" class="modal-overlay" @click="onOverlayClick">
      <section class="task-modal" role="dialog" aria-modal="true" :aria-label="taskModalTitleText">
        <header class="task-modal-head">
          <h3>{{ taskModalTitleText }}</h3>
          <button type="button" class="close-button" :aria-label="labels.close" @click="closeTaskModal">×</button>
        </header>

        <form class="task-form" @submit.prevent="onSaveTask">
          <label class="task-row">
            <span>{{ labels.title }}</span>
            <input v-model.trim="taskForm.title" type="text" maxlength="100" required />
          </label>

          <label class="task-row">
            <span>{{ labels.startAt }}</span>
            <input v-model="taskForm.startAt" type="datetime-local" required />
          </label>

          <label class="task-row">
            <span>{{ labels.dueAt }}</span>
            <input v-model="taskForm.dueAt" type="datetime-local" required />
          </label>

          <label class="task-row">
            <span>{{ labels.priority }}</span>
            <select v-model="taskForm.priority">
              <option value="low">{{ labels.priorityLow }}</option>
              <option value="medium">{{ labels.priorityMedium }}</option>
              <option value="high">{{ labels.priorityHigh }}</option>
            </select>
          </label>

          <label class="task-row">
            <span>{{ labels.locationUrl }}</span>
            <input
              v-model.trim="taskForm.locationUrl"
              type="url"
              inputmode="url"
              :placeholder="labels.locationPlaceholder"
            />
          </label>

          <label class="task-row task-row-detail">
            <span>{{ labels.detail }}</span>
            <textarea v-model="taskForm.detail" maxlength="2000" />
          </label>

          <p v-if="taskFormError" class="form-error">{{ taskFormError }}</p>

          <footer class="task-actions">
            <button type="button" class="button-secondary" :disabled="isSavingTask" @click="closeTaskModal">
              {{ labels.cancel }}
            </button>
            <button type="submit" class="button-primary" :disabled="isSavingTask">
              {{ taskSubmitLabel }}
            </button>
          </footer>
        </form>
      </section>
    </div>
  </section>
</template>

<style scoped>
.top-page {
  min-height: calc(100vh - 120px);
  width: min(1240px, 100%);
  margin: 0 auto;
  padding: 12px 24px 24px;
}

.top-grid {
  display: grid;
  grid-template-columns: minmax(360px, 1fr) minmax(540px, 1.12fr);
  grid-template-areas:
    'today calendar'
    'ai stats';
  gap: 16px 22px;
  align-items: start;
}

.panel {
  background: rgba(246, 248, 252, 0.86);
  border: 1px solid rgba(77, 74, 68, 0.22);
  border-radius: 8px;
  overflow: hidden;
}

.panel-today {
  grid-area: today;
}

.panel-calendar {
  grid-area: calendar;
}

.panel-ai {
  grid-area: ai;
}

.panel-stats {
  grid-area: stats;
}

.panel-head {
  min-height: 40px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  background: #bcc39b;
}

.panel-head h2 {
  margin: 0;
  font-size: 20px;
  color: #2f2c28;
  font-weight: 700;
}

.panel-head-split {
  justify-content: space-between;
}

.panel-head-between {
  justify-content: space-between;
}

.add-button {
  width: 40px;
  height: 28px;
  border: none;
  background: #2f97ee;
  color: #fff;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  border-radius: 6px;
}

.month-nav {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #2f2c28;
  font-size: 16px;
}

.today-button {
  height: 26px;
  border: none;
  border-radius: 6px;
  background: #2f97ee;
  color: #fff;
  font: inherit;
  font-size: 12px;
  padding: 0 8px;
  cursor: pointer;
}

.month-button {
  border: none;
  background: transparent;
  font: inherit;
  color: inherit;
  cursor: pointer;
  line-height: 1;
  padding: 0 2px;
}

.panel-body {
  border-top: 1px solid rgba(77, 74, 68, 0.18);
  background: transparent;
}

.panel-body-lines {
  min-height: 336px;
  background-image: linear-gradient(
    to bottom,
    transparent 47px,
    rgba(77, 74, 68, 0.3) 47px,
    rgba(77, 74, 68, 0.3) 48px,
    transparent 48px
  );
  background-size: 100% 48px;
  position: relative;
  overflow: hidden;
}

.panel-body-tall {
  height: 560px;
}

.month-view {
  display: grid;
  grid-template-rows: auto 1fr;
  padding: 0;
}

.week-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  border-bottom: 1px solid rgba(77, 74, 68, 0.25);
}

.week-header span {
  min-height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8a8d92;
  font-size: 12px;
  letter-spacing: 0.2px;
}

.month-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  height: 100%;
}

.day-cell {
  border-right: 1px solid rgba(77, 74, 68, 0.2);
  border-bottom: 1px solid rgba(77, 74, 68, 0.2);
  padding: 8px 4px 4px;
  background: rgba(255, 255, 255, 0.28);
  cursor: pointer;
  transition: background-color 0.15s ease, box-shadow 0.15s ease;
}

.day-cell:hover {
  background: rgba(255, 255, 255, 0.46);
}

.day-cell.is-selected {
  background: rgba(47, 151, 238, 0.12);
  box-shadow: inset 0 0 0 2px rgba(47, 151, 238, 0.45);
}

.day-cell:nth-child(7n) {
  border-right: none;
}

.day-cell.is-outside {
  background: rgba(230, 230, 230, 0.38);
}

.day-number {
  text-align: center;
  font-size: 15px;
  color: #333;
  margin-bottom: 2px;
  line-height: 1.1;
}

.day-number.is-today {
  color: #1f6aa6;
  font-weight: 700;
}

.event-stack {
  display: grid;
  gap: 3px;
}

.event-chip {
  border-radius: 6px;
  min-height: 18px;
  display: flex;
  align-items: center;
  padding: 0 5px;
  color: #fff;
  font-size: 11px;
  line-height: 1;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.chip-blue {
  background: #4256b5;
}

.chip-teal {
  background: #19b3c8;
}

.chip-green {
  background: #49af55;
}

.chip-orange {
  background: #f39a00;
}

.chip-purple {
  background: #6a43b8;
}

.chip-gray {
  background: #7a7a7a;
}

.event-more {
  font-size: 11px;
  color: #2f2c28;
  font-weight: 700;
  padding-left: 2px;
  line-height: 1;
}

.panel-body-ai {
  min-height: 280px;
}

.panel-body-stats {
  min-height: 280px;
}

.empty-text {
  margin: 0;
  padding: 14px;
  color: #6b675f;
  font-size: 13px;
}

.empty-text-faint {
  position: absolute;
  right: 10px;
  bottom: 8px;
  padding: 0;
  font-size: 12px;
  color: #9a9489;
}

.error-text {
  color: #b91c1c;
}

.today-task-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 0;
}

.today-task-item {
  height: 48px;
  display: grid;
  grid-template-columns: 170px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  padding: 0 12px;
  box-sizing: border-box;
  border-bottom: 1px solid rgba(77, 74, 68, 0.18);
  background: rgba(255, 255, 255, 0.28);
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.today-task-item:hover {
  background: rgba(47, 151, 238, 0.12);
}

.today-task-title {
  margin: 0;
  font-size: 14px;
  color: #2f2c28;
  font-weight: 600;
  text-align: left;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.today-task-time {
  margin: 0;
  font-size: 12px;
  color: #6b675f;
  white-space: nowrap;
  text-align: left;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 16px;
  background: rgba(0, 0, 0, 0.35);
  z-index: 50;
}

.task-modal {
  width: min(760px, 92vw);
  max-height: 88vh;
  overflow-y: auto;
  background: #f6f7f9;
  border: 1px solid rgba(47, 44, 40, 0.28);
  border-radius: 12px;
}

.task-modal-head {
  min-height: 56px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(47, 44, 40, 0.2);
}

.task-modal-head h3 {
  margin: 0;
  font-size: 24px;
  color: #2f2c28;
}

.close-button {
  width: 32px;
  height: 32px;
  border: 1px solid rgba(47, 44, 40, 0.3);
  border-radius: 8px;
  background: #fff;
  color: #2f2c28;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
}

.task-form {
  padding: 16px 18px 18px;
  display: grid;
  gap: 12px;
}

.task-row {
  display: grid;
  grid-template-columns: 160px 1fr;
  gap: 12px;
  align-items: center;
}

.task-row span {
  color: #2f2c28;
  font-size: 14px;
}

.task-row input,
.task-row select,
.task-row textarea {
  width: 100%;
  border: 1px solid rgba(47, 44, 40, 0.3);
  border-radius: 8px;
  background: #fff;
  font: inherit;
  color: #2f2c28;
  padding: 8px 10px;
}

.task-row-detail {
  align-items: start;
}

.task-row textarea {
  min-height: 120px;
  resize: vertical;
}

.form-error {
  margin: 0;
  color: #b91c1c;
  font-size: 13px;
}

.task-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 6px;
}

.button-secondary,
.button-primary {
  min-width: 110px;
  height: 38px;
  border-radius: 8px;
  font: inherit;
  cursor: pointer;
}

.button-secondary {
  border: 1px solid rgba(47, 44, 40, 0.4);
  background: #fff;
  color: #2f2c28;
}

.button-primary {
  border: none;
  background: #2f97ee;
  color: #fff;
}

.button-secondary:disabled,
.button-primary:disabled {
  opacity: 0.7;
  cursor: wait;
}

@media (max-width: 1200px) {
  .top-grid {
    grid-template-columns: 1fr;
    grid-template-areas:
      'today'
      'calendar'
      'ai'
      'stats';
  }

  .panel-body-tall {
    height: 500px;
  }
}

@media (max-width: 640px) {
  .top-page {
    padding: 8px 12px 16px;
  }

  .panel-head h2 {
    font-size: 18px;
  }

  .month-nav {
    font-size: 16px;
  }

  .day-number {
    font-size: 14px;
  }

  .event-chip {
    font-size: 10px;
  }

  .task-row {
    grid-template-columns: 1fr;
    gap: 6px;
  }
}
</style>
