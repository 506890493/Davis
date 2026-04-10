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
      <el-form-item label="原金额" prop="originalAmount" v-if="showAmount">
        <el-input
          v-model="queryParams.originalAmount"
          placeholder="请输入原金额"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="当前协商金额" prop="currentAmount" v-if="showAmount">
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
<!--      <el-table-column label="任务ID" align="center" prop="taskId" />-->
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
      <el-table-column label="原金额" align="center" prop="originalAmount" v-if="showAmount" />
      <el-table-column
        label="当前协商金额"
        align="center"
        prop="currentAmount"
        v-if="showAmount"
      />
      <el-table-column
        label="实际收款金额"
        align="center"
        prop="actualAmount"
        v-if="showAmount"
      />
      <el-table-column
        label="收款备注"
        align="center"
        prop="receiveRemark"
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
          <!-- Admin buttons -->
          <template v-if="isAdmin">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-s-promotion"
              v-if="scope.row.status === '3'"
              @click="handleRedispatch(scope.row)"
              v-hasPermi="['system:task:edit']"
              >重新派发</el-button
            >
            <el-button
              size="mini"
              type="text"
              icon="el-icon-s-check"
              v-if="scope.row.status === '2' && scope.row.taskType === '3'"
              @click="handleConfirmTerm(scope.row)"
              v-hasPermi="['system:task:edit']"
              >审批终止</el-button
            >
          </template>

          <!-- Accountant buttons -->
          <template v-if="isAccountant">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-video-play"
              v-if="scope.row.status === '0'"
              @click="handleStart(scope.row)"
              v-hasPermi="['system:task:edit']"
              >开始处理</el-button
            >
            <el-button
              size="mini"
              type="text"
              icon="el-icon-document-checked"
              v-if="scope.row.status === '1' && scope.row.taskType === '2'"
              @click="handleCompleteRenewal(scope.row)"
              v-hasPermi="['system:task:edit']"
              >完成续签</el-button
            >
            <el-button
              size="mini"
              type="text"
              icon="el-icon-back"
              v-if="scope.row.status === '1'"
              @click="handleReturn(scope.row)"
              v-hasPermi="['system:task:edit']"
              >退回(讲价)</el-button
            >
            <el-button
              size="mini"
              type="text"
              icon="el-icon-close"
              v-if="scope.row.status === '1'"
              @click="handleRequestTerm(scope.row)"
              v-hasPermi="['system:task:edit']"
              >申请终止</el-button
            >
          </template>

          <!-- Common buttons -->
          <el-button
            size="mini"
            type="text"
            icon="el-icon-time"
            @click="handleViewHistory(scope.row)"
            v-hasPermi="['system:task:query']"
            >操作历史</el-button
          >
          <el-button
            size="mini"
            type="text"
            icon="el-icon-check"
            v-if="scope.row.status === '1' && scope.row.taskType === '1'"
            @click="handlePayment(scope.row)"
            v-hasPermi="['system:task:edit']"
            >确认收款</el-button
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
        <el-form-item label="原金额" prop="originalAmount" v-if="showAmount">
          <el-input v-model="form.originalAmount" placeholder="请输入原金额" />
        </el-form-item>
        <el-form-item label="当前协商金额" prop="currentAmount" v-if="showAmount">
          <el-input
            v-model="form.currentAmount"
            placeholder="请输入当前协商金额"
          />
        </el-form-item>
        <el-form-item label="执行人ID (关联sys_user)" prop="assignedTo">
          <el-select v-model="form.assignedTo" placeholder="请选择执行会计" filterable @focus="loadAccountants" style="width: 100%">
            <el-option
              v-for="user in accountantList"
              :key="user.userId"
              :label="user.nickName"
              :value="user.userId"
            ></el-option>
          </el-select>
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
        <el-form-item label="合同金额" prop="amount" v-if="showAmount">
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
    <!-- 退回(讲价)对话框 -->
    <el-dialog title="退回(讲价)" :visible.sync="returnOpen" width="600px" append-to-body>
      <el-form ref="returnForm" :model="returnForm" label-width="120px">
        <el-form-item label="原金额" v-if="showReturnAmount">
          <el-input v-model="returnForm.originalAmount" disabled />
        </el-form-item>
        <el-form-item label="调整金额" prop="adjustAmount" v-if="showReturnAmount">
          <el-input-number v-model="returnForm.adjustAmount" :min="-999999" :precision="2" :step="100" @change="calcAfterAmount" />
        </el-form-item>
        <el-form-item label="调整后价格" v-if="showReturnAmount">
          <el-input v-model="returnForm.afterAmount" disabled />
        </el-form-item>
        <el-form-item label="退回原因" prop="remark">
          <el-input v-model="returnForm.remark" type="textarea" placeholder="请输入退回原因" />
        </el-form-item>
        <el-form-item label="附件上传">
          <el-upload
            ref="returnUpload"
            :action="upload.url"
            :headers="upload.headers"
            :file-list="returnFileList"
            :auto-upload="false"
            :on-change="handleReturnFileChange"
          >
            <el-button size="small" type="primary" icon="el-icon-upload2">选择文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitReturn">确 定</el-button>
        <el-button @click="returnOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 重新派发对话框 -->
    <el-dialog title="重新派发" :visible.sync="redispatchOpen" width="500px" append-to-body>
      <el-form ref="redispatchForm" :model="redispatchForm" label-width="120px">
        <el-form-item label="退回原因">
          <el-input v-model="redispatchForm.remark" type="textarea" disabled />
        </el-form-item>
        <el-form-item label="原金额" v-if="showAmount">
          <el-input v-model="redispatchForm.originalAmount" disabled />
        </el-form-item>
        <el-form-item label="客户期望金额" v-if="showAmount">
          <el-input v-model="redispatchForm.currentAmount" disabled />
        </el-form-item>
        <el-form-item label="修改后金额" prop="newAmount" v-if="showAmount">
          <el-input-number v-model="redispatchForm.newAmount" :min="0" :precision="2" :step="100" />
        </el-form-item>
        <el-form-item label="分配会计" prop="assigneeId">
          <el-select v-model="redispatchForm.assigneeId" placeholder="请选择分配会计">
            <el-option
              v-for="user in assignableUsers"
              :key="user.userId"
              :label="user.nickName"
              :value="user.userId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker
            v-model="redispatchForm.deadline"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择截止日期"
          ></el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitRedispatch">确 定</el-button>
        <el-button @click="redispatchOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 申请终止对话框 -->
    <el-dialog title="申请终止" :visible.sync="requestTermOpen" width="500px" append-to-body>
      <el-form ref="requestTermForm" :model="requestTermForm" label-width="100px">
        <el-form-item label="终止原因" prop="remark">
          <el-input v-model="requestTermForm.remark" type="textarea" placeholder="请输入终止原因" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitRequestTerm">确 定</el-button>
        <el-button @click="requestTermOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 确认终止对话框 -->
    <el-dialog title="审批终止" :visible.sync="confirmTermOpen" width="500px" append-to-body>
      <el-form ref="confirmTermForm" :model="confirmTermForm" label-width="100px">
        <el-form-item label="终止原因">
          <el-input v-model="confirmTermForm.remark" type="textarea" disabled />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="success" @click="submitConfirmTerm(true)">同 意</el-button>
        <el-button type="danger" @click="submitConfirmTerm(false)">拒 绝</el-button>
        <el-button @click="confirmTermOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 完成续签对话框 -->
    <el-dialog title="完成续签" :visible.sync="completeRenewalOpen" width="500px" append-to-body>
      <el-form ref="completeRenewalForm" :model="completeRenewalForm" label-width="100px">
        <el-form-item label="续签新金额" prop="newAmount" v-if="showAmount">
          <el-input-number v-model="completeRenewalForm.newAmount" :min="0" :precision="2" :step="100" />
        </el-form-item>
        <el-form-item label="续签期限" prop="newPeriod">
          <el-date-picker
            v-model="completeRenewalForm.newPeriod"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitCompleteRenewal">确 定</el-button>
        <el-button @click="completeRenewalOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 确认收款对话框 -->
    <el-dialog title="确认收款" :visible.sync="paymentDialogOpen" width="500px" append-to-body>
      <el-form ref="paymentForm" :model="paymentForm" :rules="paymentRules" label-width="100px">
        <el-form-item label="合同名称">
          <el-input v-model="paymentForm.contractName" disabled />
        </el-form-item>
        <el-form-item label="原金额" v-if="showAmount">
          <el-input v-model="paymentForm.originalAmount" disabled />
        </el-form-item>
        <el-form-item label="协商金额" v-if="showAmount">
          <el-input v-model="paymentForm.currentAmount" disabled />
        </el-form-item>
        <el-form-item label="实际收款" prop="actualAmount" v-if="showAmount">
          <el-input-number v-model="paymentForm.actualAmount" :min="0" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="收款备注" prop="receiveRemark">
          <el-input v-model="paymentForm.receiveRemark" type="textarea" placeholder="请输入收款备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitPayment">确 定</el-button>
        <el-button @click="paymentDialogOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 操作历史对话框 -->
    <el-dialog title="操作历史" :visible.sync="historyOpen" width="700px" append-to-body>
      <el-table :data="historyList" v-loading="loading">
        <el-table-column label="操作时间" prop="createTime" width="160">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作人" prop="operatorName" width="100"/>
        <el-table-column label="操作类型" prop="actionType" width="100">
          <template slot-scope="scope">
            <span v-if="scope.row.actionType === '0'">创建</span>
            <span v-else-if="scope.row.actionType === '1'">开始</span>
            <span v-else-if="scope.row.actionType === '2'">完成</span>
            <span v-else-if="scope.row.actionType === '3'">终止</span>
            <span v-else-if="scope.row.actionType === '4'">分配</span>
            <span v-else-if="scope.row.actionType === '5'">重新分配</span>
            <span v-else>{{ scope.row.actionType }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态变更" width="120">
          <template slot-scope="scope">
            <span v-if="scope.row.beforeStatus">{{ getStatusLabel(scope.row.beforeStatus) }}</span>
            <span v-else>-</span>
            <span> → </span>
            <span v-if="scope.row.afterStatus">{{ getStatusLabel(scope.row.afterStatus) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" :show-overflow-tooltip="true"/>
      </el-table>
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
  returnToAdmin,
  redispatch,
  requestTermination,
  confirmTermination,
  completeRenewal,
  getAssignableUsers,
  confirmPayment,
  historyTaskLog
} from "@/api/system/task";
import { getContract } from "@/api/system/contract";
import { getToken } from "@/utils/auth";
import axios from "axios";

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
        status: '0,1,3,4',
      },
      // 表单参数
      form: {},
      renewOpen: false,
      renewForm: {},

      // 退回(讲价)
      returnOpen: false,
      returnForm: {},
      returnFileList: [],

      // 重新派发
      redispatchOpen: false,
      redispatchForm: {},
      assignableUsers: [],
      accountantList: [],

      // 申请终止
      requestTermOpen: false,
      requestTermForm: {},

      // 确认终止
      confirmTermOpen: false,
      confirmTermForm: {},

      // 完成续签
      completeRenewalOpen: false,
      completeRenewalForm: {},

      // 确认收款
      paymentDialogOpen: false,
      paymentForm: {},
      paymentRules: {
        actualAmount: [{ required: true, message: "请输入实际收款金额", trigger: "blur" }]
      },
      // 操作历史
      historyOpen: false,
      historyList: [],
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
        headers: { Authorization: "Bearer " + getToken() },
        url: process.env.VUE_APP_BASE_API + "/common/uploads",
        fileList: [],
      },
    };
  },
  created() {
    this.getList();
  },
computed: {
isAdmin() {
return this.$store.getters.roles.includes('admin');
},
isManager() {
return this.$store.getters.roles.includes('manager');
},
isAccountant() {
return this.$store.getters.roles.includes('accountant');
    },
    canAssignTask() {
      return this.isAdmin || this.isManager;
    },
    showAmount() {
      const roles = this.$store.getters.roles || [];
      if (roles.includes("admin")) {
        return true;
      }
      if (roles.includes("accountant") || roles.includes("sales")) {
        return false;
      }
      return true;
    },
    showReturnAmount() {
      return true;
    }
  },
  methods: {
    /** 查询任务管理列表 */
    getList() {
      this.loading = true;
      if (this.isAccountant && !this.isAdmin) {
        this.queryParams.assignedTo = this.$store.getters.id;
      }
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
    /** 退回按钮操作 */
    handleReturn(row) {
      this.returnForm = {
        taskId: row.taskId,
        originalAmount: row.originalAmount,
        adjustAmount: 0,
        afterAmount: row.originalAmount,
        remark: null
      };
      this.returnFileList = [];
      this.returnOpen = true;
    },
    calcAfterAmount() {
      this.returnForm.afterAmount = (this.returnForm.originalAmount || 0) + (this.returnForm.adjustAmount || 0);
    },
    handleReturnFileChange(file, fileList) {
      this.returnFileList = fileList.slice(-1);
    },
    async submitReturn() {
      if (this.returnFileList.length > 0 && this.returnFileList[0].raw) {
        const formData = new FormData()
        formData.append('files', this.returnFileList[0].raw)
        try {
          const response = await axios({
            method: 'post',
            url: this.upload.url,
            headers: {
              ...this.upload.headers,
              'Content-Type': 'multipart/form-data'
            },
            data: formData
          })
          const res = response.data
          if (res.code === 200) {
            const fileNames = res.fileNames || res.fileName || ''
            this.returnForm.attachment = fileNames.split(',')[0]
            this.$modal.msgSuccess('附件上传成功')
          } else {
            this.$modal.msgError(res.msg || '附件上传失败')
            return
          }
        } catch (e) {
          console.error('Upload error:', e)
          this.$modal.msgError('附件上传失败: ' + (e.message || '未知错误'))
          return
        }
      }
      returnToAdmin(this.returnForm).then(response => {
        console.log('returnToAdmin response:', response)
        console.log('returnForm.attachment:', this.returnForm.attachment)
        this.$modal.msgSuccess("退回成功");
        this.returnOpen = false;
        this.getList();
      });
    },

    /** 重新派发按钮操作 */
    handleRedispatch(row) {
      this.redispatchForm = {
        taskId: row.taskId,
        remark: row.remark,
        originalAmount: row.originalAmount,
        currentAmount: row.currentAmount,
        newAmount: row.currentAmount || row.originalAmount,
        assigneeId: null,
        deadline: null
      };
      getAssignableUsers().then(response => {
        this.assignableUsers = response.data;
        this.redispatchOpen = true;
      }).catch(() => {
        this.assignableUsers = [];
      });
    },
    submitRedispatch() {
      redispatch(this.redispatchForm).then(response => {
        this.$modal.msgSuccess("重新派发成功");
        this.redispatchOpen = false;
        this.getList();
      });
    },
    loadAccountants() {
      if (!this.canAssignTask) return;
      if (this.accountantList.length === 0) {
        getAssignableUsers().then(response => {
          this.accountantList = response.data || [];
        }).catch(() => {
          this.accountantList = [];
        });
      }
    },

    /** 申请终止按钮操作 */
    handleRequestTerm(row) {
      this.requestTermForm = {
        taskId: row.taskId,
        remark: null
      };
      this.requestTermOpen = true;
    },
    submitRequestTerm() {
      requestTermination(this.requestTermForm).then(response => {
        this.$modal.msgSuccess("申请终止成功");
        this.requestTermOpen = false;
        this.getList();
      });
    },

    /** 审批终止按钮操作 */
    handleConfirmTerm(row) {
      this.confirmTermForm = {
        taskId: row.taskId,
        remark: row.remark
      };
      this.confirmTermOpen = true;
    },
    submitConfirmTerm(approved) {
      confirmTermination({ taskId: this.confirmTermForm.taskId, approved: approved }).then(response => {
        this.$modal.msgSuccess("审批完成");
        this.confirmTermOpen = false;
        this.getList();
      });
    },

    /** 完成续签按钮操作 */
    handleCompleteRenewal(row) {
      this.completeRenewalForm = {
        taskId: row.taskId,
        newAmount: row.originalAmount,
        newPeriod: []
      };
      this.completeRenewalOpen = true;
    },
    submitCompleteRenewal() {
      const data = {
        taskId: this.completeRenewalForm.taskId,
        newAmount: this.completeRenewalForm.newAmount,
        startDate: this.completeRenewalForm.newPeriod ? this.completeRenewalForm.newPeriod[0] : null,
        endDate: this.completeRenewalForm.newPeriod ? this.completeRenewalForm.newPeriod[1] : null
      };
      completeRenewal(data).then(response => {
        this.$modal.msgSuccess("完成续签成功");
        this.completeRenewalOpen = false;
        this.getList();
      });
    },

    /** 确认收款按钮操作 */
    handlePayment(row) {
      this.paymentForm = {
        taskId: row.taskId,
        contractName: row.taskTitle.replace('催收任务: ', ''),
        originalAmount: row.originalAmount,
        currentAmount: row.currentAmount || row.originalAmount,
        actualAmount: row.currentAmount || row.originalAmount,
        receiveRemark: ''
      };
      this.paymentDialogOpen = true;
    },
    /** 查看操作历史 */
    handleViewHistory(row) {
      historyTaskLog({ taskId: row.taskId }).then(res => {
        this.historyList = res.rows || [];
        this.historyOpen = true;
      });
    },
    submitPayment() {
      this.$refs.paymentForm.validate(valid => {
        if (valid) {
          const data = {
            taskId: this.paymentForm.taskId,
            actualAmount: this.paymentForm.actualAmount,
            receiveRemark: this.paymentForm.receiveRemark
          };
          confirmPayment(data).then(response => {
            this.$modal.msgSuccess("收款确认成功");
            this.paymentDialogOpen = false;
            this.getList();
          });
        }
      });
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
    getStatusLabel(status) {
      const dict = this.dict.type.cms_task_status || [];
      const item = dict.find(d => d.value === status);
      return item ? item.label : status;
    },
  },
};
</script>
