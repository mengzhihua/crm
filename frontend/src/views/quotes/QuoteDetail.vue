<template>
  <section>
    <div class="page-toolbar">
      <h2>{{ quote.name }}</h2>
      <el-tag :type="tagType(quote.status)">
        {{ text(maps.quoteStatus, quote.status) }}
      </el-tag>
    </div>
    <el-descriptions :column="3" border>
      <el-descriptions-item label="报价单号">
        {{ quote.quoteNo }}
      </el-descriptions-item>
      <el-descriptions-item label="小计">
        {{ quote.subtotal || 0 }}
      </el-descriptions-item>
      <el-descriptions-item label="总额">
        {{ quote.totalAmount || 0 }}
      </el-descriptions-item>
    </el-descriptions>
    <div class="page-toolbar top-gap">
      <el-form inline>
        <el-form-item label="整单折扣">
          <el-input-number v-model="discountRate" :min="0" :max="100" />
        </el-form-item>
        <el-button
          v-if="editable"
          type="primary"
          @click="saveDiscount"
        >
          保存折扣
        </el-button>
      </el-form>
      <div>
        <el-button v-if="editable" type="primary" @click="saveItems">
          保存行项目
        </el-button>
        <el-button v-if="quote.status === 'DRAFT' || quote.status === 'REJECTED'" @click="submit">
          提交审批
        </el-button>
        <el-button v-if="quote.status === 'APPROVED'" type="success" @click="accept">
          接受报价
        </el-button>
      </div>
    </div>
    <el-table :data="items">
      <el-table-column label="产品">
        <template #default="{ row }">
          <el-select v-model="row.productId" :disabled="!editable">
            <el-option
              v-for="product in products"
              :key="product.id"
              :label="`${product.code} - ${product.name}`"
              :value="product.id"
            />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="数量">
        <template #default="{ row }">
          <el-input-number v-model="row.quantity" :disabled="!editable" :min="0.0001" />
        </template>
      </el-table-column>
      <el-table-column label="单价">
        <template #default="{ row }">
          <el-input-number v-model="row.unitPrice" :disabled="!editable" :min="0" />
        </template>
      </el-table-column>
      <el-table-column label="折扣">
        <template #default="{ row }">
          <el-input-number v-model="row.discountRate" :disabled="!editable" :min="0" :max="100" />
        </template>
      </el-table-column>
      <el-table-column prop="totalPrice" label="小计" />
    </el-table>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { products as productApi, quotes as quoteApi } from "../../api";
import { maps, tagType, text } from "../../utils/enums";

const route = useRoute();
const quote = reactive({});
const items = ref([]);
const products = ref([]);
const discountRate = ref(0);
const editable = computed(() => ["DRAFT", "REJECTED"].includes(quote.status));
const load = async () => {
  Object.assign(quote, await quoteApi.get(route.params.id));
  discountRate.value = quote.discountRate || 0;
  items.value = await quoteApi.items(route.params.id);
  products.value = (await productApi.list({ page: 1, size: 100 })).records;
};
const saveItems = async () => {
  await quoteApi.replaceItems(route.params.id, { items: items.value });
  ElMessage.success("行项目保存成功");
  load();
};
const saveDiscount = async () => {
  Object.assign(quote, await quoteApi.discount(route.params.id, {
    discountRate: discountRate.value,
  }));
  ElMessage.success("折扣保存成功");
};
const submit = async () => {
  Object.assign(quote, await quoteApi.submit(route.params.id));
  ElMessage.success("报价单已提交");
};
const accept = async () => {
  Object.assign(quote, await quoteApi.accept(route.params.id));
  ElMessage.success("报价单已接受");
};
onMounted(load);
</script>
