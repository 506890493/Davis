<template>
  <div>
    <el-row :gutter="10">
      <el-col :span="8">
        <el-card>
          <div slot="header" class="clearfix">
            <span>目录树</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="handleAddRoot">新增根目录</el-button>
          </div>
          <el-tree
            ref="tree"
            :data="treeData"
            :props="defaultProps"
            node-key="id"
            :expand-on-click-node="false"
            :default-expanded-keys="expandedKeys"
            draggable
            @node-click="handleNodeClick"
            @node-drop="handleDrop"
          >
            <div class="custom-tree-node" slot-scope="{ node, data }">
              <span>
                <i v-if="data.isRequired===1" style="color:#E6A23C;margin-right:4px">★</i>
                {{ node.label }}
              </span>
              <span>
                <el-button type="text" size="mini" @click.stop="handleAdd(data)">新增</el-button>
                <el-button type="text" size="mini" @click.stop="handleEdit(data)">编辑</el-button>
                <el-button type="text" size="mini" @click.stop="handleRequired(data)" :type="data.isRequired===1?'success':''">{{ data.isRequired===1?'已必读':'设必读' }}</el-button>
                <el-button type="text" size="mini" @click.stop="handleDelete(data)" style="color:red">删除</el-button>
              </span>
            </div>
          </el-tree>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <div slot="header">目录详情</div>
          <el-form v-if="current" :model="current" label-width="100px">
            <el-form-item label="ID">{{ current.id }}</el-form-item>
            <el-form-item label="名称">{{ current.name }}</el-form-item>
            <el-form-item label="父目录 ID">{{ current.parentId }}</el-form-item>
            <el-form-item label="图标">{{ current.icon }}</el-form-item>
            <el-form-item label="排序">{{ current.orderNum }}</el-form-item>
            <el-form-item label="新员工必读">
              <el-tag v-if="current.isRequired===1" type="success">是</el-tag>
              <el-tag v-else type="info">否</el-tag>
            </el-form-item>
            <el-form-item label="状态">
              <el-tag v-if="current.status===1" type="success">正常</el-tag>
              <el-tag v-else type="danger">停用</el-tag>
            </el-form-item>
            <el-form-item label="创建时间">{{ current.createTime }}</el-form-item>
            <el-form-item label="备注">{{ current.remark }}</el-form-item>
          </el-form>
          <div v-else class="empty-tip">请点击左侧目录查看详情</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px" @closed="resetForm">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="父目录">
          <el-cascader
            v-model="form.parentIdArr"
            :options="parentOptions"
            :props="{ checkStrictly: true, value: 'id', label: 'name', emitPath: false }"
            clearable
            change-on-select
          />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="如 documentation" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.orderNum" :min="0" />
        </el-form-item>
        <el-form-item label="新员工必读">
          <el-switch v-model="form.isRequired" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCategory, addCategory, updateCategory, delCategory, orderCategory } from '@/api/system/kb/category';

export default {
  name: 'KbCategory',
  data() {
    return {
      treeData: [],
      defaultProps: { children: 'children', label: 'name' },
      expandedKeys: [],
      current: null,
      dialogVisible: false,
      dialogTitle: '',
      form: { id: null, parentId: 0, parentIdArr: null, name: '', icon: '', orderNum: 0, isRequired: 0, status: 1, remark: '' },
      rules: { name: [{ required: true, message: '请输入名称', trigger: 'blur' }] },
      parentOptions: []
    };
  },
  watch: {
    treeData: {
      handler(val) {
        this.parentOptions = [{ id: 0, name: '根目录', children: val }];
      },
      deep: true
    }
  },
  created() {
    this.loadTree();
  },
  methods: {
    async loadTree() {
      const res = await listCategory({});
      if (res.code === 200) {
        // 后端返回 TableDataInfo {total, rows}，需取 rows
        const list = (res.data && res.data.rows) || (Array.isArray(res.data) ? res.data : []);
        this.treeData = this.buildTree(list);
        this.expandedKeys = list.filter(c => c.parentId === 0).map(c => c.id);
      }
    },
    buildTree(list) {
      const map = {}; const roots = [];
      list.forEach(c => { map[c.id] = { ...c, children: [] }; });
      list.forEach(c => {
        if (c.parentId === 0) roots.push(map[c.id]);
        else if (map[c.parentId]) map[c.parentId].children.push(map[c.id]);
      });
      return roots;
    },
    handleNodeClick(data) { this.current = data; },
    handleAddRoot() { this.openDialog(null, 0); },
    handleAdd(parent) { this.openDialog(null, parent.id); },
    handleEdit(data) { this.openDialog(data); },
    openDialog(data, parentId) {
      if (data) {
        this.form = { ...data, parentIdArr: data.parentId };
        this.dialogTitle = '编辑目录';
      } else {
        this.form = { id: null, parentId: parentId || 0, parentIdArr: parentId || 0, name: '', icon: '', orderNum: 0, isRequired: 0, status: 1, remark: '' };
        this.dialogTitle = '新增目录';
      }
      this.dialogVisible = true;
    },
    resetForm() { this.form = { id: null, parentId: 0, parentIdArr: null, name: '', icon: '', orderNum: 0, isRequired: 0, status: 1, remark: '' }; },
    async submitForm() {
      await this.$refs.form.validate();
      const payload = { ...this.form, parentId: this.form.parentIdArr || 0 };
      delete payload.parentIdArr;
      const res = this.form.id ? await updateCategory(payload) : await addCategory(payload);
      if (res.code === 200) {
        this.$message.success('保存成功');
        this.dialogVisible = false;
        this.loadTree();
      } else {
        this.$message.error(res.msg || '保存失败');
      }
    },
    async handleRequired(data) {
      const newVal = data.isRequired === 1 ? 0 : 1;
      const res = await updateCategory({ id: data.id, isRequired: newVal });
      if (res.code === 200) { this.$message.success('已更新'); this.loadTree(); }
    },
    async handleDelete(data) {
      await this.$confirm('确认删除「' + data.name + '」? 子目录和文档必须先清空', '提示');
      const res = await delCategory(data.id);
      if (res.code === 200) { this.$message.success('已删除'); this.loadTree(); }
      else this.$message.error(res.msg || '删除失败');
    },
    async handleDrop(draggingNode, dropNode, dropType) {
      // 简化为后端单条更新
      const payload = [{
        id: draggingNode.data.id,
        parentId: dropNode.data.id === undefined ? 0 : dropNode.data.id,
        orderNum: dropType === 'before' ? 0 : 9999
      }];
      try {
        await orderCategory(payload);
        this.loadTree();
      } catch (e) {
        this.$message.error('排序失败: ' + (e.message || ''));
      }
    }
  }
};
</script>

<style scoped>
.custom-tree-node { flex: 1; display: flex; align-items: center; justify-content: space-between; padding-right: 8px; width: 100%; }
.empty-tip { color: #999; text-align: center; padding: 30px; }
</style>
