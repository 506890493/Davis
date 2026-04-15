# ==========================================
# 阶段 1：构建阶段 (在 GitHub 云端执行)
# ==========================================
FROM maven:3.8.5-openjdk-8 AS builder
WORKDIR /build

# RuoYi 是多模块项目，直接将整个项目源码复制进来
COPY . .

# 执行 Maven 打包 (跳过单元测试)
RUN mvn clean package -DskipTests

# ==========================================
# 阶段 2：运行阶段 (部署到你腾讯云的极简镜像)
# ==========================================
# 使用 slim 版本而不是 alpine，避免 RuoYi 图形验证码因缺少字体库而报错
FROM openjdk:8-jre-slim
WORKDIR /app

# 从构建阶段提取最终的可执行 Jar 包
# RuoYi 的主启动类和依赖通常打包在 ruoyi-admin 模块下
COPY --from=builder /build/ruoyi-admin/target/ruoyi-admin.jar app.jar

# 声明容器暴露的端口
EXPOSE 8080

# 启动命令：针对 Java 8 和你的 4G 服务器优化的内存参数
# 加入了 -XX:+UseG1GC 启用 G1 垃圾回收器，提升单机性能
CMD ["java", "-Xmx1024m", "-Xms512m", "-XX:+UseG1GC", "-jar", "app.jar"]