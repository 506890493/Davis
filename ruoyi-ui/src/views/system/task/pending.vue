<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="任务类型" prop="taskType">
        <el-select v-model="queryParams.taskType" placeholder="请选择任务类型" clearable>
          <el-option label="协商价格" value="PRICE" />
          <el-option label="终止合作" value="TERMINATE" />
        </el-select>
      </el-form-item>
      <el-form-item label="任务标题" prop="taskTitle">
        <el-input v-model="queryParams.taskTitle" placeholder="请输入任务标题" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <span style="font-weight: bold; color: #409EFF;">待审批任务列表</span>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="taskList">
      <el-table-column label="任务标题" align="center" prop="taskTitle" min-width="150" show-overflow-tooltip />
      <el-table-column label="任务类型" align="center" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.taskType === '1'" type="warning">催收</el-tag>
          <el-tag v-else-if="scope.row.taskType === '3'" type="danger">终止</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="原金额" align="center" prop="originalAmount" width="100">
        <template slot-scope="scope">
          {{ scope.row.originalAmount || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="新金额" align="center" prop="currentAmount" width="100">
        <template slot-scope="scope">
          <span v-if="scope.row.currentAmount" style="color: #E6A23C; font-weight: bold;">
            {{ scope.row.currentAmount }}
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="执行人" align="center" prop="assignedToName" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          {{ parseTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleView(scope.row)">查看</el-button>
          <el-button v-if="scope.row.taskType === '1'" size="mini" type="text" @click="handleApprovePrice(scope.row)">同意</el-button>
          <el-button v-if="scope.row.taskType === '1'" size="mini" type="text" style="color: #F56C6C;" @click="handleRejectPrice(scope.row)">拒绝</el-button>
          <el-button v-if="scope.row.taskType === '3'" size="mini" type="text" @click="handleApproveTerminate(scope.row)">同意终止</el-button>
          <el-button v-if="scope.row.taskType === '3'" size="mini" type="text" style="color: #F56C6C;" @click="handleRejectTerminate(scope.row)">拒绝终止</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="请输入拒绝原因" :visible.sync="rejectDialogVisible" width="500px" append-to-body>
      <el-form ref="rejectForm" :model="rejectForm" :rules="rejectRules" label-width="100px">
        <el-form-item label="拒绝原因" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitReject">确 定</el-button>
        <el-button @click="cancelReject">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog title="任务详情" :visible.sync="detailDialogVisible" width="600px" append-to-body>
      <el-form :model="detailData" label-width="120px">
        <el-form-item label="任务标题">{{ detailData.taskTitle }}</el-form-item>
        <el-form-item label="任务类型">{{ detailData.taskType }}</el-form-item>
        <el-form-item label="原金额">{{ detailData.originalAmount }}</el-form-item>
        <el-form-item label="协商金额">{{ detailData.currentAmount || '-' }}</el-form-item>
        <el-form-item label="执行人">{{ detailData.assignedToName }}</el-form-item>
        <el-form-item label="截止时间">{{ detailData.deadline }}</el-form-item>
        <el-form-item label="任务状态">{{ detailData.status }}</el-form-item>
        <el-form-item label="备注">{{ detailData.remark }}</el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPendingTask, redispatch, rejectPrice, confirmTermination, getTask } from "@/api/system/task";

export default {
  name: "PendingTask",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      taskList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        taskType: undefined,
        taskTitle: undefined,
        status: '2'
      },
      rejectDialogVisible: false,
      detailDialogVisible: false,
      detailData: {},
      rejectForm: {
        taskId: undefined,
        taskType: undefined,
        reason: ''
      },
      rejectRules: {
        reason: [{ required: true, message: "请输入拒绝原因", trigger: "blur" }]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listPendingTask(this.queryParams).then(response => {
        this.taskList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleView(row) {
      getTask(row.taskId).then(response => {
        this.detailData = response.data || {};
        this.detailDialogVisible = true;
      });
    },
    handleApprovePrice(row) {
      this.$confirm('确认同意此协商价格吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        redispatch({ taskId: row.taskId, currentAmount: row.currentAmount }).then(() => {
          this.$modal.msgSuccess("已同意协商价格");
          this.getList();
        });
      });
    },
    handleRejectPrice(row) {
      this.rejectForm = { taskId: row.taskId, taskType: '1', reason: '' };
      this.rejectDialogVisible = true;
    },
    handleApproveTerminate(row) {
      this.$confirm('确认同意终止合作吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        confirmTermination({ taskId: row.taskId, approved: true }).then(() => {
          this.$modal.msgSuccess("已同意终止");
          this.getList();
        });
      });
    },
    handleRejectTerminate(row) {
      this.rejectForm = { taskId: row.taskId, taskType: '3', reason: '' };
      this.rejectDialogVisible = true;
    },
    submitReject() {
      this.$refs.rejectForm.validate(valid => {
        if (valid) {
          if (this.rejectForm.taskType === '1') {
            rejectPrice({ taskId: this.rejectForm.taskId, remark: this.rejectForm.reason }).then(() => {
              this.$modal.msgSuccess("已拒绝协商价格");
              this.rejectDialogVisible = false;
              this.getList();
            });
          } else {
            confirmTermination({ taskId: this.rejectForm.taskId, approved: false, rejectReason: this.rejectForm.reason }).then(() => {
              this.$modal.msgSuccess("已拒绝终止");
              this.rejectDialogVisible = false;
              this.getList();
            });
          }
        }
      });
    },
    cancelReject() {
      this.rejectDialogVisible = false;
      this.rejectForm = { taskId: undefined, taskType: undefined, reason: '' };
    }
  }
};
</script>