<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="客户名称" prop="customerName">
        <el-input
          v-model="queryParams.customerName"
          placeholder="请输入客户名称"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系电话" prop="contactPhone">
        <el-input
          v-model="queryParams.contactPhone"
          placeholder="请输入联系电话"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="客户类型" prop="customerType">
        <el-select v-model="queryParams.customerType" placeholder="请选择" clearable size="small">
          <el-option label="公司" value="1" />
          <el-option label="个体户" value="2" />
          <el-option label="合伙企业" value="3" />
          <el-option label="民办非" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="客户状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable size="small">
          <el-option label="正常" value="0" />
          <el-option label="非正常" value="1" />
        </el-select>
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
          v-hasPermi="['system:customer:add']"
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
          v-hasPermi="['system:customer:edit']"
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
          v-hasPermi="['system:customer:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:customer:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="customerList" @selection-change="handleSelectionChange" @expand-change="handleExpandChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column type="expand">
        <template slot-scope="scope">
          <div v-if="contractMap[scope.row.customerId]" class="contract-expand">
            <div class="contract-section">
              <div class="section-title">代账合同</div>
              <el-table :data="getContractsByType(scope.row.customerId, '1')" size="mini" border v-if="getContractsByType(scope.row.customerId, '1').length > 0">
                <el-table-column prop="contractCode" label="合同编号" width="150" />
                <el-table-column prop="contractName" label="公司名称" :show-overflow-tooltip="true" />
                <el-table-column prop="amount" label="金额" width="100" />
                <el-table-column label="期限" width="200">
                  <template slot-scope="s">
                    {{ s.row.startDate }} ~ {{ s.row.endDate }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="80">
                  <template slot-scope="s">
                    <el-tag v-if="s.row.status === '0'" type="warning">进行中</el-tag>
                    <el-tag v-else-if="s.row.status === '1'" type="success">已完成</el-tag>
                    <el-tag v-else type="info">未知</el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <div v-else class="empty-tip">暂无代账合同</div>
            </div>
            <div class="contract-section">
              <div class="section-title">地址合同</div>
              <el-table :data="getContractsByType(scope.row.customerId, '2')" size="mini" border v-if="getContractsByType(scope.row.customerId, '2').length > 0">
                <el-table-column prop="contractCode" label="合同编号" width="150" />
                <el-table-column prop="contractName" label="公司名称" :show-overflow-tooltip="true" />
                <el-table-column prop="rentAmount" label="租金" width="100" />
                <el-table-column label="期限" width="200">
                  <template slot-scope="s">
                    {{ s.row.startDate }} ~ {{ s.row.endDate }}
                  </template>
                </el-table-column>
                <el-table-column prop="rentalAddress" label="地址" :show-overflow-tooltip="true" />
                <el-table-column prop="status" label="状态" width="80">
                  <template slot-scope="s">
                    <el-tag v-if="s.row.status === '0'" type="warning">进行中</el-tag>
                    <el-tag v-else-if="s.row.status === '1'" type="success">已完成</el-tag>
                    <el-tag v-else type="info">未知</el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <div v-else class="empty-tip">暂无地址合同</div>
            </div>
          </div>
          <div v-else class="empty-tip">暂无关联合同</div>
        </template>
      </el-table-column>
      <el-table-column label="客户名称" align="center" key="customerName" prop="customerName" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <el-link type="primary" @click="handleDetail(scope.row)">{{ scope.row.customerName }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="客户类型" align="center" key="customerType" prop="customerType">
        <template slot-scope="scope">
          <span>{{ scope.row.customerType || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="联系人" align="center" key="contactPerson" prop="contactPerson" />
      <el-table-column label="联系电话" align="center" key="contactPhone" prop="contactPhone" width="120" />
      <el-table-column label="邮箱" align="center" key="contactEmail" prop="contactEmail" :show-overflow-tooltip="true" />
      <el-table-column label="归属销售" align="center" key="ownerName" prop="ownerName" />
      <el-table-column label="状态" align="center" key="status" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === '0'" type="primary">正常</el-tag>
          <el-tag v-else-if="scope.row.status === '1'" type="danger">非正常</el-tag>
          <el-tag v-else type="info">-</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
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

    <!-- 添加或修改客户对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="form.customerName" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="客户类型" prop="customerType">
          <el-radio-group v-model="form.customerType">
            <el-radio :label="'1'">公司</el-radio>
            <el-radio :label="'2'">个体户</el-radio>
            <el-radio :label="'3'">合伙企业</el-radio>
            <el-radio :label="'4'">民办非</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="contactEmail">
          <el-input v-model="form.contactEmail" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="客户状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">非正常</el-radio>
          </el-radio-group>
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
import { listCustomer, getCustomer, addCustomer, updateCustomer, delCustomer, exportCustomer } from "@/api/system/customer";
import { listContract } from "@/api/system/contract";
import { getDicts } from "@/api/system/dict/data";

export default {
  name: "Customer",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      title: "",
      open: false,
      customerList: [],
      customerListSelections: [],
      expandedRowKeys: [],
      contractMap: {},
      dicts: ['cms_customer_type', 'cms_customer_status'],
      // 本地兜底：组件内直接调 getDicts 加载，不依赖 dict mixin
      customerTypeOptions: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        customerName: null,
        contactPhone: null,
        customerType: null,
        status: null
      },
      form: {},
      rules: {
        customerName: [
          { required: true, message: "客户名称不能为空", trigger: "blur" }
        ],
        customerType: [
          { required: true, message: "请选择客户类型", trigger: "change" }
        ]
      }
    };
  },
  created() {
    this.loadCustomerTypeDict();
    this.getList();
  },
  methods: {
    loadCustomerTypeDict() {
      getDicts('cms_customer_type').then(res => {
        if (res.code === 200) this.customerTypeOptions = res.data || [];
      }).catch(() => {
        this.customerTypeOptions = [];
      });
    },
    getList() {
      this.loading = true;
      listCustomer(this.queryParams).then(response => {
        this.customerList = response.data.rows;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        customerName: null,
        contactPhone: null,
        customerType: null,
        status: null
      };
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.customerId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleExpandChange(row, expanded) {
      if (expanded && !this.contractMap[row.customerId]) {
        listContract({ customerId: row.customerId }).then(res => {
          this.$set(this.contractMap, row.customerId, res.rows || []);
        });
      }
    },
    getContractsByType(customerId, type) {
      const list = this.contractMap[customerId] || [];
      return list.filter(c => c.contractType === type);
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加客户";
    },
    handleUpdate(row) {
      this.reset();
      const customerId = row.customerId || this.ids;
      getCustomer(customerId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改客户";
      });
    },
    handleDetail(row) {
      this.$router.push({ path: '/system/customer/detail/' + row.customerId });
    },
    handleDelete(row) {
      const customerIds = row.customerId || this.ids;
      this.$modal.confirm('是否确认删除客户编号为"' + customerIds + '"的数据项？').then(function() {
        return delCustomer(customerIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      this.$modal.confirm("是否确认导出所有客户数据项？").then(() => {
        this.download('/system/customer/export', {
          ...this.queryParams
        }, `customer_${new Date().getTime()}.xlsx`);
      }).catch(() => {});
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.customerId != undefined) {
            updateCustomer(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addCustomer(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    cancel() {
      this.open = false;
      this.title = "";
    },
    reset() {
      this.form = {
        customerId: null,
        customerName: null,
        customerType: null,
        contactPerson: null,
        contactPhone: null,
        contactEmail: null,
        address: null,
        remark: null,
        status: '0'
      };
      this.resetForm("form");
    }
  }
};
</script>

<style scoped>
.contract-expand {
  padding: 10px 20px;
}
.contract-section {
  margin-bottom: 20px;
}
.section-title {
  font-size: 14px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}
.empty-tip {
  color: #909399;
  font-size: 14px;
  text-align: center;
  padding: 20px;
}
</style>
