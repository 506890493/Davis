<template>
  <div class="app-container">
    <el-page-header @back="cancel" content="修改合同" />
    <el-card class="mt20">
      <el-form ref="form" :model="form" :rules="rules" label-width="140px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="合同类型" prop="contractType">
              <el-radio-group v-model="form.contractType" @change="handleTypeChange">
                <el-radio label="0">代账</el-radio>
                <el-radio label="1">地址租赁</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同编号" prop="contractCode">
              <el-input v-model="form.contractCode" placeholder="请输入合同编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公司名称" prop="contractName">
              <el-input v-model="form.contractName" placeholder="请输入公司名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" clearable filterable style="width: calc(100% - 60px)">
                <el-option
                  v-for="customer in customerList"
                  :key="customer.customerId"
                  :label="customer.customerName"
                  :value="customer.customerId"
                />
              </el-select>
              <el-button icon="el-icon-plus" size="mini" @click="handleAddCustomer" style="margin-left: 5px" />
            </el-form-item>
          </el-col>
        </el-row>

        <template v-if="form.contractType === '0'">
          <el-divider content-position="left">代账信息</el-divider>
          <el-row>
            <el-col :span="12">
              <el-form-item label="统一社会信用代码" prop="creditCode">
                <el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="开始日期" prop="startDate">
                <el-date-picker clearable v-model="form.startDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择开始日期" style="width: 100%"></el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束日期" prop="endDate">
                <el-date-picker clearable v-model="form.endDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择结束日期" style="width: 100%"></el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="纳税人类别" prop="taxType">
                <el-select v-model="form.taxType" placeholder="请选择纳税人类别" style="width: 100%">
                  <el-option label="一般纳税人" value="1"></el-option>
                  <el-option label="小规模纳税人" value="2"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="服务内容" prop="serviceContent">
                <el-select v-model="form.serviceContent" multiple placeholder="请选择服务内容" style="width: 100%">
                  <el-option label="代理记账" value="1"></el-option>
                  <el-option label="税务申报" value="2"></el-option>
                  <el-option label="发票代开" value="3"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="合同金额" prop="amount">
                <el-input v-model="form.amount" placeholder="请输入合同金额" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <template v-if="form.contractType === '1'">
          <el-divider content-position="left">地址租赁信息</el-divider>
          <el-row>
            <el-col :span="12">
              <el-form-item label="地址省市区" prop="areaCode">
                <area-cascader v-model="form.areaCode" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="详细地址" prop="rentalAddress">
                <el-input v-model="form.rentalAddress" placeholder="请输入详细地址" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="开始日期" prop="startDate">
                <el-date-picker clearable v-model="form.startDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择开始日期" style="width: 100%"></el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束日期" prop="endDate">
                <el-date-picker clearable v-model="form.endDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择结束日期" style="width: 100%"></el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="租金金额" prop="rentAmount">
                <el-input v-model="form.rentAmount" placeholder="请输入租金金额" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="押金金额" prop="depositAmount">
                <el-input v-model="form.depositAmount" placeholder="请输入押金金额" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="地址面积(平米)" prop="areaSize">
                <el-input v-model="form.areaSize" placeholder="请输入地址面积" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <el-form-item>
          <el-button type="primary" @click="submitForm">保存变更</el-button>
          <el-button @click="cancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 新增客户对话框 -->
    <el-dialog title="新增客户" :visible.sync="customerOpen" width="500px" append-to-body>
      <el-form ref="customerForm" :model="customerForm" :rules="customerRules" label-width="80px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="customerForm.customerName" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="客户类型" prop="customerType">
          <el-radio-group v-model="customerForm.customerType">
            <el-radio label="个人">个人</el-radio>
            <el-radio label="企业">企业</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="customerForm.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="customerForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="contactEmail">
          <el-input v-model="customerForm.contactEmail" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="customerForm.address" placeholder="请输入地址" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitCustomer">确 定</el-button>
        <el-button @click="customerOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getContract, updateContract } from "@/api/system/contract";
import { listCustomer, addCustomer } from "@/api/system/customer";
import AreaCascader from "@/components/AreaCascader";

export default {
  name: "ContractEditPage",
  components: { AreaCascader },
  data() {
    return {
      customerList: [],
      customerOpen: false,
      customerForm: {},
      customerRules: {
        customerName: [{ required: true, message: "客户名称不能为空", trigger: "blur" }]
      },
      form: {
        contractId: null,
        contractType: "0",
        contractCode: null,
        contractName: null,
        customerId: null,
        creditCode: null,
        startDate: null,
        endDate: null,
        taxType: null,
        serviceContent: [],
        amount: null,
        areaCode: null,
        rentalAddress: null,
        rentAmount: null,
        depositAmount: null,
        areaSize: null
      },
      rules: {
        contractType: [
          { required: true, message: "合同类型不能为空", trigger: "change" }
        ],
        contractCode: [
          { required: true, message: "合同编号不能为空", trigger: "blur" }
        ],
        contractName: [
          { required: true, message: "公司名称不能为空", trigger: "blur" }
        ],
        customerId: [
          { required: true, message: "关联客户不能为空", trigger: "change" }
        ],
        startDate: [
          { required: true, message: "开始日期不能为空", trigger: "blur" }
        ],
        endDate: [
          { required: true, message: "结束日期不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getCustomerList();
    const contractId = this.$route.params.contractId;
    if (contractId) {
      this.getDetail(contractId);
    }
  },
  methods: {
    getCustomerList() {
      listCustomer({ pageNum: 1, pageSize: 9999 }).then(response => {
        this.customerList = response.data.rows || [];
      });
    },
    getDetail(contractId) {
      getContract(contractId).then(response => {
        this.form = response.data;
        if (this.form.serviceContent && typeof this.form.serviceContent === 'string') {
          this.form.serviceContent = this.form.serviceContent.split(',');
        } else if (!this.form.serviceContent) {
          this.form.serviceContent = [];
        }
      });
    },
    handleTypeChange(val) {
      this.$refs.form.clearValidate();
    },
    cancel() {
      this.$tab.closePage();
    },
    handleAddCustomer() {
      this.customerForm = {};
      this.customerOpen = true;
    },
    submitCustomer() {
      this.$refs["customerForm"].validate(valid => {
        if (valid) {
          addCustomer(this.customerForm).then(response => {
            this.$modal.msgSuccess("新增成功");
            this.customerOpen = false;
            listCustomer({ pageNum: 1, pageSize: 100 }).then(res => {
              this.customerList = res.data.rows || [];
              this.form.customerId = response.data;
            });
          });
        }
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          let submitData = JSON.parse(JSON.stringify(this.form));
          if (Array.isArray(submitData.serviceContent)) {
            submitData.serviceContent = submitData.serviceContent.join(',');
          }
          updateContract(submitData).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.$tab.closeOpenPage({ path: '/contract/accounting' });
          });
        }
      });
    }
  }
};
</script>
<style scoped>
.mt20 {
  margin-top: 20px;
}
</style>
