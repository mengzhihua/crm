<template>
  <section>
    <div class="page-toolbar">
      <h2>{{ opportunity.name }}</h2>
      <el-tag :type="tagType(opportunity.stage)">
        {{ text(maps.stage, opportunity.stage) }}
      </el-tag>
    </div>
    <el-descriptions :column="3" border>
      <el-descriptions-item label="客户ID">
        {{ opportunity.accountId || "-" }}
      </el-descriptions-item>
      <el-descriptions-item label="金额">
        {{ opportunity.amount || 0 }}
      </el-descriptions-item>
      <el-descriptions-item label="预计关闭">
        {{ opportunity.expectedCloseDate || "-" }}
      </el-descriptions-item>
    </el-descriptions>
    <el-tabs v-model="activeTab" class="content-tabs">
      <el-tab-pane label="产品行项目" name="items">
        <div class="page-toolbar">
          <span>保存后商机金额自动重算</span>
          <el-button type="primary" @click="saveItems">保存行项目</el-button>
        </div>
        <el-table :data="items">
          <el-table-column label="产品">
            <template #default="{ row }">
              <el-select
                v-model="row.productId"
                filterable
                @change="fillPrice(row)"
              >
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
              <el-input-number v-model="row.quantity" :min="0.0001" />
            </template>
          </el-table-column>
          <el-table-column label="单价">
            <template #default="{ row }">
              <el-input-number v-model="row.unitPrice" :min="0" />
            </template>
          </el-table-column>
          <el-table-column label="折扣">
            <template #default="{ row }">
              <el-input-number v-model="row.discountRate" :min="0" :max="100" />
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="{ $index }">
              <el-button link type="danger" @click="items.splice($index, 1)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button class="top-gap" @click="addItem">新增行</el-button>
      </el-tab-pane>
      <el-tab-pane label="报价单" name="quotes">
        <el-button type="primary" @click="createQuote">
          基于商机创建报价
        </el-button>
        <el-table :data="quotes">
          <el-table-column prop="quoteNo" label="报价单号" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="totalAmount" label="总额" />
          <el-table-column label="状态">
            <template #default="{ row }">
              <el-tag :type="tagType(row.status)">
                {{ text(maps.quoteStatus, row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="活动" name="activities">
        <el-empty description="请在活动模块查看关联活动" />
      </el-tab-pane>
    </el-tabs>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  opportunities,
  opportunityItems,
  products as productApi,
  quotes as quoteApi,
} from "../../api";
import { maps, tagType, text } from "../../utils/enums";

const route = useRoute();
const router = useRouter();
const opportunity = reactive({});
const products = ref([]);
const items = ref([]);
const quotes = ref([]);
const activeTab = ref("items");
const load = async () => {
  Object.assign(opportunity, await opportunities.get(route.params.id));
  products.value = (await productApi.list({ page: 1, size: 100 })).records;
  items.value = await opportunityItems.list(route.params.id);
  quotes.value = (await quoteApi.list({
    page: 1,
    size: 100,
    opportunityId: route.params.id,
  })).records;
};
const addItem = () => {
  items.value.push({
    productId: null,
    quantity: 1,
    unitPrice: 0,
    discountRate: 0,
  });
};
const fillPrice = (row) => {
  const product = products.value.find((item) => item.id === row.productId);
  if (product && !row.unitPrice) {
    row.unitPrice = product.listPrice;
  }
};
const saveItems = async () => {
  await opportunityItems.replace(route.params.id, { items: items.value });
  ElMessage.success("行项目保存成功");
  load();
};
const createQuote = async () => {
  const quote = await quoteApi.fromOpportunity(route.params.id);
  ElMessage.success("报价单创建成功");
  router.push(`/quotes/${quote.id}`);
};
onMounted(load);
</script>
