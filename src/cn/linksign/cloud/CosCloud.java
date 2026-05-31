//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.linksign.cloud;

import cn.linksign.utils.AESUtils;
import cn.linksign.utils.PropertiesUtils;
import cn.linksign.utils.TokenProcessor;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import com.qcloud.cos.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CosCloud implements FileCloud {
    private static Logger log = LoggerFactory.getLogger(CosCloud.class);
    private static String appId;
    private static String secretId;
    private static String secretKey;
    private static String privateBucketName;
    private static String tmpBucketName;
    private static String publicBucketName;
    private static String visitUrl;
    private static String resourcesBucketName;
    private static String resourcesVisitUrl;
    private static String region;

    public CosCloud() {


    }

    private static COSClient getCOSClient() {
        // 1 初始化用户身份信息（secretId, secretKey）。
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
// 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
// clientConfig中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
        ClientConfig clientConfig = new ClientConfig(new Region(region));
 // 3 生成 cos 客户端。
         COSClient cosClient = new COSClient(cred, clientConfig);
// bucket的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式
//        String bucketName = "mybucket-1251668577";
        return cosClient;
    }

    public String uploadDoc(File file, boolean encrypt) {
        return uploadFileToCos((String) null, file, encrypt, privateBucketName);
    }

    public String updateDoc(String fileId, File file, boolean encrypt) {
        return uploadFileToCos(fileId, file, encrypt, privateBucketName);
    }

    @Override
    public String uploadResource(File file) {
        return uploadResource(null, file);
    }

    @Override
    public String updateResource(String fileId, File file) {
        return uploadResource(fileId, file);
    }

    public String uploadDoc(File file, boolean encrypt, String bucketName) {
        return uploadFileToCos((String) null, file, encrypt, bucketName);
    }

    public String updateDoc(String fileId, File file, boolean encrypt, String bucketName) {
        return uploadFileToCos(fileId, file, encrypt, bucketName);
    }

    private static String uploadFileToCos(String fileId, File file, boolean encrypt, String bucketName) {
        log.info("cos file:{},encrypt:{}", file.getAbsolutePath(), encrypt);
        COSClient cosClient = null;
        try {
            String firlUrl = file.getAbsolutePath();
            String suffix = ".pdf";
            if (firlUrl.indexOf(".") != -1) {
                suffix = firlUrl.substring(firlUrl.lastIndexOf(".") + 1);
            }
            if (fileId == null || "".equals(fileId)) {
                fileId = createFileKey() + "." + suffix;
            }

            log.info("uploadFileToCos fileId:{}", fileId);
            cosClient = getCOSClient();
            File encFile = AESUtils.encryptFile(file, suffix);
            cosClient = getCOSClient();

//            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileId, file);
            PutObjectResult putObjectResult = upload(cosClient,bucketName, fileId, encFile);
            encFile.delete();
            if (putObjectResult != null) {
                return fileId;
            }
//            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

        } catch (Exception var9) {
            var9.printStackTrace();
            log.info("上传文件到cos失败:{},fileId:{}", var9, fileId);
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }

//        log.info("uploadFileToCos result:{}", fileId);
        return null;
    }


    private static PutObjectResult upload(COSClient cosClient,String bucketName, String fileId, File file) {
         PutObjectResult putObjectResult = null;
        try {
            for (int i = 0; i < 3; i++) {
                if (i >= 2) {
                    log.error("重试{}次...上传到:{},fileid:{}", i, bucketName, fileId);
                }
                 PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileId, file);
                putObjectResult = cosClient.putObject(putObjectRequest);
                break;
            }
        } catch (Exception var9) {
            log.error("上传到:{}失败:fileid:{},{}", bucketName, fileId, var9);
        }
        return putObjectResult;
    }

    public String uploadResource(String fileId, File file) {

        COSClient cosClient = null;

        try {
            String sourceUrl = null;
            if (fileId == null || fileId.equalsIgnoreCase("")) {
                String fileUrl = file.getAbsolutePath();
                String suffix = ".pdf";
                if (fileUrl.indexOf(".") != -1) {
                    suffix = fileUrl.substring(fileUrl.lastIndexOf(".") + 1);
                }
                fileId = createFileKey() + "." + suffix;
            }
            log.info("cosUploadResource, fileId:{}", fileId);
//            cosClient = getCOSClient();
//            PutObjectRequest putObjectRequest = new PutObjectRequest(resourcesBucketName, fileId, file);
            cosClient = getCOSClient();

            PutObjectResult putObjectResult = upload(cosClient,resourcesBucketName, fileId, file);
            if (putObjectResult != null) {

                return resourcesVisitUrl + fileId;
//                cosClient = getCOSClient();
//
//                GeneratePresignedUrlRequest req =
//                        new GeneratePresignedUrlRequest(resourcesBucketName, fileId, HttpMethodName.GET);
//                URL url = cosClient.generatePresignedUrl(req);
//
//                sourceUrl = url.toString();
//                return sourceUrl;
            }
        } catch (Exception var9) {
            log.error("上传resource失败:fileid:{},{}", fileId, var9);
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
        return null;
    }
    public String uploadImg(File file,String bucketName,String visitUrl) {
        return uploadImg(file);
    }
    public String uploadImg(File file) {
        COSClient cosClient = null;

        String sourceUrl = null;
        log.info("file:{}", file.getAbsolutePath());
        String firlUrl = file.getAbsolutePath();
        String suffix = firlUrl.substring(firlUrl.lastIndexOf(".") + 1);
        String fileId = createFileKey() + "." + suffix;
        log.info("fileId:{}", fileId);
        cosClient = getCOSClient();

        PutObjectResult putObjectResult = upload(cosClient, publicBucketName, fileId, file);
        if (putObjectResult != null) {

            return visitUrl + fileId;

//            GeneratePresignedUrlRequest req =
//                    new GeneratePresignedUrlRequest(publicBucketName, fileId, HttpMethodName.GET);
//            URL url = cosClient.generatePresignedUrl(req);
//
//            sourceUrl = url.toString();
//            return sourceUrl;
        }

        cosClient.shutdown();
        return sourceUrl;
    }

    @Override
    public String uploadTempImg(File file) {

        COSClient cosClient = null;

        String sourceUrl = null;
        String firlUrl = file.getAbsolutePath();
        String suffix = firlUrl.substring(firlUrl.lastIndexOf(".") + 1);
        String fileId = createFileKey() + "." + suffix;
        log.info("uploadTempImg:{},{}", fileId,file.getAbsolutePath());
        cosClient = getCOSClient();

        PutObjectResult putObjectResult = upload(cosClient, tmpBucketName, fileId, file);
        if (putObjectResult != null) {
//            return visitUrl + fileId;
// 这里设置签名在1个小时后过期
            Date expirationDate = new Date(System.currentTimeMillis() + 60L * 60L * 1000L);
            GeneratePresignedUrlRequest req =  new GeneratePresignedUrlRequest(tmpBucketName, fileId, HttpMethodName.GET);
            req.setExpiration(expirationDate);
            URL url = cosClient.generatePresignedUrl(req);

            sourceUrl = url.toString();
            return sourceUrl;
        }

        cosClient.shutdown();
        return sourceUrl;
     }


//    public String uploadTempImg(File file) {
//        Properties p = PropertiesUtils.loadProperties("filecloud.properties.work");
//        String localPath = p.getProperty("local.path");
//        String localPathUrl = p.getProperty("local.path.url");
//        String firlUrl = file.getAbsolutePath();
//        String d = getDatePath();
//        String suffix = firlUrl.substring(firlUrl.lastIndexOf(".") + 1);
//        String newfileId = "unsync_" + TokenProcessor.getUUID() + "." + suffix;
//        String newPath = localPath + d + File.separatorChar + newfileId;
//
//        try {
//            FileUtils.copyFile(file, new File(newPath));
//            return localPathUrl + d + "/" + newfileId;
//        } catch (IOException var11) {
//            var11.printStackTrace();
//            return null;
//        }
//    }

    public File download(String key) {
        return this.download(key, privateBucketName);
    }

    public File download(String key, String bucketName) {
        COSClient client = getCOSClient();

        try {
            String suffix = key.substring(key.lastIndexOf(".") + 1);
            File tempFile = File.createTempFile("temp_", "." + suffix);
            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
            ObjectMetadata downObjectMeta = client.getObject(getObjectRequest, tempFile);

            File okFile = AESUtils.decryptFile(tempFile, suffix);
            if (tempFile.exists()) {
                tempFile.delete();
            }
            return okFile;

        } catch (IOException var14) {
            var14.printStackTrace();
        } finally {
            client.shutdown();
        }

        return null;
    }

    public void delete(String fileId) {
        delete(privateBucketName, fileId);
    }

    public void delete(String bucketName, String fileId) {
        COSClient client = getCOSClient();
        try {
            client.deleteObject(privateBucketName, fileId);

        } catch (Exception var14) {
            log.error("删除文件失败：fileId:{}, {}", fileId, var14);
        } finally {
            client.shutdown();
        }
    }

    public String getUrl(String fileId) {
        return null;
    }

    public void deleteImg(String key) {
        this.deleteImg(key, publicBucketName);
    }

    public void deleteImg(String key, String bucketName) {
        log.info("delete object: {}", key);
        if (!StringUtils.isNullOrEmpty(key) && !StringUtils.isNullOrEmpty(bucketName)) {
            if (key.startsWith(visitUrl)) {
                key = key.replace(visitUrl, "");
            } else if (key.startsWith(resourcesVisitUrl)) {
                key = key.replace(resourcesVisitUrl, "");
            }

            delete(bucketName, key);

        }
    }

    @Override
    public void deleteResource(String key) {
            this.deleteImg(key, resourcesBucketName);
    }

    @Override
    public String uploadDoc(File file, String key, Map<String, Object> meta) {
        return null;
    }

    @Override
    public FileObject downloadWithMeta(String key) {
        return null;
    }

    @Override
    public String getUploadStaticUrl(String key) {

        // bucket 的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式
        String bucketName =  publicBucketName;

 // 设置签名过期时间(可选), 若未进行设置, 则默认使用 ClientConfig 中的签名过期时间(1小时)
// 这里设置签名在半个小时后过期
        COSClient cosClient = getCOSClient();
        Date expirationTime = new Date(System.currentTimeMillis() + 24 * 60L * 60L * 1000L);
        URL url = cosClient.generatePresignedUrl(bucketName, key, expirationTime, HttpMethodName.PUT);
        cosClient.shutdown();
        return url.toString();
    }

    @Override
    public int restoreFile(String key) {
        return 1;
    }

    @Override
    public int restoreFile(String key, int restoreType) {
        return 0;
    }

    @Override
    public int restoreFile(String bucketName, String key) {
        return 1;
    }

    @Override
    public int restoreFile(String bucketName, String key, int restoreType) {
        return 0;
    }

    @Override
    public int isArchive(String bucketName, String key) {
        return 0;
    }

    @Override
    public int isArchive(String key) {
        return 0;
    }

    private static String createFileKey() {
        String d = getDatePath();
        return d + "/tx" + TokenProcessor.getUUID();
    }

    private static String getDatePath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        String d = sdf.format(new Date());
        return "/" + d;
    }

    public static void main(String[] args) {
    }

    static {
        Properties properties = PropertiesUtils.loadProperties("filecloud.properties");
        appId = properties.getProperty("tx.appId");
        secretId = properties.getProperty("tx.secretId");
        secretKey = properties.getProperty("tx.secretKey");
        privateBucketName = properties.getProperty("tx.privateBucketName");
        tmpBucketName = properties.getProperty("tx.tmpBucketName");
        publicBucketName = properties.getProperty("tx.publicBucketName");
        visitUrl = properties.getProperty("tx.visitUrl");
        resourcesBucketName = properties.getProperty("tx.static.resourcesBucketName");
        resourcesVisitUrl = properties.getProperty("tx.static.resourcesVisitUrl");
        region = properties.getProperty("tx.region");
    }
}
