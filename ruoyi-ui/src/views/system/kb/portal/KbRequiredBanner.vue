<template>
  <el-card v-if="list.length" class="kb-required-banner" shadow="never">
    <div slot="header" class="banner-header">
      <i class="el-icon-star-on" style="color:#E6A23C; margin-right: 6px"></i>
      <span>新员工必读</span>
    </div>
    <el-row :gutter="16">
      <el-col v-for="d in list" :key="d.id" :span="6">
        <el-card shadow="hover" class="banner-card" @click.native="$router.push('/kb/view/detail/' + d.id)">
          <div class="title">{{ d.title }}</div>
          <div class="meta">{{ d.publishedTime }}</div>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script>
import { listRequired } from '@/api/system/kb/portal';

export default {
  name: 'KbRequiredBanner',
  data() { return { list: [] }; },
  async created() {
    const res = await listRequired({ limit: 4 });
    if (res.code === 200) {
      const list = (res.data && res.data.rows) || (Array.isArray(res.data) ? res.data : []);
      this.list = list;
    }
  }
};
</script>

<style scoped>
.kb-required-banner { margin-bottom: 16px; }
.banner-header { font-size: 16px; font-weight: 500; }
.banner-card { cursor: pointer; }
.title { font-size: 14px; font-weight: 500; }
.meta { color: #999; font-size: 12px; margin-top: 4px; }
</style>
