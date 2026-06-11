<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :sm="24" :lg="24">
        <blockquote class="text-warning" style="font-size: 14px">
          欢迎来到达维斯管理系统
        </blockquote>
      </el-col>
    </el-row>

    <kb-required-banner style="margin: 0 0 16px 0" />

    <div v-loading="loading" style="min-height: 400px">
      <!-- 管理员/经理视图 -->
      <div v-if="stats.roleType === 'admin' || stats.roleType === 'manager'">
        <el-row :gutter="20" class="panel-group">
          <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
            <div class="card-panel">
              <div class="card-panel-icon-wrapper icon-money">
                <i class="el-icon-money card-panel-icon" />
              </div>
              <div class="card-panel-description">
                <div class="card-panel-text">本月应完成金额</div>
                <count-to :start-val="0" :end-val="stats.monthTargetAmount" :duration="2600" class="card-panel-num" />
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
            <div class="card-panel">
              <div class="card-panel-icon-wrapper icon-shopping">
                <i class="el-icon-s-claim card-panel-icon" />
              </div>
              <div class="card-panel-description">
                <div class="card-panel-text">本月实际完成金额</div>
                <count-to :start-val="0" :end-val="stats.monthActualAmount" :duration="2600" class="card-panel-num" />
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
            <div class="card-panel">
              <div class="card-panel-icon-wrapper icon-document">
                <i class="el-icon-document card-panel-icon" />
              </div>
              <div class="card-panel-description">
                <div class="card-panel-text">合同总数</div>
                <count-to :start-val="0" :end-val="stats.totalContracts || 0" :duration="2000" class="card-panel-num" />
              </div>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="panel-group">
          <el-col :xs="12" :sm="12" :lg="4" class="card-panel-col">
            <div class="card-panel">
              <div class="card-panel-icon-wrapper icon-people">
                <i class="el-icon-user card-panel-icon" />
              </div>
              <div class="card-panel-description">
                <div class="card-panel-text">客户总数</div>
                <count-to :start-val="0" :end-val="stats.totalCustomers || 0" :duration="2000" class="card-panel-num" />
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="12" :lg="4" class="card-panel-col">
            <div class="card-panel">
              <div class="card-panel-icon-wrapper icon-message">
                <i class="el-icon-s-finance card-panel-icon" />
              </div>
              <div class="card-panel-description">
                <div class="card-panel-text">代账客户</div>
                <count-to :start-val="0" :end-val="stats.accountingCustomerCount || 0" :duration="2000" class="card-panel-num" />
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="12" :lg="4" class="card-panel-col">
            <div class="card-panel">
              <div class="card-panel-icon-wrapper icon-shopping">
                <i class="el-icon-office-building card-panel-icon" />
              </div>
              <div class="card-panel-description">
                <div class="card-panel-text">租赁客户</div>
                <count-to :start-val="0" :end-val="stats.rentalCustomerCount || 0" :duration="2000" class="card-panel-num" />
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div v-else-if="stats.roleType === 'accountant'">
        <el-row :gutter="20" class="panel-group">
          <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
            <div class="card-panel">
              <div class="card-panel-icon-wrapper icon-money">
                <i class="el-icon-money card-panel-icon" />
              </div>
              <div class="card-panel-description">
                <div class="card-panel-text">本月应收金额</div>
                <count-to :start-val="0" :end-val="stats.totalReceivable" :duration="2000" class="card-panel-num" />
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
            <div class="card-panel">
              <div class="card-panel-icon-wrapper icon-shopping">
                <i class="el-icon-wallet card-panel-icon" />
              </div>
              <div class="card-panel-description">
                <div class="card-panel-text">本月已收金额</div>
                <count-to :start-val="0" :end-val="stats.totalReceived" :duration="2000" class="card-panel-num" />
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
            <div class="card-panel">
              <div class="card-panel-icon-wrapper icon-people">
                <i class="el-icon-user card-panel-icon" />
              </div>
              <div class="card-panel-description">
                <div class="card-panel-text">代账应收家数</div>
                <count-to :start-val="0" :end-val="stats.bookkeepingTargetCount" :duration="2000" class="card-panel-num" />
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="12" :lg="6" class="card-panel-col">
            <div class="card-panel">
              <div class="card-panel-icon-wrapper icon-message">
                <i class="el-icon-check card-panel-icon" />
              </div>
              <div class="card-panel-description">
                <div class="card-panel-text">代账已收家数</div>
                <count-to :start-val="0" :end-val="stats.bookkeepingDoneCount" :duration="2000" class="card-panel-num" />
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 销售视图 -->
      <div v-else-if="stats.roleType === 'sales'">
        <el-row :gutter="20" class="panel-group">
          <el-col :xs="12" :sm="12" :lg="8" class="card-panel-col">
            <div class="card-panel">
              <div class="card-panel-icon-wrapper icon-people">
                <i class="el-icon-user-solid card-panel-icon" />
              </div>
              <div class="card-panel-description">
                <div class="card-panel-text">我的客户总数</div>
                <count-to :start-val="0" :end-val="stats.myCustomerCount" :duration="2000" class="card-panel-num" />
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="12" :lg="8" class="card-panel-col">
            <div class="card-panel">
              <div class="card-panel-icon-wrapper icon-money">
                <i class="el-icon-s-flag card-panel-icon" />
              </div>
              <div class="card-panel-description">
                <div class="card-panel-text">本月目标金额</div>
                <count-to :start-val="0" :end-val="stats.monthTargetAmount" :duration="2000" class="card-panel-num" />
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="12" :lg="8" class="card-panel-col">
            <div class="card-panel">
              <div class="card-panel-icon-wrapper icon-shopping">
                <i class="el-icon-trophy card-panel-icon" />
              </div>
              <div class="card-panel-description">
                <div class="card-panel-text">本月完成金额</div>
                <count-to :start-val="0" :end-val="stats.monthActualAmount" :duration="2000" class="card-panel-num" />
              </div>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="box-card">
              <div slot="header" class="clearfix">
                <span>我的所有客户</span>
              </div>
              <el-table :data="stats.myCustomers" style="width: 100%" height="400">
                <el-table-column prop="contractName" label="客户/合同名称" />
                <el-table-column prop="contactPerson" label="联系人" width="100" />
                <el-table-column prop="contactPhone" label="电话" width="120" />
                <el-table-column prop="createTime" label="创建时间" width="160">
                  <template slot-scope="scope">
                    <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 默认/空状态 -->
      <div v-else class="text-center" style="padding: 50px;">
        <p>暂无数据或无权限查看仪表盘</p>
      </div>
    </div>

    <!-- 创建催缴任务对话框 -->
    <el-dialog title="创建催缴任务" :visible.sync="taskDialogVisible" width="500px" append-to-body>
      <el-form ref="taskForm" :model="taskForm" :rules="taskRules" label-width="100px">
        <el-form-item label="合同名称">
          <el-input v-model="taskForm.contractName" disabled />
        </el-form-item>
        <el-form-item label="合同金额">
          <el-input v-model="taskForm.amount" disabled />
        </el-form-item>
        <el-form-item label="分配给" prop="assignedTo">
          <el-select v-model="taskForm.assignedTo" placeholder="请选择会计" style="width: 100%">
            <el-option v-for="user in accountantList" :key="user.userId" :label="user.nickName" :value="user.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker v-model="taskForm.deadline" type="date" placeholder="请选择截止时间" value-format="yyyy-MM-dd" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="taskForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTask">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getDashboardStats } from "@/api/system/dashboard";
import { createCollectionTask } from "@/api/system/task";
import { listUser } from "@/api/system/user";
import CountTo from 'vue-count-to'
import KbRequiredBanner from './system/kb/portal/KbRequiredBanner.vue';

export default {
  name: "Index",
  components: {
    CountTo,
    KbRequiredBanner
  },
  dicts: ['cms_contract_type'],
  data() {
    return {
      loading: true,
      stats: {
        roleType: '',
        monthTargetAmount: 0,
        monthActualAmount: 0,
        expiringContractCount: 0,
        expiringContracts: [],
        totalReceivable: 0,
        totalReceived: 0,
        bookkeepingTargetCount: 0,
        bookkeepingDoneCount: 0,
        myCustomerCount: 0,
        myCustomers: [],
        newCustomerCount: 0,
        totalContracts: 0,
        totalCustomers: 0,
        accountingCustomerCount: 0,
        rentalCustomerCount: 0
      },
      taskDialogVisible: false,
      taskForm: {
        contractId: null,
        contractName: '',
        amount: 0,
        assignedTo: null,
        deadline: '',
        remark: ''
      },
      taskRules: {
        assignedTo: [{ required: true, message: "请选择分配对象", trigger: "change" }],
        deadline: [{ required: true, message: "请选择截止时间", trigger: "change" }]
      },
      accountantList: []
    };
  },
  created() {
    this.getStats();
  },
  methods: {
    getStats() {
      this.loading = true;
      getDashboardStats().then(response => {
        if (response.data) {
          this.stats = response.data;
        }
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    handleCreateTask(row) {
      this.taskForm = {
        contractId: row.contractId,
        contractName: row.contractName,
        amount: row.amount,
        assignedTo: null,
        deadline: row.endDate,
        remark: ''
      };
      this.loadAccountantList();
      this.taskDialogVisible = true;
    },
    loadAccountantList() {
      listUser({ pageNum: 1, pageSize: 100 }).then(response => {
        this.accountantList = response.rows.filter(user => {
          return user.roles && user.roles.some(role => role.roleKey === 'accountant');
        });
      });
    },
    submitTask() {
      this.$refs.taskForm.validate(valid => {
        if (valid) {
          const data = {
            contractId: this.taskForm.contractId,
            assignedTo: this.taskForm.assignedTo,
            deadline: this.taskForm.deadline,
            remark: this.taskForm.remark,
            priority: '高'
          };
          createCollectionTask(data).then(response => {
            this.$modal.msgSuccess("创建成功");
            this.taskDialogVisible = false;
            this.getStats();
          });
        }
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.panel-group {
  margin-top: 18px;

  .card-panel-col {
    margin-bottom: 32px;
  }

  .card-panel {
    height: 108px;
    cursor: pointer;
    font-size: 12px;
    position: relative;
    overflow: hidden;
    color: #666;
    background: #fff;
    box-shadow: 4px 4px 40px rgba(0, 0, 0, .05);
    border-color: rgba(0, 0, 0, .05);
    transition: all 0.3s;

    &:hover {
      .card-panel-icon-wrapper {
        color: #fff;
      }

      .icon-people {
        background: #40c9c6;
      }

      .icon-message {
        background: #36a3f7;
      }

      .icon-money {
        background: #f4516c;
      }

      .icon-shopping {
        background: #34bfa3;
      }

      .icon-warning {
        background: #ffba00;
      }

      .icon-document {
        background: #9c27b0;
      }
    }

    .icon-people {
      color: #40c9c6;
    }

    .icon-message {
      color: #36a3f7;
    }

    .icon-money {
      color: #f4516c;
    }

    .icon-shopping {
      color: #34bfa3;
    }

    .icon-warning {
      color: #ffba00;
    }

    .icon-document {
      color: #9c27b0;
    }

    .card-panel-icon-wrapper {
      float: left;
      margin: 14px 0 0 14px;
      padding: 16px;
      transition: all 0.38s ease-out;
      border-radius: 6px;
    }

    .card-panel-icon {
      float: left;
      font-size: 48px;
    }

    .card-panel-description {
      float: right;
      font-weight: bold;
      margin: 26px 26px 26px 0;

      .card-panel-text {
        line-height: 18px;
        color: rgba(0, 0, 0, 0.45);
        font-size: 16px;
        margin-bottom: 12px;
      }

      .card-panel-num {
        font-size: 20px;
      }
    }
  }
}

.box-card {
  margin-bottom: 20px;
}
</style>
