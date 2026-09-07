<template>
  <section>
    <div class="page-toolbar">
      <h2 class="page-title">市场活动</h2>
      <el-button v-if="canEdit" type="primary" @click="openCreate">新建活动</el-button>
    </div>
    <el-form inline @submit.prevent>
      <el-input v-model="keyword" placeholder="活动名称" clearable />
      <el-select v-model="status" placeholder="状态" clearable>
        <el-option
          v-for="(label, value) in maps.campaignStatus"
          :key="value"
          :label="label"
          :value="value"
        />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </el-form>
    <el-table :data="records">
      <el-table-column prop="name" label="名称" />
      <el-table-column label="类型">
        <template #default="{ row }">{{ text(maps.campaignType, row.type) }}</template>
      </el-table-column>
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="tagType(row.status)">
            {{ text(maps.campaignStatus, row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="开始日期" />
      <el-table-column prop="endDate" label="结束日期" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/campaigns/${row.id}`)">
            详情
          </el-button>
          <el-button v-if="canEdit" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      layout="total, prev, pager, next"
      :total="total"
      @current-change="load"
    />
    <el-dialog v-model="dialogVisible" title="市场活动">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type">
            <el-option
              v-for="(label, value) in maps.campaignType"
              :key="value"
              :label="label"
              :value="value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option
              v-for="(label, value) in maps.campaignStatus"
              :key="value"
              :label="label"
              :value="value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="结束日期"><el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="预算"><el-input-number v-model="form.budgetCost" :min="0" /></el-form-item>
        <el-form-item label="实际成本"><el-input-number v-model="form.actualCost" :min="0" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { campaigns } from "../../api";
import { maps, tagType, text } from "../../utils/enums";
import { currentUser } from "../../utils/permission";

const records = ref([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const keyword = ref("");
const status = ref("");
const dialogVisible = ref(false);
const form = reactive({
  name: "",
  type: "EVENT",
  status: "PLANNED",
});
const canEdit = computed(() => ["ADMIN", "SALES_MANAGER"].includes(currentUser()?.role));

const load = async () => {
  const data = await campaigns.list({
    page: page.value,
    size: size.value,
    keyword: keyword.value,
    status: status.value || undefined,
  });
  records.value = data.records;
  total.value = data.total;
};
const openCreate = () => {
  Object.assign(form, { id: null, name: "", type: "EVENT", status: "PLANNED" });
  dialogVisible.value = true;
};
const save = async () => {
  if (!form.name.trim()) {
    ElMessage.warning("请输入活动名称");
    return;
  }
  if (form.id) {
    await campaigns.update(form.id, form);
  } else {
    await campaigns.add(form);
  }
  dialogVisible.value = false;
  ElMessage.success("保存成功");
  load();
};
const remove = async (row) => {
  await ElMessageBox.confirm("确认删除该市场活动吗？", "提示");
  await campaigns.remove(row.id);
  ElMessage.success("删除成功");
  load();
};
onMounted(load);
</script>
