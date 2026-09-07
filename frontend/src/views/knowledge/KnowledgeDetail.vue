<template>
  <div>
    <div class="page-title">
      {{ isNew ? "新增知识文章" : "知识文章详情" }}
      <el-button link @click="$router.back()">返回</el-button>
    </div>
    <el-card>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标题" prop="title"
          ><el-input v-model="form.title"
        /></el-form-item>
        <el-form-item label="分类"
          ><el-input v-model="form.category"
        /></el-form-item>
        <el-form-item label="关键词"
          ><el-input v-model="form.keywords"
        /></el-form-item>
        <el-form-item label="正文" prop="content"
          ><el-input v-model="form.content" type="textarea" :rows="12"
        /></el-form-item>
        <el-form-item
          ><el-button type="primary" @click="save">保存</el-button
          ><el-button
            v-if="form.id && form.status === 'DRAFT'"
            type="success"
            @click="publish"
            >发布</el-button
          ></el-form-item
        >
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { knowledge } from "../../api";

const route = useRoute();
const isNew = route.params.id === "new";
const formRef = ref();
const form = reactive({
  title: "",
  category: "",
  keywords: "",
  content: "",
  status: "DRAFT",
});
const rules = {
  title: [{ required: true, message: "请输入标题", trigger: "blur" }],
  content: [{ required: true, message: "请输入正文", trigger: "blur" }],
};
const save = async () => {
  await formRef.value.validate();
  if (form.id) await knowledge.update(form.id, form);
  else await knowledge.add(form);
  ElMessage.success("保存成功");
};
const publish = async () => {
  await knowledge.publish(form.id);
  form.status = "PUBLISHED";
  ElMessage.success("发布成功");
};

onMounted(async () => {
  if (!isNew) Object.assign(form, await knowledge.get(route.params.id));
});
</script>
