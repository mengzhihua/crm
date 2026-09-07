<template>
  <section>
    <div class="page-toolbar">
      <h2 class="page-title">{{ contract.name }}</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>
    <el-descriptions border :column="3">
      <el-descriptions-item label="合同号">{{ contract.contractNo }}</el-descriptions-item>
      <el-descriptions-item label="金额">{{ contract.amount }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="tagType(contract.status)">{{ text(maps.contractStatus, contract.status) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="已回款">{{ contract.receivedAmount }}</el-descriptions-item>
      <el-descriptions-item label="待回款">{{ contract.receivableAmount }}</el-descriptions-item>
      <el-descriptions-item label="付款条款">{{ contract.paymentTerms }}</el-descriptions-item>
    </el-descriptions>
    <div class="top-gap">
      <el-button
        v-if="contract.status === 'PENDING_SIGN'"
        type="success"
        @click="activate"
      >激活合同</el-button>
      <el-button
        v-if="contract.status === 'ACTIVE'"
        type="danger"
        @click="terminate"
      >终止合同</el-button>
      <el-button type="primary" @click="paymentDialog = true">登记回款</el-button>
    </div>
    <el-tabs class="top-gap">
      <el-tab-pane label="回款计划">
        <el-table :data="plans">
          <el-table-column prop="seq" label="期次" />
          <el-table-column prop="planAmount" label="计划金额" />
          <el-table-column prop="paidAmount" label="已回款" />
          <el-table-column label="状态">
            <template #default="{ row }">{{ text(maps.paymentStatus, row.status) }}</template>
          </el-table-column>
          <el-table-column prop="planDate" label="计划日期" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="回款记录">
        <el-table :data="payments">
          <el-table-column prop="amount" label="金额" />
          <el-table-column prop="paidDate" label="日期" />
          <el-table-column prop="voucherNo" label="凭证号" />
          <el-table-column label="方式">
            <template #default="{ row }">{{ text(maps.paymentMethod, row.method) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="动态">
        <RecordTimeline target-type="CONTRACT" :target-id="route.params.id" />
      </el-tab-pane>
    </el-tabs>
    <el-dialog v-model="paymentDialog" title="登记回款">
      <el-form label-width="90px">
        <el-form-item label="金额"><el-input-number v-model="payment.amount" :min="0.01" /></el-form-item>
        <el-form-item label="计划ID"><el-input-number v-model="payment.planId" :min="1" /></el-form-item>
        <el-form-item label="日期"><el-date-picker v-model="payment.paidDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="方式">
          <el-select v-model="payment.method">
            <el-option
              v-for="(label, value) in maps.paymentMethod"
              :key="value"
              :label="label"
              :value="value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="凭证号"><el-input v-model="payment.voucherNo" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="addPayment">保存</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useRoute } from "vue-router";
import { contracts } from "../../api";
import { maps, tagType, text } from "../../utils/enums";
import RecordTimeline from "../../components/RecordTimeline.vue";

const route = useRoute();
const contract = reactive({});
const plans = ref([]);
const payments = ref([]);
const paymentDialog = ref(false);
const payment = reactive({
  amount: 0,
  method: "BANK_TRANSFER",
  paidDate: new Date().toISOString().slice(0, 10),
});
const load = async () => {
  Object.assign(contract, await contracts.get(route.params.id));
  plans.value = await contracts.plans(route.params.id);
  payments.value = await contracts.payments(route.params.id);
};
const activate = async () => {
  await contracts.activate(route.params.id);
  ElMessage.success("合同已激活");
  load();
};
const terminate = async () => {
  const { value } = await ElMessageBox.prompt("请输入终止原因", "终止合同");
  await contracts.terminate(route.params.id, { reason: value });
  ElMessage.success("合同已终止");
  load();
};
const addPayment = async () => {
  await contracts.addPayment(route.params.id, payment);
  paymentDialog.value = false;
  ElMessage.success("回款登记成功");
  load();
};
onMounted(load);
</script>
