<template>
  <div class="kb-required">
    <el-page-header @back="$router.back()" content="新员工必读" style="margin-bottom: 12px" />
    <el-alert title="📌 以下内容为新员工入职必读" type="warning" :closable="false" show-icon style="margin-bottom: 16px" />
    <el-row :gutter="16">
      <el-col v-for="d in list" :key="d.id" :span="6" style="margin-bottom: 16px">
        <el-card shadow="hover" class="required-card" @click.native="$router.push('/kb/view/detail/' + d.id)">
          <div class="card-title">
            <el-tag type="warning" size="mini" style="margin-right:4px">★</el-tag>
            {{ d.title }}
          </div>
          <div class="card-meta">
            <el-tag v-if="d.docType===1" size="mini" type="info">文件</el-tag>
            <el-tag v-else size="mini">富文本</el-tag>
            <span style="margin-left: 8px">{{ d.publishedTime }}</span>
          </div>
          <div v-if="d.summary" class="card-summary">{{ d.summary }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { listRequired } from '@/api/system/kb/portal';

export default {
  name: 'KbRequired',
  data() { return { list: [] }; },
  async created() {
    const res = await listRequired({ limit: 20 });
    if (res.code === 200) {
      this.list = res.rows || [];
    }
  }
};
</script>

<style scoped>
.kb-required { padding: 16px; }
.required-card { cursor: pointer; min-height: 120px; }
.card-title { font-size: 16px; font-weight: 500; margin-bottom: 8px; }
.card-meta { color: #999; font-size: 12px; margin-bottom: 6px; }
.card-summary { color: #666; font-size: 13px; }
</style>
