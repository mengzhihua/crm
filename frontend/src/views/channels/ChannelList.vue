<template>
  <section>
    <div class="page-toolbar">
      <h2>消息渠道</h2>
      <el-button type="primary" @click="open()">新增渠道</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="name" label="名称" />
      <el-table-column label="类型">
        <template #default="{ row }">{{ text(maps.channelType, row.type) }}</template>
      </el-table-column>
      <el-table-column prop="target" label="目标" />
      <el-table-column prop="enabled" label="启用">
        <template #default="{ row }">{{ row.enabled ? "是" : "否" }}</template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link type="primary" @click="test(row)">测试</el-button>
          <el-button link @click="open(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="visible" :title="form.id ? '编辑渠道' : '新增渠道'">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type">
            <el-option
              v-for="(label, value) in maps.channelType"
              :key="value"
              :label="label"
              :value="value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标"><el-input v-model="form.target" /></el-form-item>
        <el-form-item label="密钥"><el-input v-model="form.secret" /></el-form-item>
        <el-form-item label="事件">
          <el-select v-model="events" multiple>
            <el-option
              v-for="(label, value) in maps.notificationType"
              :key="value"
              :label="label"
              :value="value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { channels } from "../../api";
import { maps, text } from "../../utils/enums";

const rows = ref([]);
const visible = ref(false);
const events = ref([]);
const form = reactive({
  name: "",
  type: "WEBHOOK",
  target: "",
  secret: "",
  eventTypes: "",
  enabled: true,
});
const load = async () => {
  rows.value = await channels.list();
};
const open = (row) => {
  Object.assign(form, row || {
    name: "",
    type: "WEBHOOK",
    target: "",
    secret: "",
    eventTypes: "",
    enabled: true,
  });
  events.value = form.eventTypes ? form.eventTypes.split(",") : [];
  visible.value = true;
};
const save = async () => {
  form.eventTypes = events.value.join(",");
  if (form.id) await channels.update(form.id, form);
  else await channels.add(form);
  visible.value = false;
  await load();
};
const test = async (row) => {
  await channels.test(row.id);
  ElMessage.success("测试请求已发送");
};
const remove = async (row) => {
  await ElMessageBox.confirm("确认删除该渠道？", "提示");
  await channels.remove(row.id);
  await load();
};
onMounted(load);
</script>
