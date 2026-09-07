<template>
  <div>
    <div class="page-title">
      工单详情 <el-button link @click="$router.back()">返回</el-button>
    </div>
    <el-card v-if="item">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="工单号">{{
          item.caseNo
        }}</el-descriptions-item>
        <el-descriptions-item label="主题">{{
          item.subject
        }}</el-descriptions-item>
        <el-descriptions-item label="状态"
          ><el-tag :type="tagType(item.status)">{{
            text(maps.caseStatus, item.status)
          }}</el-tag></el-descriptions-item
        >
        <el-descriptions-item label="优先级"
          ><el-tag :type="tagType(item.priority)">{{
            text(maps.priority, item.priority)
          }}</el-tag></el-descriptions-item
        >
        <el-descriptions-item label="负责人">{{
          item.owner || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="SLA">{{
          remaining(item.slaDueAt)
        }}</el-descriptions-item>
      </el-descriptions>
      <p>{{ item.description }}</p>
      <div class="toolbar">
        <el-button
          v-for="target in transitions[item.status] || []"
          :key="target"
          type="primary"
          @click="changeStatus(target)"
          >{{ text(maps.caseStatus, target) }}</el-button
        >
        <el-button type="warning" @click="escalate">升级</el-button>
        <el-button @click="assignVisible = true">分派</el-button>
      </div>
    </el-card>
    <el-card>
      <template #header>评论时间线</template>
      <el-timeline
        ><el-timeline-item v-for="comment in comments" :key="comment.id"
          ><el-tag v-if="comment.internal" size="small">内部</el-tag>
          {{ comment.author }}：{{ comment.content }}</el-timeline-item
        ></el-timeline
      >
      <el-form
        ref="commentRef"
        :model="commentForm"
        :rules="commentRules"
        inline
        ><el-form-item prop="author"
          ><el-input
            v-model="commentForm.author"
            placeholder="作者" /></el-form-item
        ><el-form-item prop="content"
          ><el-input
            v-model="commentForm.content"
            placeholder="评论内容" /></el-form-item
        ><el-form-item
          ><el-checkbox v-model="commentForm.internal">内部备注</el-checkbox
          ><el-button type="primary" @click="addComment"
            >发表评论</el-button
          ></el-form-item
        ></el-form
      >
    </el-card>
    <el-dialog v-model="solutionVisible" title="解决工单"
      ><el-form :model="statusForm"
        ><el-form-item label="解决方案"
          ><el-input
            v-model="statusForm.solution"
            type="textarea" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="solutionVisible = false">取消</el-button
        ><el-button type="primary" @click="submitStatus"
          >确认解决</el-button
        ></template
      ></el-dialog
    >
    <el-dialog v-model="assignVisible" title="分派工单"
      ><el-form :model="assignForm"
        ><el-form-item label="负责人"
          ><el-input v-model="assignForm.owner" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="assignVisible = false">取消</el-button
        ><el-button type="primary" @click="submitAssign"
          >保存</el-button
        ></template
      ></el-dialog
    >
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { cases } from "../../api";
import { maps, tagType, text } from "../../utils/enums";

const route = useRoute();
const item = ref();
const comments = ref([]);
const solutionVisible = ref(false);
const assignVisible = ref(false);
const statusForm = reactive({});
const assignForm = reactive({ owner: "" });
const commentForm = reactive({ author: "", content: "", internal: false });
const commentRef = ref();
const commentRules = {
  author: [{ required: true, message: "请输入作者", trigger: "blur" }],
  content: [{ required: true, message: "请输入评论", trigger: "blur" }],
};
const transitions = {
  NEW: ["IN_PROGRESS", "ESCALATED"],
  IN_PROGRESS: ["PENDING_CUSTOMER", "ESCALATED", "RESOLVED"],
  PENDING_CUSTOMER: ["IN_PROGRESS", "RESOLVED"],
  ESCALATED: ["IN_PROGRESS", "RESOLVED"],
  RESOLVED: ["CLOSED", "IN_PROGRESS"],
};
const remaining = (value) => {
  if (!value) return "-";
  const hours = (new Date(value) - Date.now()) / 3600000;
  return hours < 0
    ? `已超期 ${Math.abs(hours).toFixed(1)} 小时`
    : `${hours.toFixed(1)} 小时`;
};
const load = async () => {
  item.value = await cases.get(route.params.id);
  comments.value = await cases.comments(route.params.id);
};
const changeStatus = (target) => {
  Object.assign(statusForm, { status: target, solution: "" });
  if (target === "RESOLVED") solutionVisible.value = true;
  else submitStatus();
};
const submitStatus = async () => {
  await cases.status(route.params.id, statusForm);
  ElMessage.success("状态更新成功");
  solutionVisible.value = false;
  await load();
};
const escalate = async () => {
  await cases.escalate(route.params.id);
  ElMessage.success("升级成功");
  await load();
};
const submitAssign = async () => {
  await cases.assign(route.params.id, assignForm);
  ElMessage.success("分派成功");
  assignVisible.value = false;
  await load();
};
const addComment = async () => {
  await commentRef.value.validate();
  await cases.comment(route.params.id, commentForm);
  ElMessage.success("评论成功");
  commentForm.content = "";
  await load();
};

onMounted(load);
</script>
