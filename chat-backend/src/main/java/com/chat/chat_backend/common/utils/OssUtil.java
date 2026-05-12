package com.chat.chat_backend.common.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.ObjectMetadata;
import com.chat.chat_backend.config.AliyunOSSProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云OSS文件上传工具类，支持V4签名、按日期分目录存储和UUID文件名
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssUtil {

    /** 阿里云OSS配置属性 */
    private final AliyunOSSProperties aliyunOSSProperties;

    /**
     * 上传文件到OSS指定目录，使用自动生成的文件名
     * @param file 待上传的文件
     * @param folder 目标目录前缀（如"avatars/"）
     * @return 上传文件的公开访问URL
     * @throws IOException 文件读取失败时抛出
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        return uploadFile(file, folder, null);
    }

    /**
     * 上传文件到OSS指定目录，使用UUID文件名，支持V4签名（如北京region必须）
     * @param file 待上传的文件
     * @param folder 目标目录前缀
     * @param oldFileUrl 旧文件URL（当前未使用，预留参数）
     * @return 上传文件的公开访问URL
     * @throws IOException 文件读取失败时抛出
     */
    public String uploadFile(MultipartFile file, String folder, String oldFileUrl) throws IOException {
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();
        String accessKeyId = aliyunOSSProperties.getAccessKeyId();
        String accessKeySecret = aliyunOSSProperties.getAccessKeySecret();

        // 检查配置是否完整
        if (endpoint == null || endpoint.isEmpty() || accessKeyId == null || accessKeyId.isEmpty()) {
            throw new RuntimeException("OSS configuration is incomplete");
        }

        // 创建凭证提供者
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);

        // 按日期分目录：folder/yyyy/MM
        String dir = folder + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + extension;
        String objectName = dir + "/" + newFileName;

        // 配置V4签名（北京region必须）
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

        // 构建OSS客户端
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            // 设置文件元信息并上传
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType(file.getContentType());
            ossClient.putObject(bucketName, objectName, file.getInputStream(), meta);
            log.info("OSS上传成功: {}, contentType={}", objectName, file.getContentType());

            // 返回公开访问URL
            return "https://" + bucketName + "." + endpoint.replace("https://", "") + "/" + objectName;
        } finally {
            // 关闭OSS客户端释放资源
            ossClient.shutdown();
        }
    }
}