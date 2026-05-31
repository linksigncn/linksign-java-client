package cn.linksign.cloud;

import cn.linksign.utils.AESUtils;
import cn.linksign.utils.PropertiesUtils;
import cn.linksign.utils.TokenProcessor;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.qcloud.cos.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 阿里云OSS
 *
 * @author wz
 */
public class OSSCloud implements FileCloud {
    private static Logger log = LoggerFactory.getLogger(OSSCloud.class);

    private static String endpoint;
    private static String accessKeyId;
    private static String accessKeySecret;
    private static String privateBucketName;
    private static String publicBucketName;
    private static String tmpBucketName;
    private static String visitUrl;
    private static String resourcesBucketName;
    private static String resourcesVisitUrl;

    private static ExecutorService executorService = Executors.newFixedThreadPool(8);
    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    static {
        Properties properties = PropertiesUtils.loadProperties("filecloud.properties");
        endpoint = properties.getProperty("al.endpoint");
        accessKeyId = properties.getProperty("al.accessKeyId");
        accessKeySecret = properties.getProperty("al.accessKeySecret");
        privateBucketName = properties.getProperty("al.privateBucketName");
        publicBucketName = properties.getProperty("al.publicBucketName");
        tmpBucketName = properties.getProperty("al.tmpBucketName");
        visitUrl = properties.getProperty("al.visitUrl");
        resourcesBucketName = properties.getProperty("al.static.resourcesBucketName");
        resourcesVisitUrl = properties.getProperty("al.static.resourcesVisitUrl");
    }

    @Override
    public String uploadDoc(File file, boolean encrypt) {
        return uploadFileToOss(null, file, encrypt, privateBucketName);
    }

    @Override
    public String uploadDoc(File file, boolean encrypt, String bucketName) {
        return uploadFileToOss(null, file, encrypt, bucketName);
    }

    @Override
    public String updateDoc(String fileId, File file, boolean encrypt) {
        return uploadFileToOss(fileId, file, encrypt, privateBucketName);
    }

    @Override
    public String updateDoc(String fileId, File file, boolean encrypt, String bucketName) {
        return uploadFileToOss(fileId, file, encrypt, bucketName);
    }

    /**
     * @param fileId     文件ID，不传=新文件，传=更新文件
     * @param file
     * @param encrypt
     * @param bucketName
     * @return
     */
    private static String uploadFileToOss(String fileId, File file, boolean encrypt, String bucketName) {
        encrypt = false;
        log.info("upload file:{},encrypt:{}", file.getAbsolutePath(), encrypt);
        String firlUrl = file.getAbsolutePath();
        String suffix = firlUrl.substring(firlUrl.lastIndexOf(".") + 1);
        if (fileId == null || "".equals(fileId)) {
            fileId = createFileKey() + "." + suffix;
        }
        for (int i = 0; i < 3; i++) {
            OSSClient client = getClient();
            log.info("第 {} 次上传, fileId:{}, file:{}", i + 1, fileId, file.getAbsolutePath());
            try {
                ObjectMetadata meta = new ObjectMetadata();
                meta.addUserMetadata("fileType", suffix);
                if (encrypt) {
                    File newFile = AESUtils.encryptFile(file, suffix);
                    if (newFile == null) {
                        return null;
                    }

                    meta.addUserMetadata("encrypt", "true");
                    client.putObject(bucketName, fileId, newFile, meta);
                    try {
                        FileUtils.forceDelete(newFile);
                    } catch (Exception e) {
                        log.error("delete file failed:{}", newFile.getAbsoluteFile());
                    }
                } else {
                    meta.addUserMetadata("encrypt", "false");
                    client.putObject(bucketName, fileId, file, meta);
                }
                return fileId;
            } catch (Exception e) {
                log.info("第 {} 次上传失败！", (i + 1));
                e.printStackTrace();
            } finally {
                client.shutdown();
            }
        }
        return null;
    }

    @Override
    public String uploadResource(File file) {
        log.info("uploadResource file:{}", file.getAbsolutePath());
        String firlUrl = file.getAbsolutePath();
        String suffix = firlUrl.substring(firlUrl.lastIndexOf(".") + 1);
        String fileId = createFileKey() + "." + suffix;
        return resourcesVisitUrl+uploadFileToOss(fileId, file, false, resourcesBucketName);

//        for (int i = 0; i < 3; i++) {
//            OSSClient client = getClient();
//            log.info("第 {} 次上传, fileId:{}, file:{}", i + 1, fileId, file.getAbsolutePath());
//            try {
//                ObjectMetadata meta = new ObjectMetadata();
//                meta.addUserMetadata("fileType", suffix);
//                meta.addUserMetadata("encrypt", "false");
//                client.putObject(resourcesBucketName, fileId, file, meta);
//                return resourcesVisitUrl + fileId;
//            } catch (Exception e) {
//                log.info("第 {} 次下载失败！", (i + 1));
//                e.printStackTrace();
//            } finally {
//                client.shutdown();
//            }
//        }
//        return null;
    }

    @Override
    public String updateResource(String fileId, File file) {
        log.info("updateResource fileId:{}, file:{}", fileId, file.getAbsolutePath());
        String firlUrl = file.getAbsolutePath();
        String suffix = firlUrl.substring(firlUrl.lastIndexOf(".") + 1);
        if (fileId == null || fileId.trim().equals("")) {
            fileId = createFileKey() + "." + suffix;
        } else if (fileId.toLowerCase().startsWith("http")) {
            fileId = fileId.substring(fileId.lastIndexOf("/") + 1, fileId.length());
            if (!fileId.startsWith("al")) {
                fileId = createFileKey() + "." + suffix;
            }
        } else if (!fileId.startsWith("al")) {
            fileId = createFileKey() + "." + suffix;
        } else
        {
            fileId = createFileKey() + "." + suffix;
        }
        return resourcesVisitUrl + uploadFileToOss(fileId, file, false, resourcesBucketName);

//        for (int i = 0; i < 3; i++) {
//            OSSClient client = getClient();
//            log.info("第 {} 次上传, fileId:{}, file:{}", i + 1, fileId, file.getAbsolutePath());
//            try {
//                ObjectMetadata meta = new ObjectMetadata();
//                meta.addUserMetadata("fileType", suffix);
//                meta.addUserMetadata("encrypt", "false");
//                client.putObject(resourcesBucketName, fileId, file, meta);
//                return resourcesVisitUrl + fileId;
//            } catch (Exception e) {
//                log.info("第 {} 次上传失败！", (i + 1));
//                e.printStackTrace();
//            } finally {
//                client.shutdown();
//            }
//        }
//        return null;
    }
    @Override
    public String uploadImg(File file) {
        return uploadImg(file, publicBucketName, visitUrl);
    }

    @Override
    public String uploadImg(File file,String bucketName, String visitUrl) {
        String firlUrl = file.getAbsolutePath();
        String suffix = firlUrl.substring(firlUrl.lastIndexOf(".") + 1);
        String fileId = createFileKey() + "." + suffix;
        return visitUrl + uploadFileToOss(fileId, file, false, bucketName);

//        // String fileId = "alf811839b6f424292ae2cc4ef29280385";
//        for (int i = 0; i < 3; i++) {
//            OSSClient client = getClient();
//            log.info("第 {} 次上传, fileId:{}, file:{}", i + 1, fileId, file.getAbsolutePath());
//            try {
//                ObjectMetadata meta = new ObjectMetadata();
//                meta.addUserMetadata("fileType", suffix);
//                meta.addUserMetadata("encrypt", "false");
//                PutObjectResult result = client.putObject(publicBucketName, fileId, file, meta);
//                return visitUrl + fileId;
//            } catch (Exception e) {
//                log.info("第 {} 次下载失败！", (i + 1));
//                e.printStackTrace();
//            } finally {
//                client.shutdown();
//            }
//        }
//        return null;
    }

    @Override
    public File download(String key) {
        return donwloadOSSFile(key, privateBucketName);
    }

    @Override
    public File download(String key, String bucketName) {
        return donwloadOSSFile(key, bucketName);
    }

    /**
     * 从oss下载文件
     *
     * @param key
     * @param bucketName
     * @return
     */
    private File donwloadOSSFile(String key, String bucketName) {
        for (int i = 0; i < 3; i++) {
            OSSClient client = getClient();
            log.info("第 {} 次下载, key:{}, bucketName:{}", i + 1, key, bucketName);
            try {
                String suffix = key.substring(key.lastIndexOf(".") + 1);
                File tempFile = File.createTempFile("temp_", "." + suffix);
                ObjectMetadata objectMetadata = client.getObject(new GetObjectRequest(bucketName, key), tempFile);
                Map<String, String> map = objectMetadata.getUserMetadata();
                String type = map.get("filetype");
                String isEncrypt = map.get("encrypt");
                if (isEncrypt != null && isEncrypt.equals("true")) {
                    tempFile = AESUtils.decryptFile(tempFile, type);
                }
                return tempFile;
            }
            catch (OSSException e)
            {
//                if (e.getErrorCode() != null && e.getErrorCode().equalsIgnoreCase("InvalidObjectState")) {
//
//                        // 设置解冻冷归档Object的优先级。
//                        // RestoreTier.RESTORE_TIER_EXPEDITED 表示1小时内完成解冻。
//                        // RestoreTier.RESTORE_TIER_STANDARD 表示2~5小时内完成解冻。
//                        // RestoreTier.RESTORE_TIER_BULK 表示5~12小时内完成解冻。
//                        RestoreJobParameters jobParameters = new RestoreJobParameters(RestoreTier.RESTORE_TIER_STANDARD);
//
//                        // 配置解冻参数，以设置5小时内解冻完成，解冻状态保持2天为例。
//                        // 第一个参数表示保持解冻状态的天数，默认是1天，此参数适用于解冻Archive（归档）与ColdArchive（冷归档）类型Object。
//                        // 第二个参数jobParameters表示解冻优先级，只适用于解冻ColdArchive类型Object。
//                        RestoreConfiguration configuration = new RestoreConfiguration(365, jobParameters);
//                        // 发起解冻请求。
//                        RestoreObjectResult restoreObjectResult = client.restoreObject(bucketName, key, configuration);
//                        log.error("{}下载失败！发起解冻请求:{}",key, restoreObjectResult.getStatusCode());
//
                    throw e;
                }
//            }
            catch (Exception e) {
                log.error("第 {} 次下载失败！,{}", (i + 1), e);
             } finally {
                client.shutdown();
            }
        }
        return null;
    }

    @Override
    public void delete(String fileId) {
        OSSClient client = getClient();
        client.deleteObject(privateBucketName, fileId);
        client.shutdown();
    }

    @Override
    public String getUrl(String fileId) {
        OSSClient client = getClient();

        return null;
    }

    private static OSSClient getClient() {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        return ossClient;
    }

    /**
     * 文件key
     *
     * @return
     */
    private static String createFileKey() {
        String d = getDatePath();
        return d + "/al" + TokenProcessor.getUUID();
    }


    /**
     * @param srcBucketName
     * @param srcKey
     * @param destBucketName
     * @param destKey
     * @return
     */
    public static void copyObject(final String srcBucketName, final String srcKey, final String destBucketName, final String destKey) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                OSSClient client = getClient();
                CopyObjectResult result = client.copyObject(srcBucketName, srcKey, destBucketName, destKey);
                // log.info("ETag: " + result.getETag() + " LastModified: " + result.getLastModified());
                System.out.println(srcBucketName + "-" + srcKey + " --> " + destBucketName + "-" + destKey + " ,count:" + atomicInteger.incrementAndGet());
                client.shutdown();
            }
        });

    }

    /**
     * copy bucket
     *
     * @param srcBucketName
     * @param destBucketName
     * @return
     */
    public static void copyBucket(String srcBucketName, String destBucketName) {
        OSSClient client = getClient();
        final int maxKeys = 200;
        String nextMarker = null;
        ObjectListing objectListing = null;
        do {
            objectListing = client.listObjects(new ListObjectsRequest(srcBucketName).withMarker(nextMarker).withMaxKeys(maxKeys));
            List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
            for (OSSObjectSummary s : sums) {
                String key = s.getKey();
                copyObject(srcBucketName, key, destBucketName, key);
            }
            nextMarker = objectListing.getNextMarker();

        } while (objectListing.isTruncated());
        client.shutdown();
    }

    /**
     * CopyObject
     */
    private static void copy() {
        String[] src = new String[]{"storagels", "staticls", "resourcels"};
        String[] dest = new String[]{"uatstorage", "uatstatic", "uatresource"};

        for (int i = 0; i < src.length; i++) {
            String srcBucket = src[i];
            String destBucket = dest[i];
            copyBucket(srcBucket, destBucket);
        }
    }

    public static void main(String[] args) {
        FileCloud fileCloud = new OSSCloud();
        // File file = new File("E:\\test\\test.txt");
        // fileCloud.uploadDoc(file, true);
        File file = fileCloud.download("alf811839b6f424292ae2cc4ef29280385");
        System.out.println(file.getAbsolutePath());
        // fileCloud.delete("alf811839b6f424292ae2cc4ef29280385");

        // File file = new File("E:\\test\\pic.jpg");
        // String fileId = fileCloud.uploadResource(file);
        // System.out.println(fileId);

        // getAllObject("uatresource");
        // copyObject("storagels", "alac2da15065b606b4048631a1ab669afa.pdf", "uatstorage", "alac2da15065b606b4048631a1ab669afa.pdf");

        // copy();

    }

    @Override
    public String uploadTempImg(File file) {

        //上传到临时文档库，此bucket会定时清理过期文件
        String key = FSUtils.uploadDoc(file, false, tmpBucketName);
        //服务器端生成url签名字串
        OSSClient Server  = getClient();
        // 设置URL过期时间为1小时
        Date expiration = new Date(new Date().getTime() + 3600 * 1000 * 24);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(tmpBucketName, key, HttpMethod.GET);
        //设置过期时间
        request.setExpiration(expiration);
        // 生成URL签名(HTTP GET请求)
        URL signedUrl = Server .generatePresignedUrl(request);
        log.info("signed url for getObject: " + signedUrl);

//		OSSClient client  = new OSSClient(endpoint, accessId, accessKey);
//		Map<String, String> customHeaders = new HashMap<String, String>();
////		// 添加GetObject请求头
//		customHeaders.put("Range", "bytes=100-1000");
//		OSSObject object = client.getObject(signedUrl,customHeaders);
//		object.getKey();
        String url = signedUrl.toString();
        //如果是http，并且是默认端口，转成httpS

        if(signedUrl.getProtocol().equalsIgnoreCase("http"))
        {
            url = url.replaceFirst("http", "https");
            url = url.replace("-internal", "");
            return url;
        }
        else {
            url = url.replace("-internal", "");
            return url;
        }
    }

    private static String getDatePath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        String d = sdf.format(new Date());
        return d;
    }

    @Override
    public void deleteImg(String key) {
        deleteImg(key, publicBucketName);
    }

    @Override
    public void deleteImg(String key, final String bucketName) {
        if (StringUtils.isNullOrEmpty(key) || StringUtils.isNullOrEmpty(bucketName)) {
            return;
        }
        if (key.startsWith(visitUrl)) {
            key = key.replace(visitUrl, "");
        } else if (key.startsWith(resourcesVisitUrl)) {
            key = key.replace(resourcesVisitUrl, "");
        }
        final String ossKey = key;
        executorService.submit(new Runnable() {
            public void run() {
                OSSClient client = getClient();
                boolean isExist = client.doesObjectExist(bucketName, ossKey);
                if (isExist) {
                    log.info("delete object success: {}", ossKey);
                    client.deleteObject(bucketName, ossKey);
                } else {
                    log.error("delete object fail: {} not exist", ossKey);
                }
                client.shutdown();
            }
        });
    }

    @Override
    public void deleteResource(String key) {
        // TODO Auto-generated method stub
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
        return null;
    }
    @Override
    public int restoreFile(String key) {
        return restoreFile(privateBucketName,key);
    }

    @Override
    public int restoreFile(String key, int restoreType) {

        return restoreFile(privateBucketName,key,restoreType);
    }

    @Override
    public int restoreFile(String bucketName, String key) {
        return restoreFile(bucketName, key, 0);
    }

    @Override
    public int restoreFile(String bucketName, String key, int restoreType) {
        OSSClient client = getClient();
        try {
            ObjectMetadata objectMetadata = client.getObjectMetadata(bucketName, key);
            StorageClass storageClass = objectMetadata.getObjectStorageClass();
            if (storageClass == StorageClass.Archive || storageClass == StorageClass.ColdArchive) {
                // 解冻Object。
                // 设置解冻冷归档Object的优先级。费用不同
//                        // RestoreTier.RESTORE_TIER_EXPEDITED 表示1小时内完成解冻。
//                        // RestoreTier.RESTORE_TIER_STANDARD 表示2~5小时内完成解冻。
//                        // RestoreTier.RESTORE_TIER_BULK 表示5~12小时内完成解冻。
                RestoreJobParameters jobParameters = null;
                if (restoreType == 0) {
                    //RestoreTier.RESTORE_TIER_STANDARD 表示2~5小时内完成解冻。
                    jobParameters = new RestoreJobParameters(RestoreTier.RESTORE_TIER_STANDARD);
                }else if (restoreType == 1) {
                    // RestoreTier.RESTORE_TIER_BULK 表示5~12小时内完成解冻。
                    jobParameters = new RestoreJobParameters(RestoreTier.RESTORE_TIER_BULK);
                } else if (restoreType == -9) {
                    // RestoreTier.RESTORE_TIER_EXPEDITED 表示1小时内完成解冻。【尽量不要用，好贵】
                    jobParameters = new RestoreJobParameters(RestoreTier.RESTORE_TIER_EXPEDITED);
                }

                log.info("解冻类型：{}",jobParameters.getRestoreTier().toString());
                RestoreConfiguration restoreConfiguration = new RestoreConfiguration(365, jobParameters);
                RestoreObjectResult restoreObjectResult = client.restoreObject(bucketName, key, restoreConfiguration);
                log.info("解冻Object:{},status:{}", key,restoreObjectResult.getStatusCode());
                if(restoreObjectResult.getStatusCode()==202)
                    return 1;//解除操作执行了
                else if(restoreObjectResult.getStatusCode()==200)
                    return 0;//解除状态下
                else {
                    return restoreObjectResult.getStatusCode();
                }
                //?1:restoreObjectResult.getStatusCode();//1是成功，其他是错误
            }else {
                return 0;//文件状态正常
            }
        }catch(OSSException e){
            if (e.getErrorCode() != null && e.getErrorCode().equalsIgnoreCase("RestoreAlreadyInProgress")) {
                return 2;//解除中
            }else {
                log.error("{}", e);
            }
        }catch(Exception e){
            log.error("{}", e);
        } finally {
            client.shutdown();
        }
        return -1;
    }

    @Override
    public int isArchive(String bucketName, String key) {
        OSSClient client = getClient();
        try {
            ObjectMetadata objectMetadata = client.getObjectMetadata(bucketName, key);
            StorageClass storageClass = objectMetadata.getObjectStorageClass();
            if (storageClass == StorageClass.Archive || storageClass == StorageClass.ColdArchive) {
                if (objectMetadata.getObjectRawRestore() != null) {
                    return objectMetadata.isRestoreCompleted()?2:3;//2是已解除，3=解除中
                }
                return 1;
            } else {
                return 0;//文件状态正常
            }
        } catch (OSSException e) {
            log.error("{}", e);
        } catch (Exception e) {
            log.error("{}", e);
        } finally {
            client.shutdown();
        }
        return -1;
    }

    @Override
    public int isArchive(String key) {
        return this.isArchive(privateBucketName, key);
    }
}
