<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      size="small"
      :inline="true"
      v-show="showSearch"
      label-width="68px"
    >
      <el-form-item label="任务标题" prop="taskTitle">
        <el-input
          v-model="queryParams.taskTitle"
          placeholder="请输入任务标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="合同编号" prop="contractId">
        <el-input
          v-model="queryParams.contractId"
          placeholder="请输入关联同ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-input
          v-model="queryParams.priority"
          placeholder="请输入优先级"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="原金额" prop="originalAmount">
        <el-input
          v-model="queryParams.originalAmount"
          placeholder="请输入原金额"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="当前协商金额" prop="currentAmount">
        <el-input
          v-model="queryParams.currentAmount"
          placeholder="请输入当前协商金额"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="执行人" prop="assignedToName">
        <el-input
          v-model="queryParams.assignedToName"
          placeholder="请输入执行人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="截止时间" prop="deadline">
        <el-date-picker
          clearable
          v-model="queryParams.deadline"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择截止时间"
        >
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          icon="el-icon-search"
          size="mini"
          @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery"
          >重置</el-button
        >
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
          v-hasPermi="['system:task:add']"
          >新增</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:task:edit']"
          >修改</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:task:remove']"
          >删除</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:task:export']"
          >导出</el-button
        >
      </el-col>
      <right-toolbar
        :showSearch.sync="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="taskList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务ID" align="center" prop="taskId" />
      <el-table-column label="任务标题" align="center" prop="taskTitle" />
      <el-table-column label="合同编号" align="center" prop="contractId" />
      <el-table-column label="任务类型" align="center" prop="taskType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.cms_task_type" :value="scope.row.taskType"/>
        </template>
      </el-table-column>
      <el-table-column label="优先级" align="center" prop="priority">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.cms_task_priority" :value="scope.row.priority"/>
        </template>
      </el-table-column>
      <el-table-column label="原金额" align="center" prop="originalAmount" />
      <el-table-column
        label="当前协商金额"
        align="center"
        prop="currentAmount"
      />
      <el-table-column
        label="执行人"
        align="center"
        prop="assignedToName"
      />
      <el-table-column
        label="截止时间"
        align="center"
        prop="deadline"
        width="180"
      >
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.deadline, "{y}-{m}-{d}") }}</span>
        </template>
      </el-table-column>
      <el-table-column label="任务状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.cms_task_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="任务描述/备注" align="center" prop="remark" />
      <el-table-column
        label="操作"
        align="center"
        class-name="small-padding fixed-width"
        width="200"
      >
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-video-play"
            v-if="scope.row.status === '0'"
            @click="handleStart(scope.row)"
            v-hasPermi="['system:task:edit']"
            >开始</el-button
          >
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:task:edit']"
            >修改</el-button
          >
          <el-button
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleComplete(scope.row)"
            v-hasPermi="['system:task:edit']"
            >完成</el-button
          >
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:task:remove']"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改任务管理对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="任务标题" prop="taskTitle">
          <el-input v-model="form.taskTitle" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="合同编号" prop="contractId">
          <el-input v-model="form.contractId" placeholder="请输入关联同ID" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input v-model="form.priority" placeholder="请输入优先级" />
        </el-form-item>
        <el-form-item label="原金额" prop="originalAmount">
          <el-input v-model="form.originalAmount" placeholder="请输入原金额" />
        </el-form-item>
        <el-form-item label="当前协商金额" prop="currentAmount">
          <el-input
            v-model="form.currentAmount"
            placeholder="请输入当前协商金额"
          />
        </el-form-item>
        <el-form-item label="执行人ID (关联sys_user)" prop="assignedTo">
          <el-input
            v-model="form.assignedTo"
            placeholder="请输入执行人ID (关联sys_user)"
          />
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            clearable
            v-model="form.deadline"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择截止时间"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="删除标志" prop="delFlag">
          <el-input v-model="form.delFlag" placeholder="请输入删除标志" />
        </el-form-item>
        <el-form-item label="任务描述/备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            placeholder="请输入内容"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 续签合同录入对话框 -->
    <el-dialog
      title="续签合同录入"
      :visible.sync="renewOpen"
      width="500px"
      append-to-body
    >
      <el-form ref="renewForm" :model="renewForm" label-width="100px">
        <el-form-item label="公司名称" prop="contractName">
          <el-input
            v-model="renewForm.contractName"
            placeholder="请输入公司名称"
          />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input
            v-model="renewForm.contactPerson"
            placeholder="请输入联系人"
          />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input
            v-model="renewForm.contactPhone"
            placeholder="请输入联系电话"
          />
        </el-form-item>
        <el-form-item label="合同金额" prop="amount">
          <el-input v-model="renewForm.amount" placeholder="请输入合同金额" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            clearable
            v-model="renewForm.startDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择合同开始日期"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            clearable
            v-model="renewForm.endDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择合同结束日期"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="附件" prop="annex">
          <el-upload
            ref="upload"
            multiple
            :limit="3"
            accept=".jpg, .png"
            action="#"
            :http-request="() => {}"
            :file-list="upload.fileList"
            :auto-upload="false"
          >
            <el-button slot="trigger" size="small" type="primary"
              >选取文件</el-button
            >
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitRenew">确 定</el-button>
        <el-button @click="renewOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listTask,
  getTask,
  delTask,
  addTask,
  updateTask,
  completeCollectionTask,
} from "@/api/system/task";
import { getContract } from "@/api/system/contract";

export default {
  name: "Task",
  dicts: ['cms_task_type', 'cms_task_priority', 'cms_task_status'],
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
      // 任务管理表格数据
      taskList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        taskTitle: null,
        contractId: null,
        taskType: null,
        priority: null,
        originalAmount: null,
        currentAmount: null,
        assignedToName: null,
        deadline: null,
        status: null,
      },
      // 表单参数
      form: {},
      renewOpen: false,
      renewForm: {},
      rules: {
        contractName: [
          { required: true, message: "公司名称不能为空", trigger: "blur" },
        ],
        amount: [
          { required: true, message: "合同金额不能为空", trigger: "blur" },
        ],
        startDate: [
          { required: true, message: "开始日期不能为空", trigger: "blur" },
        ],
        endDate: [
          { required: true, message: "结束日期不能为空", trigger: "blur" },
        ],
      },
      upload: {
        fileList: [],
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询任务管理列表 */
    getList() {
      this.loading = true;
      listTask(this.queryParams).then((response) => {
        this.taskList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        taskId: null,
        taskTitle: null,
        contractId: null,
        taskType: null,
        priority: null,
        originalAmount: null,
        currentAmount: null,
        assignedToName: null,
        deadline: null,
        status: null,
        delFlag: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null,
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map((item) => item.taskId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加任务管理";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const taskId = row.taskId || this.ids;
      getTask(taskId).then((response) => {
        this.form = response.data;
        this.open = true;
        this.title = "修改任务管理";
      });
    },
    /** 开始按钮操作 */
    handleStart(row) {
      const task = { taskId: row.taskId, status: '1' };
      updateTask(task).then(response => {
        this.$modal.msgSuccess("任务已开始");
        this.getList();
      });
    },
    /** 完成按钮操作 */
    handleComplete(row) {
      if (row.taskType === "1") {
        getContract(row.sourceContractId).then((response) => {
          this.renewForm = {
            taskId: row.taskId,
            contractName: response.data.contractName,
            contactPerson: response.data.contactPerson,
            contactPhone: response.data.contactPhone,
            amount: null,
            startDate: null,
            endDate: null,
            annex: null,
          };
          this.upload.fileList = [];
          this.renewOpen = true;
        });
      } else {
        // Handle normal task completion
        this.$modal.msgSuccess("任务完成");
      }
    },
    /** 提交续签合同 */
    submitRenew() {
      this.$refs["renewForm"].validate((valid) => {
        if (valid) {
          completeCollectionTask(this.renewForm).then((response) => {
            this.$modal.msgSuccess("续签成功");
            this.renewOpen = false;
            this.getList();
          });
        }
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          if (this.form.taskId != null) {
            updateTask(this.form).then((response) => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addTask(this.form).then((response) => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const taskIds = row.taskId || this.ids;
      this.$modal
        .confirm('是否确认删除任务管理编号为"' + taskIds + '"的数据项？')
        .then(function () {
          return delTask(taskIds);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download(
        "system/task/export",
        {
          ...this.queryParams,
        },
        `task_${new Date().getTime()}.xlsx`
      );
    },
  },
};
</script>
