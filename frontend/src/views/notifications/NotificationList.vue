<template>
  <section>
    <div class="page-toolbar">
      <h2>通知中心</h2>
      <el-checkbox v-model="unreadOnly" @change="load">只看未读</el-checkbox>
      <el-button @click="markAllRead">全部标为已读</el-button>
    </div>
    <el-table :data="rows" stripe @row-click="open">
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ text(maps.notificationType, row.type) }}</template>
      </el-table-column>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.read ? '' : 'danger'">{{ row.read ? "已读" : "未读" }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button v-if="!row.read" link type="primary" @click.stop="read(row)">
            已读
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load"
    />
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { notifications } from "../../api";
import { maps, text } from "../../utils/enums";
import { refreshUnread } from "../../utils/notify";

const router = useRouter();
const rows = ref([]);
const page = ref(1);
const size = ref(20);
const total = ref(0);
const unreadOnly = ref(false);
const load = async () => {
  const result = await notifications.list({
    page: page.value,
    size: size.value,
    unreadOnly: unreadOnly.value,
  });
  rows.value = result.records;
  total.value = result.total;
};
const read = async (row) => {
  await notifications.markRead(row.id);
  await refreshUnread();
  ElMessage.success("已标记为已读");
  load();
};
const markAllRead = async () => {
  await notifications.markAllRead();
  await refreshUnread();
  load();
};
const open = async (row) => {
  if (!row.read) {
    await notifications.markRead(row.id);
    await refreshUnread();
  }
  const paths = {
    QUOTE: row.relatedId ? `/quotes/${row.relatedId}` : "/quotes",
    CASE: row.relatedId ? `/cases/${row.relatedId}` : "/cases",
    CONTRACT: "/contracts",
  };
  router.push(paths[row.relatedType] || "/dashboard");
};
onMounted(load);
</script>
