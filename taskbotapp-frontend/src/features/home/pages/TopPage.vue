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
type TaskStatus = 'todo' | 'doing' | 'done'
type TaskFilter = 'all' | 'open' | 'done'
type TaskForm = {
  title: string
  startDate: string
  startTime: string
  dueDate: string
  dueTime: string
  priority: Priority
  status: TaskStatus
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
  status: TaskStatus
  detail: string | null
  locationUrl: string | null
  createdAt: string
  updatedAt: string
}

const labels = {
  todayTasks: '\u672c\u65e5\u306e\u30bf\u30b9\u30af',
  activeTasks: '\u671f\u9593\u5185\u30bf\u30b9\u30af',
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
  status: '\u30b9\u30c6\u30fc\u30bf\u30b9',
  locationUrl: '\u958b\u50ac\u5834\u6240URL',
  detail: '\u30bf\u30b9\u30af\u8a73\u7d30',
  cancel: '\u30ad\u30e3\u30f3\u30bb\u30eb',
  delete: '\u524a\u9664',
  save: '\u4fdd\u5b58',
  update: '\u66f4\u65b0',
  saving: '\u4fdd\u5b58\u4e2d...',
  updating: '\u66f4\u65b0\u4e2d...',
  deleting: '\u524a\u9664\u4e2d...',
  saveSuccess: '\u4fdd\u5b58\u3067\u304d\u307e\u3057\u305f',
  deleteSuccess: '\u524a\u9664\u3067\u304d\u307e\u3057\u305f',
  priorityLow: '\u4f4e',
  priorityMedium: '\u4e2d',
  priorityHigh: '\u9ad8',
  statusTodo: '\u672a\u7740\u624b',
  statusDoing: '\u9032\u884c\u4e2d',
  statusDone: '\u5b8c\u4e86',
  filterAll: '\u3059\u3079\u3066',
  filterOpen: '\u672a\u5b8c\u4e86',
  filterDone: '\u5b8c\u4e86',
  locationPlaceholder: 'https://teams.microsoft.com/...',
  close: '\u9589\u3058\u308b',
  invalidUrl: 'URL\u306f https:// \u5f62\u5f0f\u3067\u5165\u529b\u3057\u3066\u304f\u3060\u3055\u3044',
  requiredTitle: '\u30bf\u30b9\u30af\u540d\u306f\u5fc5\u9808\u3067\u3059',
  requiredStart: '\u958b\u59cb\u65e5\u6642\u306f\u5fc5\u9808\u3067\u3059',
  requiredDue: '\u7de0\u5207\u65e5\u6642\u306f\u5fc5\u9808\u3067\u3059',
  invalidRange: '\u7de0\u5207\u65e5\u6642\u306f\u958b\u59cb\u65e5\u6642\u4ee5\u964d\u306b\u3057\u3066\u304f\u3060\u3055\u3044',
  deleteConfirm: '\u3053\u306e\u30bf\u30b9\u30af\u3092\u524a\u9664\u3057\u307e\u3059\u304b\uff1f',
  deleteFailed: '\u524a\u9664\u306b\u5931\u6557\u3057\u307e\u3057\u305f',
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

  return isToday ? labels.todayTasks : `${month}\u6708${day}\u65e5\u306e\u5f53\u65e5\u30bf\u30b9\u30af`
})
const isTaskModalOpen = ref(false)
const editingTaskId = ref<number | null>(null)
const isSavingTask = ref(false)
const isDeletingTask = ref(false)
const taskFormError = ref('')
const successNotice = ref('')
const tasksByMonth = ref<Record<string, TaskItem[]>>({})
const isLoadingTasks = ref(false)
const taskLoadError = ref('')
const selectedTaskFilter = ref<TaskFilter>('open')
const activeTaskFilter = ref<TaskFilter>('open')
const taskForm = ref<TaskForm>({
  title: '',
  startDate: '',
  startTime: '',
  dueDate: '',
  dueTime: '',
  priority: 'medium',
  status: 'todo',
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
const taskDeleteLabel = computed(() => (isDeletingTask.value ? labels.deleting : labels.delete))

let successNoticeTimer: ReturnType<typeof setTimeout> | null = null

const toMonthKey = (year: number, month: number): string => `${year}-${String(month).padStart(2, '0')}`

const monthKeyFromDateKey = (dateKey: string): string => {
  const [year, month] = dateKey.split('-')
  if (!year || !month) return toMonthKey(new Date().getFullYear(), new Date().getMonth() + 1)
  return `${year}-${month}`
}

const currentMonthKey = computed(() =>
  toMonthKey(calendarBaseDate.value.getFullYear(), calendarBaseDate.value.getMonth() + 1)
)
const todayMonthKey = computed(() => {
  const now = new Date()
  return toMonthKey(now.getFullYear(), now.getMonth() + 1)
})

const currentMonthTasks = computed<TaskItem[]>(() => tasksByMonth.value[currentMonthKey.value] ?? [])

const filterTasksByStatus = (tasks: TaskItem[], filter: TaskFilter): TaskItem[] => {
  if (filter === 'done') {
    return tasks.filter((task) => task.status === 'done')
  }
  if (filter === 'open') {
    return tasks.filter((task) => task.status !== 'done')
  }
  return tasks
}

const selectedDateTasks = computed<TaskItem[]>(() => {
  const monthKey = monthKeyFromDateKey(selectedDateKey.value)
  const tasks = tasksByMonth.value[monthKey] ?? []

  return tasks.filter((task) => {
    const startDateKey = toDateKey(new Date(task.startAt))
    const dueDateKey = toDateKey(new Date(task.dueAt))
    return startDateKey === selectedDateKey.value && dueDateKey === selectedDateKey.value
  })
})

const selectedTasks = computed<TaskItem[]>(() => filterTasksByStatus(selectedDateTasks.value, selectedTaskFilter.value))

const activePeriodTasks = computed<TaskItem[]>(() => {
  const selectedDate = new Date(selectedDateKey.value)
  const selectedStart = new Date(
    selectedDate.getFullYear(),
    selectedDate.getMonth(),
    selectedDate.getDate()
  ).getTime()
  const selectedEnd = new Date(
    selectedDate.getFullYear(),
    selectedDate.getMonth(),
    selectedDate.getDate() + 1
  ).getTime() - 1

  return (tasksByMonth.value[monthKeyFromDateKey(selectedDateKey.value)] ?? []).filter((task) => {
    const start = new Date(task.startAt).getTime()
    const due = new Date(task.dueAt).getTime()
    const startDateKey = toDateKey(new Date(task.startAt))
    const dueDateKey = toDateKey(new Date(task.dueAt))
    const isSingleDaySelectedTask =
      startDateKey === selectedDateKey.value && dueDateKey === selectedDateKey.value

    return (
      Number.isFinite(start) &&
      Number.isFinite(due) &&
      start <= selectedEnd &&
      due >= selectedStart &&
      !isSingleDaySelectedTask
    )
  })
})

const activeTasks = computed<TaskItem[]>(() => filterTasksByStatus(activePeriodTasks.value, activeTaskFilter.value))

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

const toDateValue = (isoString: string): string => toDateTimeLocalValue(isoString).slice(0, 10)

const toTimeValue = (isoString: string): string => toDateTimeLocalValue(isoString).slice(11, 16)

const composeIsoString = (dateValue: string, timeValue: string): string => {
  return new Date(`${dateValue}T${timeValue}`).toISOString()
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
    startDate: selectedDateKey.value,
    startTime: '',
    dueDate: selectedDateKey.value,
    dueTime: '',
    priority: 'medium',
    status: 'todo',
    detail: '',
    locationUrl: '',
  }
}

const openTaskModal = (task?: TaskItem): void => {
  if (task) {
    editingTaskId.value = task.id
    taskForm.value = {
      title: task.title,
      startDate: toDateValue(task.startAt),
      startTime: toTimeValue(task.startAt),
      dueDate: toDateValue(task.dueAt),
      dueTime: toTimeValue(task.dueAt),
      priority: task.priority,
      status: task.status,
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

const showSuccessNotice = (message: string): void => {
  successNotice.value = message

  if (successNoticeTimer) {
    clearTimeout(successNoticeTimer)
  }

  successNoticeTimer = setTimeout(() => {
    successNotice.value = ''
    successNoticeTimer = null
  }, 2200)
}

const closeTaskModal = (): void => {
  if (isSavingTask.value || isDeletingTask.value) return
  isTaskModalOpen.value = false
  taskFormError.value = ''
  resetTaskForm()
}

const forceCloseTaskModal = (): void => {
  isTaskModalOpen.value = false
  taskFormError.value = ''
  resetTaskForm()
}

const closeTaskModalFromUi = (): void => {
  closeTaskModal()
}

const onOverlayClick = (event: MouseEvent): void => {
  if (event.target !== event.currentTarget) return
  closeTaskModalFromUi()
}

const validateTaskForm = (): string => {
  if (!taskForm.value.title.trim()) return labels.requiredTitle
  if (!taskForm.value.startDate || !taskForm.value.startTime) return labels.requiredStart
  if (!taskForm.value.dueDate || !taskForm.value.dueTime) return labels.requiredDue

  const start = new Date(`${taskForm.value.startDate}T${taskForm.value.startTime}`).getTime()
  const due = new Date(`${taskForm.value.dueDate}T${taskForm.value.dueTime}`).getTime()
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
  if (isSavingTask.value || isDeletingTask.value) return

  const errorMessage = validateTaskForm()
  if (errorMessage) {
    taskFormError.value = errorMessage
    return
  }

  isSavingTask.value = true
  taskFormError.value = ''

  try {
    const startAtIso = composeIsoString(taskForm.value.startDate, taskForm.value.startTime)
    const dueAtIso = composeIsoString(taskForm.value.dueDate, taskForm.value.dueTime)

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
        status: taskForm.value.status,
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
    forceCloseTaskModal()
    showSuccessNotice(labels.saveSuccess)
  } catch {
    taskFormError.value = '\u4fdd\u5b58\u306b\u5931\u6557\u3057\u307e\u3057\u305f'
  } finally {
    isSavingTask.value = false
  }
}

const onDeleteTask = async (): Promise<void> => {
  if (!isEditingTask.value || editingTaskId.value === null) return
  if (isSavingTask.value || isDeletingTask.value) return
  if (!window.confirm(labels.deleteConfirm)) return

  isDeletingTask.value = true
  taskFormError.value = ''

  try {
    const response = await fetch(`http://localhost:8080/api/tasks/${editingTaskId.value}`, {
      method: 'DELETE',
      credentials: 'include',
    })

    if (!response.ok) {
      if (response.status === 401) {
        taskFormError.value = 'ログイン状態を確認してください'
        return
      }
      taskFormError.value = labels.deleteFailed
      return
    }

    const selectedDate = new Date(selectedDateKey.value)
    const selectedYear = selectedDate.getFullYear()
    const selectedMonth = selectedDate.getMonth() + 1
    const today = new Date()
    await loadTasksForMonth(selectedYear, selectedMonth, true)
    await loadTasksForMonth(today.getFullYear(), today.getMonth() + 1, true)
    forceCloseTaskModal()
    showSuccessNotice(labels.deleteSuccess)
  } catch {
    taskFormError.value = labels.deleteFailed
  } finally {
    isDeletingTask.value = false
  }
}

watch(
  calendarBaseDate,
  async (value) => {
    await loadTasksForMonth(value.getFullYear(), value.getMonth() + 1)
    const today = new Date()
    await loadTasksForMonth(today.getFullYear(), today.getMonth() + 1)
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
    <transition name="toast-fade">
      <div v-if="successNotice" class="success-toast" role="status" aria-live="polite">
        {{ successNotice }}
      </div>
    </transition>

    <div class="top-grid">
      <article class="panel panel-today">
          <header class="panel-head panel-head-split">
            <h2>{{ selectedTaskTitle }}</h2>
            <button type="button" class="add-button" :aria-label="labels.addTask" @click="openNewTaskModal">+</button>
          </header>
        <div class="panel-body panel-body-lines">
          <div class="task-filter-bar" aria-label="today-task-filter">
            <button
              type="button"
              class="task-filter-button"
              :class="{ 'is-active': selectedTaskFilter === 'open' }"
              @click="selectedTaskFilter = 'open'"
            >
              {{ labels.filterOpen }}
            </button>
            <button
              type="button"
              class="task-filter-button"
              :class="{ 'is-active': selectedTaskFilter === 'done' }"
              @click="selectedTaskFilter = 'done'"
            >
              {{ labels.filterDone }}
            </button>
            <button
              type="button"
              class="task-filter-button"
              :class="{ 'is-active': selectedTaskFilter === 'all' }"
              @click="selectedTaskFilter = 'all'"
            >
              {{ labels.filterAll }}
            </button>
          </div>
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

      <article class="panel panel-active">
        <header class="panel-head">
          <h2>{{ labels.activeTasks }}</h2>
        </header>
        <div class="panel-body panel-body-active">
          <div class="task-filter-bar" aria-label="active-task-filter">
            <button
              type="button"
              class="task-filter-button"
              :class="{ 'is-active': activeTaskFilter === 'open' }"
              @click="activeTaskFilter = 'open'"
            >
              {{ labels.filterOpen }}
            </button>
            <button
              type="button"
              class="task-filter-button"
              :class="{ 'is-active': activeTaskFilter === 'done' }"
              @click="activeTaskFilter = 'done'"
            >
              {{ labels.filterDone }}
            </button>
            <button
              type="button"
              class="task-filter-button"
              :class="{ 'is-active': activeTaskFilter === 'all' }"
              @click="activeTaskFilter = 'all'"
            >
              {{ labels.filterAll }}
            </button>
          </div>
          <ul v-if="activeTasks.length > 0" class="today-task-list active-task-list">
            <li
              v-for="task in activeTasks"
              :key="`active-${task.id}`"
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

          <p v-else class="empty-text panel-empty-text">{{ labels.noTasks }}</p>
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
            <div class="task-datetime-fields">
              <input v-model="taskForm.startDate" type="date" required />
              <input v-model="taskForm.startTime" type="time" />
            </div>
          </label>

          <label class="task-row">
            <span>{{ labels.dueAt }}</span>
            <div class="task-datetime-fields">
              <input v-model="taskForm.dueDate" type="date" required />
              <input v-model="taskForm.dueTime" type="time" />
            </div>
          </label>

          <label class="task-row">
            <span>{{ labels.priority }}</span>
            <select v-model="taskForm.priority">
              <option value="low">{{ labels.priorityLow }}</option>
              <option value="medium">{{ labels.priorityMedium }}</option>
              <option value="high">{{ labels.priorityHigh }}</option>
            </select>
          </label>

          <label v-if="isEditingTask" class="task-row">
            <span>{{ labels.status }}</span>
            <select v-model="taskForm.status">
              <option value="todo">{{ labels.statusTodo }}</option>
              <option value="doing">{{ labels.statusDoing }}</option>
              <option value="done">{{ labels.statusDone }}</option>
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
            <button
              v-if="isEditingTask"
              type="button"
              class="button-danger"
              :disabled="isSavingTask || isDeletingTask"
              @click="onDeleteTask"
            >
              {{ taskDeleteLabel }}
            </button>
            <button
              type="button"
              class="button-secondary"
              :disabled="isSavingTask || isDeletingTask"
              @click="closeTaskModalFromUi"
            >
              {{ labels.cancel }}
            </button>
            <button type="submit" class="button-primary" :disabled="isSavingTask || isDeletingTask">
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

.success-toast {
  position: fixed;
  top: 72px;
  left: 50%;
  transform: translateX(-50%);
  min-width: 220px;
  padding: 12px 18px;
  border: 1px solid rgba(77, 74, 68, 0.22);
  border-radius: 10px;
  background: rgba(236, 247, 232, 0.96);
  color: #295b2f;
  box-shadow: 0 12px 24px rgba(47, 44, 40, 0.12);
  font-size: 14px;
  font-weight: 600;
  text-align: center;
  z-index: 80;
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -8px);
}

.top-grid {
  display: grid;
  grid-template-columns: minmax(360px, 1fr) minmax(540px, 1.12fr);
  grid-template-areas:
    'today calendar'
    'active calendar'
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

.panel-active {
  grid-area: active;
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
  height: 288px;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-body-tall {
  height: 560px;
}

.panel-body-active {
  height: 228px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.task-filter-bar {
  display: flex;
  gap: 8px;
  padding: 10px 12px 8px;
  border-bottom: 1px solid rgba(77, 74, 68, 0.16);
  background: rgba(255, 255, 255, 0.3);
}

.task-filter-button {
  min-width: 72px;
  height: 28px;
  border: 1px solid rgba(77, 74, 68, 0.22);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  color: #5f5a52;
  font: inherit;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease, border-color 0.15s ease;
}

.task-filter-button.is-active {
  background: #2f97ee;
  border-color: #2f97ee;
  color: #fff;
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

.panel-empty-text {
  padding: 12px;
}

.empty-text-faint {
  position: static;
  padding: 12px;
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
  display: block;
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-gutter: stable;
  scroll-snap-type: y mandatory;
}

.active-task-list {
  flex: 1 1 auto;
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
  scroll-snap-align: start;
  overflow: hidden;
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

.task-datetime-fields {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 140px;
  gap: 10px;
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
.button-primary,
.button-danger {
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

.button-danger {
  border: none;
  background: #d95858;
  color: #fff;
}

.button-secondary:disabled,
.button-primary:disabled,
.button-danger:disabled {
  opacity: 0.7;
  cursor: wait;
}

@media (max-width: 1200px) {
  .top-grid {
    grid-template-columns: 1fr;
    grid-template-areas:
      'today'
      'active'
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
