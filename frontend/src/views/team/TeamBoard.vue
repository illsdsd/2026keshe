<template>
  <div class="page-container" v-loading="loading">
    <div class="board-head">
      <el-page-header content="任务看板" @back="$router.push(`/teams/${teamId}`)" />
      <el-button type="primary" @click="openCreate()">
        <el-icon><Plus /></el-icon> 新建任务
      </el-button>
    </div>

    <!-- 进度统计柱状图 -->
    <el-card class="stat-card">
      <div class="stat-title">整体进度（共 {{ stat.total }} 个任务）</div>
      <div class="bars">
        <div v-for="b in bars" :key="b.key" class="bar-row">
          <span class="bar-label">{{ b.label }}</span>
          <div class="bar-track">
            <div class="bar-fill" :style="{ width: pct(b.value) + '%', background: b.color }">
              <span v-if="b.value" class="bar-num">{{ b.value }}</span>
            </div>
          </div>
          <span class="bar-pct">{{ pct(b.value) }}%</span>
        </div>
      </div>
    </el-card>

    <!-- 三栏看板 -->
    <div class="board">
      <div v-for="col in columns" :key="col.status" class="column">
        <div class="column-head" :style="{ borderColor: col.color }">
          <span>{{ col.label }}</span>
          <el-tag size="small" round>{{ col.list.length }}</el-tag>
        </div>
        <draggable
          :list="col.list"
          group="tasks"
          item-key="id"
          class="card-list"
          :animation="180"
          @change="(e) => onChange(e, col.status)"
        >
          <template #item="{ element }">
            <div class="task-card" @click="openEdit(element)">
              <div class="task-title">{{ element.title }}</div>
              <div v-if="element.description" class="task-desc">{{ element.description }}</div>
              <div class="task-foot">
                <span v-if="element.assigneeName" class="assignee">
                  <el-icon><User /></el-icon> {{ element.assigneeName }}
                </span>
                <span v-else class="text-muted">未指派</span>
                <span v-if="element.deadline" class="deadline">
                  {{ element.deadline.slice(5, 10) }}
                </span>
              </div>
            </div>
          </template>
        </draggable>
      </div>
    </div>

    <!-- 任务新建 / 编辑 -->
    <el-dialog v-model="dialog" :title="editing ? '编辑任务' : '新建任务'" width="500px">
      <el-form :model="form" label-width="72px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="任务标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="form.assigneeId" placeholder="选择成员" clearable style="width: 100%">
            <el-option v-for="m in members" :key="m.userId" :label="m.nickname" :value="m.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止">
          <el-date-picker v-model="form.deadline" type="datetime"
                          value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button v-if="editing" type="danger" plain style="float: left" @click="onDelete">删除</el-button>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import draggable from 'vuedraggable'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTeam } from '@/api/team'
import {
  listTeamTasks, taskStat, createTask, updateTask, updateTaskStatus, deleteTask
} from '@/api/task'

const route = useRoute()
const teamId = route.params.id

const loading = ref(false)
const members = ref([])
const todo = ref([])
const doing = ref([])
const done = ref([])
const stat = reactive({ todo: 0, doing: 0, done: 0, total: 0 })

const columns = computed(() => [
  { status: 'TODO', label: '待办', color: '#909399', list: todo.value },
  { status: 'DOING', label: '进行中', color: '#e6a23c', list: doing.value },
  { status: 'DONE', label: '已完成', color: '#67c23a', list: done.value }
])

const bars = computed(() => [
  { key: 'todo', label: '待办', value: stat.todo, color: '#909399' },
  { key: 'doing', label: '进行中', value: stat.doing, color: '#e6a23c' },
  { key: 'done', label: '已完成', value: stat.done, color: '#67c23a' }
])

function pct(v) {
  return stat.total ? Math.round((v / stat.total) * 100) : 0
}

const dialog = ref(false)
const editing = ref(false)
const saving = ref(false)
const form = reactive({ id: null, title: '', description: '', assigneeId: null, deadline: null })

async function loadTasks() {
  const tasks = await listTeamTasks(teamId)
  todo.value = tasks.filter((t) => t.status === 'TODO')
  doing.value = tasks.filter((t) => t.status === 'DOING')
  done.value = tasks.filter((t) => t.status === 'DONE')
  Object.assign(stat, await taskStat(teamId))
}

async function onChange(evt, status) {
  // 仅当卡片被拖入本列时更新其状态
  if (evt.added) {
    const task = evt.added.element
    try {
      await updateTaskStatus(task.id, { status, sortOrder: evt.added.newIndex })
      Object.assign(stat, await taskStat(teamId))
    } catch {
      loadTasks()
    }
  }
}

function openCreate() {
  editing.value = false
  Object.assign(form, { id: null, title: '', description: '', assigneeId: null, deadline: null })
  dialog.value = true
}

function openEdit(task) {
  editing.value = true
  Object.assign(form, {
    id: task.id,
    title: task.title,
    description: task.description,
    assigneeId: task.assigneeId,
    deadline: task.deadline
  })
  dialog.value = true
}

async function save() {
  if (!form.title?.trim()) {
    ElMessage.warning('请输入任务标题')
    return
  }
  saving.value = true
  try {
    if (editing.value) {
      await updateTask(form.id, form)
    } else {
      await createTask({ teamId, ...form })
    }
    ElMessage.success('已保存')
    dialog.value = false
    loadTasks()
  } finally {
    saving.value = false
  }
}

async function onDelete() {
  await ElMessageBox.confirm('确定删除该任务？', '提示', { type: 'warning' })
  await deleteTask(form.id)
  ElMessage.success('已删除')
  dialog.value = false
  loadTasks()
}

onMounted(async () => {
  loading.value = true
  try {
    const detail = await getTeam(teamId)
    members.value = detail.members || []
    await loadTasks()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.board-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-card {
  margin: 16px 0;
}
.stat-title {
  font-weight: 600;
  margin-bottom: 14px;
}
.bars {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.bar-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.bar-label {
  width: 56px;
  font-size: 13px;
  color: #606266;
}
.bar-track {
  flex: 1;
  height: 22px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  transition: width 0.4s ease;
  min-width: 2px;
}
.bar-num {
  color: #fff;
  font-size: 12px;
  padding-right: 6px;
}
.bar-pct {
  width: 40px;
  text-align: right;
  font-size: 13px;
  color: #909399;
}
.board {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.column {
  flex: 1;
  background: #f0f2f5;
  border-radius: 8px;
  padding: 12px;
  min-height: 200px;
}
.column-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  padding-bottom: 8px;
  margin-bottom: 10px;
  border-bottom: 2px solid;
}
.card-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 120px;
}
.task-card {
  background: #fff;
  border-radius: 6px;
  padding: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  cursor: grab;
}
.task-card:active {
  cursor: grabbing;
}
.task-title {
  font-weight: 500;
  margin-bottom: 6px;
}
.task-desc {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.task-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}
.assignee {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  color: #3b6ef5;
}
.deadline {
  color: #e6a23c;
}
</style>
