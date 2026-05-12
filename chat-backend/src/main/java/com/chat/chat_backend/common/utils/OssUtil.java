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

@Slf4j
@Component
@RequiredArgsConstructor
public class OssUtil {

    private final AliyunOSSProperties aliyunOSSProperties;

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        return uploadFile(file, folder, null);
    }

    public String uploadFile(MultipartFile file, String folder, String oldFileUrl) throws IOException {
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();
        String accessKeyId = aliyunOSSProperties.getAccessKeyId();
        String accessKeySecret = aliyunOSSProperties.getAccessKeySecret();

        if (endpoint == null || endpoint.isEmpty() || accessKeyId == null || accessKeyId.isEmpty()) {
            throw new RuntimeException("OSS configuration is incomplete");
        }

        // 使用配置中的 AK/SK
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);

        // 按日期分目录
        String dir = folder + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + extension;
        String objectName = dir + "/" + newFileName;

        // V4 签名（北京 region 必须）
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType(file.getContentType());
            ossClient.putObject(bucketName, objectName, file.getInputStream(), meta);
            log.info("OSS上传成功: {}, contentType={}", objectName, file.getContentType());

            return "https://" + bucketName + "." + endpoint.replace("https://", "") + "/" + objectName;
        } finally {
            ossClient.shutdown();
        }
    }
}