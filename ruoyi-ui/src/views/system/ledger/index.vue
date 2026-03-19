<template>
  <div class="app-container">
    <div v-if="checkRole(['admin'])">
      <!-- Date Range Picker and Export Button -->
      <el-form :inline="true" class="demo-form-inline">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            @change="handleQuery"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
          <el-button type="warning" icon="el-icon-download" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>

      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <!-- Tab 1: 总账概览 -->
        <el-tab-pane label="总账概览" name="summary">
          <el-row :gutter="20" class="panel-group">
            <el-col :span="6" class="card-panel-col">
              <el-card shadow="hover">
                <div class="card-panel-text">合同总数</div>
                <div class="card-panel-num">{{ summaryData.totalContracts || 0 }}</div>
              </el-card>
            </el-col>
            <el-col :span="6" class="card-panel-col">
              <el-card shadow="hover">
                <div class="card-panel-text">代账合同金额总计</div>
                <div class="card-panel-num">{{ summaryData.totalAgencyAmount || 0 }}</div>
              </el-card>
            </el-col>
            <el-col :span="6" class="card-panel-col">
              <el-card shadow="hover">
                <div class="card-panel-text">地址租赁金额总计</div>
                <div class="card-panel-num">{{ summaryData.totalAddressAmount || 0 }}</div>
              </el-card>
            </el-col>
            <el-col :span="6" class="card-panel-col">
              <el-card shadow="hover">
                <div class="card-panel-text">即将到期合同数</div>
                <div class="card-panel-num">{{ summaryData.expiringContracts || 0 }}</div>
              </el-card>
            </el-col>
          </el-row>

          <el-table v-loading="loading" :data="summaryData.contractList" style="width: 100%; margin-top: 20px;">
            <el-table-column prop="contractName" label="合同名称" />
            <el-table-column prop="contractType" label="类型">
              <template slot-scope="scope">
                <span v-if="scope.row.contractType === '1'">代账</span>
                <span v-else-if="scope.row.contractType === '2'">地址</span>
                <span v-else>{{ scope.row.contractType }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" />
            <el-table-column prop="startDate" label="签订日期" width="180">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.startDate, '{y}-{m}-{d}') }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="endDate" label="到期日期" width="180">
              <template slot-scope="scope">
                <span>{{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}</span>
              </template>
            </el-table-column>
          </el-table>
          <pagination
            v-show="summaryData.total>0"
            :total="summaryData.total"
            :page.sync="queryParams.pageNum"
            :limit.sync="queryParams.pageSize"
            @pagination="getSummary"
          />
        </el-tab-pane>

        <!-- Tab 2: 按人汇总 -->
        <el-tab-pane label="按人汇总" name="byPerson">
          <el-table v-loading="loading" :data="personData" style="width: 100%">
            <el-table-column prop="accountantName" label="会计姓名" />
            <el-table-column prop="contractCount" label="负责合同数" />
            <el-table-column prop="totalAmount" label="合同总金额" />
          </el-table>
        </el-tab-pane>

        <!-- Tab 3: 趋势分析 -->
        <el-tab-pane label="趋势分析" name="trend">
          <div v-loading="loading" id="trendChart" style="height: 400px; width: 100%;"></div>
        </el-tab-pane>
      </el-tabs>
    </div>
    <div v-else>
      <el-alert title="无权限访问" type="error" description="您没有权限访问此页面，仅管理员可访问。" show-icon :closable="false" />
    </div>
  </div>
</template>

<script>
import { getLedgerSummary, getLedgerByPerson, getLedgerTrend } from "@/api/system/ledger";
import { listContract } from "@/api/system/contract";
import { getDashboardStats } from "@/api/system/dashboard";
import { checkRole } from "@/utils/permission";
import * as echarts from 'echarts';

export default {
  name: "Ledger",
  data() {
    return {
      activeTab: 'summary',
      dateRange: [],
      loading: false,
      summaryData: {
        totalContracts: 0,
        totalAgencyAmount: 0,
        totalAddressAmount: 0,
        expiringContracts: 0,
        contractList: [],
        total: 0
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10
      },
      personData: [],
      trendData: {
        months: [],
        agencyIncome: [],
        addressIncome: []
      },
      chartInstance: null
    };
  },
  created() {
    if (this.checkRole(['admin'])) {
      this.getSummary();
    }
  },
  mounted() {
    window.addEventListener('resize', this.resizeChart);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeChart);
    if (this.chartInstance) {
      this.chartInstance.dispose();
      this.chartInstance = null;
    }
  },
  methods: {
    checkRole,
    handleQuery() {
      if (this.activeTab === 'summary') {
        this.getSummary();
      } else if (this.activeTab === 'byPerson') {
        this.getByPerson();
      } else if (this.activeTab === 'trend') {
        this.getTrend();
      }
    },
    handleTabClick(tab) {
      if (tab.name === 'summary' && !this.summaryData.contractList.length) {
        this.getSummary();
      } else if (tab.name === 'byPerson' && !this.personData.length) {
        this.getByPerson();
      } else if (tab.name === 'trend') {
        this.$nextTick(() => {
          if (!this.chartInstance) {
            this.initChart();
          }
          this.getTrend();
        });
      }
    },
    getQueryParams() {
      const params = {};
      if (this.dateRange && this.dateRange.length === 2) {
        params.beginDate = this.dateRange[0];
        params.endDate = this.dateRange[1];
      }
      return params;
    },
    getSummary() {
      this.loading = true;
      const params = this.getQueryParams();
      
      // 1. Get Ledger Summary
      getLedgerSummary(params).then(response => {
        const data = response.data || {};
        this.summaryData.totalContracts = data.totalContracts || 0;
        this.summaryData.totalAgencyAmount = data.dazhangAmount || 0;
        this.summaryData.totalAddressAmount = data.addressAmount || 0;
      });

      // 2. Get Dashboard Stats for expiring contracts
      getDashboardStats().then(response => {
        const data = response.data || {};
        this.summaryData.expiringContracts = data.expiringContractCount || 0;
      });

      // 3. Get Contract List
      const listParams = {
        ...this.queryParams,
        ...params
      };
      listContract(listParams).then(response => {
        this.summaryData.contractList = response.rows || [];
        this.summaryData.total = response.total || 0;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    getByPerson() {
      this.loading = true;
      getLedgerByPerson(this.getQueryParams()).then(response => {
        this.personData = response.data || [];
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    getTrend() {
      this.loading = true;
      getLedgerTrend(this.getQueryParams()).then(response => {
        this.trendData = response.data || {
          months: [],
          agencyIncome: [],
          addressIncome: []
        };
        this.updateChart();
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    initChart() {
      const chartDom = document.getElementById('trendChart');
      if (chartDom) {
        this.chartInstance = echarts.init(chartDom);
      }
    },
    updateChart() {
      if (!this.chartInstance) return;
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['代账收入', '地址租赁收入']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.trendData.months || []
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '代账收入',
            type: 'line',
            data: this.trendData.agencyIncome || []
          },
          {
            name: '地址租赁收入',
            type: 'line',
            data: this.trendData.addressIncome || []
          }
        ]
      };
      this.chartInstance.setOption(option);
    },
    resizeChart() {
      if (this.chartInstance) {
        this.chartInstance.resize();
      }
    },
    handleExport() {
      this.$modal.confirm('是否确认导出总账报表数据项？').then(() => {
        this.download('system/ledger/export', {
          ...this.getQueryParams()
        }, `ledger_${new Date().getTime()}.xlsx`)
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.panel-group {
  margin-top: 18px;
}
.card-panel-col {
  margin-bottom: 32px;
}
.card-panel-text {
  line-height: 18px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 16px;
  margin-bottom: 12px;
}
.card-panel-num {
  font-size: 20px;
  font-weight: bold;
}
</style>
