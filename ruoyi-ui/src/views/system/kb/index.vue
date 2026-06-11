<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="目录管理" name="category">
        <category-tab v-if="activeTab==='category'" :key="'cat'" />
      </el-tab-pane>
      <el-tab-pane label="文档管理" name="document">
        <document-tab v-if="activeTab==='document'" :key="'doc'" />
      </el-tab-pane>
      <el-tab-pane label="回收站" name="recycle">
        <recycle-tab v-if="activeTab==='recycle'" :key="'rec'" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import CategoryTab from './category.vue';
import DocumentTab from './document.vue';
import RecycleTab from './recycle.vue';

export default {
  name: 'KbAdmin',
  components: { CategoryTab, DocumentTab, RecycleTab },
  data() {
    return { activeTab: 'category' };
  }
};
</script>
