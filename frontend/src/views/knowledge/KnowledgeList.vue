<template>
  <div>
    <div class="page-title">知识库</div>
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索标题、关键词、正文"
        clearable
      /><el-button type="primary" @click="load">搜索</el-button
      ><el-button type="success" @click="$router.push('/knowledge/new')"
        >新增文章</el-button
      >
    </div>
    <el-card>
      <el-table :data="rows"
        ><el-table-column prop="title" label="标题" /><el-table-column
          prop="category"
          label="分类"
        /><el-table-column prop="status" label="状态"
          ><template #default="{ row }"
            ><el-tag :type="tagType(row.status)">{{
              text(maps.articleStatus, row.status)
            }}</el-tag></template
          ></el-table-column
        ><el-table-column prop="viewCount" label="浏览量" /><el-table-column
          label="操作"
          ><template #default="{ row }"
            ><el-button
              link
              type="primary"
              @click="$router.push(`/knowledge/${row.id}`)"
              >查看</el-button
            ><el-button
              v-if="row.status === 'DRAFT'"
              link
              type="success"
              @click="publish(row)"
              >发布</el-button
            ><el-button link type="danger" @click="remove(row)"
              >删除</el-button
            ></template
          ></el-table-column
        ></el-table
      >
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="load"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { knowledge } from "../../api";
import { maps, tagType, text } from "../../utils/enums";

const rows = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const keyword = ref("");
const load = async () => {
  const data = await knowledge.list({
    page: page.value,
    size: size.value,
    keyword: keyword.value,
  });
  rows.value = data.records;
  total.value = data.total;
};
const publish = async (row) => {
  await knowledge.publish(row.id);
  ElMessage.success("发布成功");
  await load();
};
const remove = async (row) => {
  await ElMessageBox.confirm("确认删除吗？", "提示");
  await knowledge.remove(row.id);
  ElMessage.success("删除成功");
  await load();
};

onMounted(load);
</script>
