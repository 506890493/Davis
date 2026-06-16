<template>
  <div class="app-container">
    <el-page-header @back="cancel" content="修改合同" />
    <el-card class="mt20">
      <el-form ref="form" :model="form" :rules="rules" label-width="140px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="合同类型" prop="contractType">
              <el-radio-group v-model="form.contractType" @change="handleTypeChange">
                <el-radio label="1">代账</el-radio>
                <el-radio label="2">地址租赁</el-radio>
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
              <el-select v-model="form.customerId" placeholder="请选择客户" clearable filterable style="width: calc(100% - 60px)" @change="handleCustomerChange">
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

        <template v-if="form.contractType === '1'">
          <el-divider content-position="left">代账信息</el-divider>
          <el-row>
            <el-col :span="12">
              <el-form-item label="统一社会信用代码" prop="creditCode">
                <el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="法人" prop="legalPerson">
                <el-input v-model="form.legalPerson" placeholder="请输入法人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系人" prop="contactPerson">
                <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" prop="contactPhone">
                <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系邮箱" prop="contactEmail">
                <el-input v-model="form.contactEmail" placeholder="请输入联系邮箱" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="收费标准" prop="amount">
                <el-input v-model="form.amount" placeholder="请输入收费标准" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="付款周期" prop="paymentCycle">
                <el-select v-model="form.paymentCycle" placeholder="请选择付款周期" style="width: 100%">
                  <el-option
                    v-for="dict in dict.type.cms_pay_cycle"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="收款日期" prop="paymentDate">
                <el-date-picker clearable v-model="form.paymentDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择收款日期" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="收款方式" prop="paymentMethod">
                <el-select v-model="form.paymentMethod" placeholder="请选择收款方式" style="width: 100%">
                  <el-option
                    v-for="dict in dict.type.cms_pay_method"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  />
                </el-select>
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
              <el-form-item label="成立日期" prop="establishmentDate">
                <el-date-picker clearable v-model="form.establishmentDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择成立日期" style="width: 100%" />
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
          </el-row>
        </template>

        <template v-if="form.contractType === '2'">
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
            <el-col :span="12">
              <el-form-item label="是否已出租" prop="isRented">
                <el-radio-group v-model="form.isRented">
                  <el-radio label="0">否</el-radio>
                  <el-radio label="1">是</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="利润" prop="profit">
                <el-input v-model="form.profit" placeholder="请输入利润" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系人" prop="contactPerson">
                <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" prop="contactPhone">
                <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <el-divider content-position="left">其他信息</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker clearable v-model="form.startDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择开始日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker clearable v-model="form.endDate" type="date" value-format="yyyy-MM-dd" placeholder="请选择结束日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="会计" prop="ownerId">
              <el-select v-model="form.ownerId" placeholder="请选择会计" clearable @change="handleAccountantChange" style="width: 100%">
                <el-option
                  v-for="user in userList"
                  :key="user.userId"
                  :label="user.nickName"
                  :value="user.userId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="催交状态" prop="reminderStatus">
              <el-radio-group v-model="form.reminderStatus">
                <el-radio
                  v-for="dict in dict.type.cms_reminder_status"
                  :key="dict.value"
                  :label="dict.value"
                >{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="附件" prop="annex">
              <el-upload
                ref="upload"
                multiple
                :limit="5"
                accept=".jpg,.png,.pdf,.doc,.docx,.xls,.xlsx"
                action="#"
                :http-request="() => {}"
                :headers="upload.headers"
                :file-list="upload.fileList"
                :on-change="handleChange"
                :on-remove="handleRemove"
                :auto-upload="false"
              >
                <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
                <el-button style="margin-left: 10px" size="small" type="success" :loading="upload.isUploading" @click="submitUpload">上传到服务器</el-button>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>

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
import { listUser } from "@/api/system/user";
import { getToken } from "@/utils/auth";
import axios from "axios";
import AreaCascader from "@/components/AreaCascader";

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('联系电话不能为空'));
  } else if (!/^\d+$/.test(value)) {
    callback(new Error('联系电话只能输入数字'));
  } else if (value.length !== 11) {
    callback(new Error('联系电话必须为11位'));
  } else {
    callback();
  }
};

const validateAmount = (rule, value, callback) => {
  if (!value) {
    callback(new Error('收费标准不能为空'));
  } else if (!/^\d+(\.\d+)?$/.test(value)) {
    callback(new Error('收费标准只能输入数值'));
  } else {
    callback();
  }
};

export default {
  name: "ContractEditPage",
  components: { AreaCascader },
  dicts: ["cms_pay_cycle", "cms_pay_method", "cms_reminder_status"],
  data() {
    return {
      customerList: [],
      customerOpen: false,
      customerForm: {},
      customerRules: {
        customerName: [{ required: true, message: "客户名称不能为空", trigger: "blur" }]
      },
      userList: [],
      form: {
        contractId: null,
        contractType: "1",
        contractCode: null,
        contractName: null,
        customerId: null,
        creditCode: null,
        legalPerson: null,
        contactPerson: null,
        contactPhone: null,
        contactEmail: null,
        amount: null,
        paymentCycle: null,
        paymentDate: null,
        paymentMethod: null,
        taxType: null,
        establishmentDate: null,
        serviceContent: [],
        areaCode: null,
        rentalAddress: null,
        rentAmount: null,
        depositAmount: null,
        areaSize: null,
        isRented: null,
        profit: null,
        startDate: null,
        endDate: null,
        ownerId: null,
        deptId: null,
        reminderStatus: null,
        remark: null,
        annex: null
      },
      rules: {
        contractType: [
          { required: true, message: "合同类型不能为空", trigger: "change" }
        ],
        contractCode: [
          { required: false, message: "合同编号不能为空", trigger: "blur" }
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
        ],
        contactPhone: [
          { required: true, validator: validatePhone, trigger: "blur" }
        ],
        amount: [
          { required: true, validator: validateAmount, trigger: "blur" }
        ],
        paymentDate: [
          { required: true, message: "收款日期不能为空", trigger: "change" }
        ],
        paymentMethod: [
          { required: true, message: "收款方式不能为空", trigger: "change" }
        ],
        ownerId: [
          { required: true, message: "会计不能为空", trigger: "change" }
        ]
      },
      upload: {
        isUploading: false,
        headers: { Authorization: "Bearer " + getToken() },
        url: process.env.VUE_APP_BASE_API + "/common/uploads",
        fileList: []
      }
    };
  },
  created() {
    this.getCustomerList();
    this.getUserList();
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
    getUserList() {
      listUser().then(response => {
        this.userList = response.rows || [];
      });
    },
    handleTypeChange(val) {
      this.$refs.form.clearValidate();
    },
    handleCustomerChange(customerId) {
      if (!customerId) {
        this.form.contactPerson = null;
        this.form.contactPhone = null;
        this.form.contactEmail = null;
        return;
      }
      const customer = this.customerList.find(c => c.customerId === customerId);
      if (customer) {
        this.form.contactPerson = customer.contactPerson || null;
        this.form.contactPhone = customer.contactPhone || null;
        this.form.contactEmail = customer.contactEmail || null;
      }
    },
    handleAccountantChange(userId) {
      if (!userId) {
        this.form.deptId = null;
        return;
      }
      const user = this.userList.find(u => u.userId === userId);
      if (user) {
        this.form.deptId = user.deptId;
      }
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
    getDetail(contractId) {
      getContract(contractId).then(response => {
        this.form = response.data;
        // 处理serviceContent回显
        if (this.form.serviceContent && typeof this.form.serviceContent === 'string') {
          this.form.serviceContent = this.form.serviceContent.split(',');
        } else if (!this.form.serviceContent) {
          this.form.serviceContent = [];
        }
        // 处理附件回显
        this.upload.fileList = [];
        if (this.form.annex) {
          try {
            let annexObj = this.form.annex;
            if (typeof annexObj === "string") {
              annexObj = JSON.parse(annexObj);
              this.form.annex = annexObj;
            }
            if (annexObj.urls && annexObj.urls.length > 0) {
              this.upload.fileList = annexObj.urls.map((url, index) => ({
                name: (annexObj.originalFilenames && annexObj.originalFilenames[index]) ? annexObj.originalFilenames[index] : ("附件" + (index + 1)),
                url: url,
                status: "success"
              }));
            }
          } catch (e) {
            console.error("附件JSON解析失败:", e);
            this.form.annex = {};
          }
        }
      });
    },
    handleChange(file, fileList) {
      this.upload.fileList = fileList;
    },
    handleRemove(file, fileList) {
      this.upload.fileList = fileList;
    },
    submitUpload() {
      if (this.upload.fileList.length === 0) {
        this.$message.warning("请先选择文件");
        return;
      }
      const newFiles = this.upload.fileList.filter(item => item.raw);
      if (newFiles.length === 0) {
        this.$message.warning("所有文件已上传");
        return;
      }
      this.upload.isUploading = true;
      const formData = new FormData();
      newFiles.forEach(item => {
        formData.append("files", item.raw);
      });
      axios({
        method: "post",
        url: this.upload.url,
        headers: {
          ...this.upload.headers,
          "Content-Type": "multipart/form-data"
        },
        data: formData
      }).then(response => {
        const res = response.data;
        if (res.code === 200) {
          this.$message.success("上传成功");
          const serverUrls = res.urls.split(",");
          newFiles.forEach((item, index) => {
            if (serverUrls[index]) {
              item.url = serverUrls[index];
            }
          });
          this.upload.fileList.forEach(item => {
            item.status = "success";
          });
        } else {
          this.$message.error(res.msg || "上传失败");
          this.upload.fileList.forEach(item => {
            item.status = "exception";
          });
        }
        this.upload.isUploading = false;
      }).catch(() => {
        this.$message.error("服务器错误");
        this.upload.isUploading = false;
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          let submitData = JSON.parse(JSON.stringify(this.form));
          if (Array.isArray(submitData.serviceContent)) {
            submitData.serviceContent = submitData.serviceContent.join(',');
          }
          // 处理附件
          if (this.upload.fileList && this.upload.fileList.length > 0) {
            const urls = this.upload.fileList.map(f => f.url);
            const originalFilenames = this.upload.fileList.map(f => f.name);
            submitData.annex = JSON.stringify({
              urls: urls,
              originalFilenames: originalFilenames
            });
          } else {
            submitData.annex = "";
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
