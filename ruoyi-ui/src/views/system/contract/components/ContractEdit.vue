<template>
  <div class="app-container">
    <el-page-header @back="cancel" :content="isEdit ? '修改合同' : '新增合同'" />
    <el-card class="mt20">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
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
            <el-form-item label="合同类型" prop="contractType">
              <el-select
                v-model="form.contractType"
                placeholder="请选择合同类型"
                @change="syncDialogType"
              >
                <el-option
                  v-for="dict in dict.type.cms_contract_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in dict.type.cms_contract_status"
                  :key="dict.value"
                  :label="dict.value"
                >{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <template v-if="dialogType === accountingType">
          <el-divider content-position="left">代账信息</el-divider>
          <el-row>
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
                <el-select v-model="form.paymentCycle" placeholder="请选择付款周期">
                  <el-option
                    v-for="dict in dict.type.cms_pay_cycle"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="收款日期" prop="paymentDate">
                <el-date-picker
                  clearable
                  v-model="form.paymentDate"
                  type="date"
                  value-format="yyyy-MM-dd"
                  placeholder="请选择收款日期"
                >
                </el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="收款方式" prop="paymentMethod">
                <el-select v-model="form.paymentMethod" placeholder="请选择收款方式">
                  <el-option
                    v-for="dict in dict.type.cms_pay_method"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="税务类型" prop="taxType">
                <el-select v-model="form.taxType" placeholder="请选择税务类型">
                  <el-option
                    v-for="dict in dict.type.cms_tax_type"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="成立日期" prop="establishmentDate">
                <el-date-picker
                  clearable
                  v-model="form.establishmentDate"
                  type="date"
                  value-format="yyyy-MM-dd"
                  placeholder="请选择成立日期"
                >
                </el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <template v-else-if="dialogType === rentType">
          <el-divider content-position="left">地址出租信息</el-divider>
          <el-row>
            <el-col :span="12">
              <el-form-item label="租赁地址" prop="rentalAddress">
                <el-input v-model="form.rentalAddress" placeholder="请输入租赁地址" />
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
              <el-date-picker
                clearable
                v-model="form.startDate"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择合同开始日期"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                clearable
                v-model="form.endDate"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择合同结束日期"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="会计" prop="ownerId">
              <el-select v-model="form.ownerId" placeholder="请选择会计" clearable @change="handleAccountantChange">
                <el-option
                  v-for="user in userList"
                  :key="user.userId"
                  :label="user.nickName"
                  :value="user.userId"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="form.remark"
                type="textarea"
                placeholder="请输入内容"
              />
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
            <el-form-item label="附件列表" prop="annex">
              <el-upload
                ref="upload"
                multiple
                :limit="3"
                accept=".jpg, .png"
                action="#"
                :http-request="() => {}"
                :headers="upload.headers"
                :file-list="upload.fileList"
                :on-change="handleChange"
                :on-remove="handleRemove"
                :auto-upload="false"
              >
                <el-button slot="trigger" size="small" type="primary"
                  >选取文件</el-button
                >
                <el-button
                  style="margin-left: 10px"
                  size="small"
                  type="success"
                  :loading="upload.isUploading"
                  @click="submitUpload"
                  >上传到服务器</el-button
                >
                <div slot="tip" class="el-upload__tip">
                  只能上传jpg/png文件，且不超过500kb
                </div>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" @click="submitForm">{{ isEdit ? '保存变更' : '立即创建' }}</el-button>
          <el-button @click="cancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { getContract, addContract, updateContract } from "@/api/system/contract";
import { listUser } from "@/api/system/user";
import { getToken } from "@/utils/auth";
import axios from "axios";

export default {
  name: "ContractForm",
  dicts: [
    "cms_contract_type",
    "cms_tax_type",
    "cms_pay_cycle",
    "cms_pay_method",
    "cms_reminder_status",
    "cms_contract_status",
  ],
  data() {
    return {
      isEdit: false,
      form: {
        status: '0',
        contractType: null,
      },
      rules: {
        contractCode: [
          { required: true, message: "合同编号不能为空", trigger: "blur" },
        ],
        contractName: [
          { required: true, message: "公司名称不能为空", trigger: "blur" },
        ],
        contractType: [
          { required: true, message: "合同类型不能为空", trigger: "change" },
        ],
        startDate: [
          { required: true, message: "合同开始日期不能为空", trigger: "blur" },
        ],
        endDate: [
          { required: true, message: "合同结束日期不能为空", trigger: "blur" },
        ],
      },
      dialogType: null,
      accountingType: "Accounting",
      rentType: "Rent",
      dictAccounting: "1",
      dictRent: "2",
      userList: [],
      upload: {
        isUploading: false,
        headers: { Authorization: "Bearer " + getToken() },
        url: process.env.VUE_APP_BASE_API + "/common/uploads",
        fileList: [],
      },
    };
  },
  created() {
    this.getUserList();
    const contractId = this.$route.params.contractId;
    if (contractId) {
      this.isEdit = true;
      this.getDetail(contractId);
    } else {
      this.isEdit = false;
      this.reset();
    }
  },
  methods: {
    getUserList() {
      listUser().then(response => {
        this.userList = response.rows;
      });
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
    reset() {
      this.form = {
        contractId: null,
        contractCode: null,
        contractName: null,
        contractType: null,
        legalPerson: null,
        contactPerson: null,
        contactPhone: null,
        contactEmail: null,
        amount: null,
        paymentCycle: null,
        paymentDate: null,
        paymentMethod: null,
        startDate: null,
        endDate: null,
        taxType: null,
        establishmentDate: null,
        rentalAddress: null,
        isRented: null,
        profit: null,
        ownerId: null,
        deptId: null,
        status: "0",
        delFlag: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null,
        reminderStatus: null,
        annex: null,
      };
      this.upload.fileList = [];
      this.dialogType = null;
    },
    getDetail(contractId) {
      getContract(contractId).then((response) => {
        this.form = response.data;
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
              this.upload.fileList = annexObj.urls.map((url, index) => {
                return {
                  name:
                    annexObj.originalFilenames &&
                    annexObj.originalFilenames[index]
                      ? annexObj.originalFilenames[index]
                      : "附件" + (index + 1),
                  url: url,
                  status: "success",
                };
              });
            }
          } catch (e) {
            console.error("附件JSON解析失败:", e);
            this.form.annex = {};
          }
        }
        this.syncDialogType(this.form.contractType);
      });
    },
    syncDialogType(val) {
      this.dialogType = this.mapDictToView(val);
    },
    mapDictToView(val) {
      if (val === this.dictAccounting) return this.accountingType;
      if (val === this.dictRent) return this.rentType;
      return null;
    },
    cancel() {
      this.$tab.closePage();
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
      this.upload.isUploading = true;
      const formData = new FormData();
      this.upload.fileList.forEach((item) => {
        if (item.raw) {
          formData.append("files", item.raw);
        }
      });
      axios({
        method: "post",
        url: this.upload.url,
        headers: {
          ...this.upload.headers,
          "Content-Type": "multipart/form-data",
        },
        data: formData,
      })
        .then((response) => {
          const res = response.data;
          if (res.code === 200) {
            this.$message.success("上传成功");
            this.form.annex = {
              urls: res.urls.split(","),
              newFileNames: res.newFileNames.split(","),
              fileNames: res.fileNames.split(","),
              originalFilenames: res.originalFilenames.split(","),
            };
            this.upload.fileList.forEach((item) => {
              item.status = "success";
            });
          } else {
            this.$message.error(res.msg || "上传失败");
            this.upload.fileList.forEach((item) => {
              item.status = "exception";
            });
          }
          this.upload.isUploading = false;
        })
        .catch((err) => {
          console.error(err);
          this.$message.error("服务器错误");
          this.upload.isUploading = false;
        });
    },
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          let submitData = JSON.parse(JSON.stringify(this.form));
          if (submitData.annex && typeof submitData.annex === "object") {
            submitData.annex = JSON.stringify(submitData.annex);
          } else if (!submitData.annex) {
            submitData.annex = null;
          }

          if (this.form.contractId != null) {
            updateContract(submitData).then((response) => {
              this.$modal.msgSuccess("修改成功");
              this.$tab.closeOpenPage({ path: '/contract/accounting' }); // Default return, logic can be refined
            });
          } else {
            addContract(submitData).then((response) => {
              this.$modal.msgSuccess("新增成功");
              this.$tab.closeOpenPage({ path: '/contract/accounting' });
            });
          }
        }
      });
    },
  },
};
</script>
<style scoped>
.mt20 {
  margin-top: 20px;
}
</style>
