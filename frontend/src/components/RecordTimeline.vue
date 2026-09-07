<template>
  <el-card class="timeline-card">
    <template #header>
      <div class="timeline-header">
        <span>动态</span>
        <el-button size="small" @click="load">刷新</el-button>
      </div>
    </template>
    <el-form inline @submit.prevent>
      <el-input
        v-model="noteContent"
        placeholder="写一条备注"
        style="width: 320px"
      />
      <el-button type="primary" @click="addNote">发表备注</el-button>
      <input type="file" @change="upload" />
    </el-form>
    <el-timeline>
      <el-timeline-item
        v-for="item in timeline"
        :key="item.key"
        :timestamp="item.time"
      >
        <template v-if="item.kind === 'history'">
          字段 <b>{{ item.field }}</b> 从
          <span>{{ item.oldValue }}</span> 变更为
          <span>{{ item.newValue }}</span>（{{ item.operator }}）
        </template>
        <template v-else-if="item.kind === 'note'">
          <el-tag size="small">备注</el-tag>
          {{ item.content }}（{{ item.author }}）
        </template>
        <template v-else>
          <el-tag size="small" type="warning">附件</el-tag>
          <el-link type="primary" @click="download(item)">
            {{ item.fileName }}
          </el-link>
          <el-button link type="danger" @click="remove(item)">删除</el-button>
        </template>
      </el-timeline-item>
    </el-timeline>
  </el-card>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { records } from "../api";

const props = defineProps({
  targetType: {
    type: String,
    required: true,
  },
  targetId: {
    type: [String, Number],
    required: true,
  },
});

const history = ref([]);
const notes = ref([]);
const attachments = ref([]);
const noteContent = ref("");

const timeline = computed(() => [
  ...history.value.map((item) => ({
    ...item,
    kind: "history",
    key: `h-${item.id}`,
    time: item.changedAt,
  })),
  ...notes.value.map((item) => ({
    ...item,
    kind: "note",
    key: `n-${item.id}`,
    time: item.createdAt,
  })),
  ...attachments.value.map((item) => ({
    ...item,
    kind: "attachment",
    key: `a-${item.id}`,
    time: item.createdAt,
  })),
].sort((a, b) => String(b.time).localeCompare(String(a.time))));

const load = async () => {
  const params = {
    targetType: props.targetType,
    targetId: props.targetId,
  };
  [history.value, notes.value, attachments.value] = await Promise.all([
    records.history(params),
    records.notes(params),
    records.attachments(params),
  ]);
};

const addNote = async () => {
  if (!noteContent.value.trim()) {
    return;
  }
  await records.addNote({
    targetType: props.targetType,
    targetId: props.targetId,
    content: noteContent.value,
  });
  noteContent.value = "";
  ElMessage.success("备注已发表");
  load();
};

const upload = async (event) => {
  const file = event.target.files[0];
  if (!file) {
    return;
  }
  const form = new FormData();
  form.append("file", file);
  form.append("targetType", props.targetType);
  form.append("targetId", props.targetId);
  await records.upload(form);
  ElMessage.success("附件上传成功");
  event.target.value = "";
  load();
};

const download = async (item) => {
  const blob = await records.download(item.id);
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = item.fileName;
  link.click();
  URL.revokeObjectURL(url);
};

const remove = async (item) => {
  await records.removeAttachment(item.id);
  ElMessage.success("附件已删除");
  load();
};

watch(() => [props.targetType, props.targetId], load, { immediate: true });
</script>
