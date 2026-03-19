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
  </div>
</template>

<script>
import { getContract, updateContract } from "@/api/system/contract";
import AreaCascader from "@/components/AreaCascader";

export default {
  name: "ContractEditPage",
  components: { AreaCascader },
  data() {
    return {
      form: {
        contractId: null,
        contractType: "0",
        contractCode: null,
        contractName: null,
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
    const contractId = this.$route.params.contractId;
    if (contractId) {
      this.getDetail(contractId);
    }
  },
  methods: {
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
