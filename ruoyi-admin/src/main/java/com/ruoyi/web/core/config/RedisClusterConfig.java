package com.ruoyi.web.core.config;

import java.net.InetSocketAddress;
import java.util.Objects;

import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.internal.HostAndPort;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DnsResolver;
import io.lettuce.core.resource.MappingSocketAddressResolver;

@Configuration
public class RedisClusterConfig {
//    @Bean
//    public LettuceClientConfigurationBuilderCustomizer lettuceCustomizer() {
//        // 定义映射规则：将任何 172.18.0.x 的 IP 强制映射到 127.0.0.1
//        // 端口保持不变
//        MappingSocketAddressResolver resolver = MappingSocketAddressResolver.create(
//                DnsResolver.jvmDefault(),
//                (HostAndPort hostAndPort) -> {
//                    String host = hostAndPort.getHostText();
//                    if (host.startsWith("172.18.")) {
//                        return HostAndPort.of("127.0.0.1", hostAndPort.getPort());
//                    }
//                    return hostAndPort;
//                });
//
//        return clientConfigurationBuilder -> {
//            // 设置集群拓扑刷新选项
//            ClusterTopologyRefreshOptions refreshOptions = ClusterTopologyRefreshOptions.builder()
//                    .enableAllAdaptiveRefreshTriggers() // 开启自适应刷新
//                    .enablePeriodicRefresh() // 开启周期刷新
//                    .build();
//
//            clientConfigurationBuilder.clientOptions(Objects.requireNonNull(
//                    ClusterClientOptions.builder()
//                            .topologyRefreshOptions(refreshOptions)
//                            .build()));
//
//            // 将自定义的地址解析器注入到客户端资源中
//            clientConfigurationBuilder.clientResources(Objects.requireNonNull(
//                    ClientResources.builder()
//                            .socketAddressResolver(resolver)
//                            .build()));
//        };
//    }

    /**
     * 1. 自定义 Lettuce 客户端资源（包含 IP 映射规则）
     */
    @Bean(destroyMethod = "shutdown")
    public io.lettuce.core.resource.ClientResources lettuceClientResources() {
        System.out.println("======> 步骤 1: 正在构建自定义 ClientResources (IP 映射字典) ...");

        io.lettuce.core.resource.MappingSocketAddressResolver resolver = io.lettuce.core.resource.MappingSocketAddressResolver.create(
                io.lettuce.core.resource.DnsResolvers.UNRESOLVED,
                original -> {
                    String host = original.getHostText();
                    int port = original.getPort();

                    // 打印底层的解析请求，只要这行打印了，说明拦截生效！
                    System.out.println("======> 拦截到 Redis 连接请求: 尝试连接 " + host + ":" + port);

                    if ("172.20.0.11".equals(host) && port == 6379) return io.lettuce.core.internal.HostAndPort.of("127.0.0.1", 7001);
                    if ("172.20.0.12".equals(host) && port == 6379) return io.lettuce.core.internal.HostAndPort.of("127.0.0.1", 7002);
                    if ("172.20.0.13".equals(host) && port == 6379) return io.lettuce.core.internal.HostAndPort.of("127.0.0.1", 7003);
                    if ("172.20.0.14".equals(host) && port == 6379) return io.lettuce.core.internal.HostAndPort.of("127.0.0.1", 7004);
                    if ("172.20.0.15".equals(host) && port == 6379) return io.lettuce.core.internal.HostAndPort.of("127.0.0.1", 7005);
                    if ("172.20.0.16".equals(host) && port == 6379) return io.lettuce.core.internal.HostAndPort.of("127.0.0.1", 7006);

                    return original;
                }
        );

        return io.lettuce.core.resource.DefaultClientResources.builder()
                .socketAddressResolver(resolver)
                .build();
    }
}