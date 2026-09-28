<template>
  <section>
    <div class="page-toolbar"><h2>数据权限</h2></div>
    <el-table :data="rows" border>
      <el-table-column prop="role" label="角色">
        <template #default="{ row }">{{ text(maps.role, row.role) }}</template>
      </el-table-column>
      <el-table-column prop="objectType" label="对象" />
      <el-table-column label="范围">
        <template #default="{ row }">
          <el-select v-model="row.scope" @change="save(row)">
            <el-option
              v-for="(label, value) in maps.dataScope"
              :key="value"
              :label="label"
              :value="value"
            />
          </el-select>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { dataScopes } from "../../api";
import { maps, text } from "../../utils/enums";

const rows = ref([]);
const load = async () => {
  rows.value = await dataScopes.list();
};
const save = async (row) => {
  await dataScopes.save(row);
};
onMounted(load);
</script>
