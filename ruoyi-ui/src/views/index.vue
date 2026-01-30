<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :sm="24" :lg="24">
        <blockquote class="text-warning" style="font-size: 14px">
          欢迎来到达维斯管理系统
        </blockquote>
        <hr />
      </el-col>
    </el-row>

    <!-- 加载状态 -->
    <div v-loading="loading" style="min-height: 400px">
      
      <!-- ================= 管理员视图 ================= -->
      <div v-if="stats.roleType === 'admin'">
        <el-row :gutter="20" class="panel-group">
          <el-col :xs="12" :sm="12" :lg="12" class="card-panel-col">
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
          <el-col :xs="12" :sm="12" :lg="12" class="card-panel-col">
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
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-card class="box-card">
              <div slot="header" class="clearfix">
                <span>本月即将过期合同 ({{ stats.expiringContractCount }})</span>
              </div>
              <el-table :data="stats.expiringContracts" style="width: 100%" height="400">
                <el-table-column prop="contractCode" label="合同编号" width="180" />
                <el-table-column prop="contractName" label="合同名称" />
                <el-table-column prop="endDate" label="到期日期" width="180">
                  <template slot-scope="scope">
                    <span style="color: red">{{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="amount" label="金额" width="120" />
                <el-table-column prop="ownerName" label="归属人" width="120" />
                <el-table-column label="操作" width="100">
                  <template slot-scope="scope">
                    <router-link :to="'/system/contract/index?contractId=' + scope.row.contractId" class="link-type">
                      <span>查看</span>
                    </router-link>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- ================= 会计视图 ================= -->
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

        <el-row>
          <el-col :span="24">
            <el-card class="box-card">
              <div slot="header" class="clearfix">
                <span>本月待处理合同 ({{ stats.expiringContractCount }})</span>
              </div>
              <el-table :data="stats.expiringContracts" style="width: 100%" height="400">
                <el-table-column prop="contractCode" label="合同编号" width="180" />
                <el-table-column prop="contractName" label="合同名称" />
                <el-table-column prop="contractType" label="类型" width="120">
                   <template slot-scope="scope">
                     <dict-tag :options="dict.type.cms_contract_type" :value="scope.row.contractType"/>
                   </template>
                </el-table-column>
                <el-table-column prop="endDate" label="到期/应收日期" width="180">
                  <template slot-scope="scope">
                    <span>{{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="amount" label="金额" width="120" />
                <el-table-column prop="ownerName" label="归属人" width="120" />
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- ================= 销售视图 ================= -->
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
           <el-col :span="12">
            <el-card class="box-card">
              <div slot="header" class="clearfix">
                <span>本月需关注 (即将到期)</span>
              </div>
              <el-table :data="stats.expiringContracts" style="width: 100%" height="400">
                <el-table-column prop="contractName" label="合同名称" />
                <el-table-column prop="endDate" label="到期日期" width="100">
                   <template slot-scope="scope">
                    <span style="color: red">{{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="amount" label="金额" width="100" />
                <el-table-column label="操作" width="80">
                  <template slot-scope="scope">
                    <el-button size="mini" type="text">跟进</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- ================= 默认/空状态 ================= -->
      <div v-else class="text-center">
        <p>暂无数据或无权限查看仪表盘</p>
      </div>
    </div>
  </div>
</template>

<script>
import { getDashboardStats } from "@/api/system/dashboard";
import CountTo from 'vue-count-to'

export default {
  name: "Index",
  components: {
    CountTo
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
        newCustomerCount: 0
      }
    };
  },
  created() {
    this.getStats();
  },
  methods: {
    getStats() {
      this.loading = true;
      getDashboardStats().then(response => {
        if(response.data) {
          this.stats = response.data;
        }
        this.loading = false;
      }).catch(() => {
        this.loading = false;
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
        background: #34bfa3
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
      color: #34bfa3
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
