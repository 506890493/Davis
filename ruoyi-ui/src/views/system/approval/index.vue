<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="申请编号" prop="applyNo">
        <el-input
          v-model="queryParams.applyNo"
          placeholder="请输入申请编号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="申请人ID (sys_user)" prop="applicantId">
        <el-input
          v-model="queryParams.applicantId"
          placeholder="请输入申请人ID (sys_user)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="关联原合同ID" prop="contractId">
        <el-input
          v-model="queryParams.contractId"
          placeholder="请输入关联原合同ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="关联任务ID" prop="taskId">
        <el-input
          v-model="queryParams.taskId"
          placeholder="请输入关联任务ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="审批人ID" prop="approverId">
        <el-input
          v-model="queryParams.approverId"
          placeholder="请输入审批人ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="审批时间" prop="approvalTime">
        <el-date-picker clearable
          v-model="queryParams.approvalTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择审批时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:approval:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:approval:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:approval:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:approval:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="approvalList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="审批ID" align="center" prop="approvalId" />
      <el-table-column label="申请编号" align="center" prop="applyNo" />
      <el-table-column label="申请人ID (sys_user)" align="center" prop="applicantId" />
      <el-table-column label="关联原合同ID" align="center" prop="contractId" />
      <el-table-column label="关联任务ID" align="center" prop="taskId" />
      <el-table-column label="审批类型" align="center" prop="approvalType" />
      <el-table-column label="申请内容快照" align="center" prop="contentSnapshot" />
      <el-table-column label="审批状态" align="center" prop="status" />
      <el-table-column label="审批人ID" align="center" prop="approverId" />
      <el-table-column label="审批时间" align="center" prop="approvalTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.approvalTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审批意见" align="center" prop="approvalMsg" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:approval:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:approval:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改业务审批对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="申请编号" prop="applyNo">
          <el-input v-model="form.applyNo" placeholder="请输入申请编号" />
        </el-form-item>
        <el-form-item label="申请人ID (sys_user)" prop="applicantId">
          <el-input v-model="form.applicantId" placeholder="请输入申请人ID (sys_user)" />
        </el-form-item>
        <el-form-item label="关联原合同ID" prop="contractId">
          <el-input v-model="form.contractId" placeholder="请输入关联原合同ID" />
        </el-form-item>
        <el-form-item label="关联任务ID" prop="taskId">
          <el-input v-model="form.taskId" placeholder="请输入关联任务ID" />
        </el-form-item>
        <el-form-item label="审批人ID" prop="approverId">
          <el-input v-model="form.approverId" placeholder="请输入审批人ID" />
        </el-form-item>
        <el-form-item label="审批时间" prop="approvalTime">
          <el-date-picker clearable
            v-model="form.approvalTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择审批时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="审批意见" prop="approvalMsg">
          <el-input v-model="form.approvalMsg" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listApproval, getApproval, delApproval, addApproval, updateApproval } from "@/api/system/approval"

export default {
  name: "Approval",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 业务审批表格数据
      approvalList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        applyNo: null,
        applicantId: null,
        contractId: null,
        taskId: null,
        approvalType: null,
        contentSnapshot: null,
        status: null,
        approverId: null,
        approvalTime: null,
        approvalMsg: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        applicantId: [
          { required: true, message: "申请人ID (sys_user)不能为空", trigger: "blur" }
        ],
        approvalType: [
          { required: true, message: "审批类型不能为空", trigger: "change" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询业务审批列表 */
    getList() {
      this.loading = true
      listApproval(this.queryParams).then(response => {
        this.approvalList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        approvalId: null,
        applyNo: null,
        applicantId: null,
        contractId: null,
        taskId: null,
        approvalType: null,
        contentSnapshot: null,
        status: null,
        approverId: null,
        approvalTime: null,
        approvalMsg: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.approvalId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加业务审批"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const approvalId = row.approvalId || this.ids
      getApproval(approvalId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改业务审批"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.approvalId != null) {
            updateApproval(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addApproval(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const approvalIds = row.approvalId || this.ids
      this.$modal.confirm('是否确认删除业务审批编号为"' + approvalIds + '"的数据项？').then(function() {
        return delApproval(approvalIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/approval/export', {
        ...this.queryParams
      }, `approval_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
