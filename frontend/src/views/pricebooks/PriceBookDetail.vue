<template>
  <section>
    <div class="page-toolbar">
      <h2>{{ book.name }}：价格条目</h2>
      <el-button v-if="canEdit" type="primary" @click="openCreate">
        新增条目
      </el-button>
    </div>
    <el-table :data="entries" stripe>
      <el-table-column prop="productId" label="产品ID" />
      <el-table-column prop="unitPrice" label="单价" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="row.active ? 'success' : 'info'">
            {{ row.active ? "启用" : "停用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button v-if="canEdit" link type="danger" @click="remove(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" title="价格条目">
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item label="产品" prop="productId">
          <el-select v-model="form.productId" filterable>
            <el-option
              v-for="product in products"
              :key="product.id"
              :label="`${product.code} - ${product.name}`"
              :value="product.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="form.unitPrice" :min="0" />
        </el-form-item>
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
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { priceBooks, products as productApi } from "../../api";
import { currentUser } from "../../utils/permission";

const route = useRoute();
const book = reactive({ name: "价格手册" });
const entries = ref([]);
const products = ref([]);
const dialogVisible = ref(false);
const formRef = ref();
const form = reactive({ productId: null, unitPrice: 0, active: true });
const rules = {
  productId: [{ required: true, message: "请选择产品", trigger: "change" }],
  unitPrice: [{ required: true, message: "请输入单价", trigger: "blur" }],
};
const canEdit = computed(() =>
  ["ADMIN", "SALES_MANAGER"].includes(currentUser()?.role)
);
const load = async () => {
  Object.assign(book, await priceBooks.get(route.params.id));
  entries.value = await priceBooks.entries(route.params.id);
  products.value = (await productApi.list({ page: 1, size: 100 })).records;
};
const openCreate = () => {
  Object.assign(form, { productId: null, unitPrice: 0, active: true });
  dialogVisible.value = true;
};
const save = async () => {
  await formRef.value.validate();
  await priceBooks.addEntry(route.params.id, form);
  ElMessage.success("保存成功");
  dialogVisible.value = false;
  load();
};
const remove = async (row) => {
  await ElMessageBox.confirm("确认删除该价格条目吗？", "提示");
  await priceBooks.removeEntry(route.params.id, row.id);
  ElMessage.success("删除成功");
  load();
};
onMounted(load);
</script>
