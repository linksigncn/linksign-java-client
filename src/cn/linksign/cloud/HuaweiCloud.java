package cn.linksign.cloud;

import cn.linksign.utils.PropertiesUtils;
import cn.linksign.utils.TokenProcessor;
import com.obs.services.ObsClient;
import com.obs.services.model.DeleteObjectResult;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectResult;
import com.qcloud.cos.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class HuaweiCloud implements FileCloud {
    private static Logger log = LoggerFactory.getLogger(HuaweiCloud.class);
    private static String endpoint;
    private static String accessKeyId;
    private static String accessKeySecret;
    private static String privateBucketName;

    static {
        Properties properties = PropertiesUtils.loadProperties("filecloud.properties");
        endpoint = properties.getProperty("obs.endpoint");
        accessKeyId = properties.getProperty("obs.accessKeyId");
        accessKeySecret = properties.getProperty("obs.accessKeySecret");
        privateBucketName = properties.getProperty("obs.privateBucketName");

    }

    public static void main(String[] args) {
//        String inFile = args[0];
        HuaweiCloud cloud = new HuaweiCloud();
        String key = cloud.uploadDoc(new File("/Users/lamwinking/Desktop/12123213123"), false);
        System.out.println(key);

        File file = cloud.download(key);
        System.out.println(file.getAbsoluteFile());
    }

    private static String createFileKey() {
        return TokenProcessor.getUUID();
    }

    /**
     * 创建
     *
     * @return
     */
    private ObsClient getClient() {
        return new ObsClient(accessKeyId, accessKeySecret, endpoint);
    }

    @Override
    public String uploadDoc(File file, boolean encrypt) {
        String firlUrl = file.getAbsolutePath();
        String suffix = "";
        if (firlUrl.indexOf(".") != -1)
            suffix = "." + firlUrl.substring(firlUrl.lastIndexOf(".") + 1);

        String fileId = createFileKey() + suffix;
        for (int i = 0; i < 3; i++) {
            ObsClient obsClient = getClient();
            try {
                log.info("第 {} 次上传, fileId:{}, file:{}", i + 1, fileId, file.getAbsolutePath());

                PutObjectResult putObjectResult = obsClient.putObject(privateBucketName, fileId, file);
                log.info("key:{},url:{}", putObjectResult.getObjectKey(), putObjectResult.getObjectUrl());
                return putObjectResult.getObjectKey();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    obsClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    @Override
    public String updateDoc(String fileId, File file, boolean encrypt) {
        for (int i = 0; i < 3; i++) {
            ObsClient obsClient = getClient();
            try {
                log.info("第 {} 次上传, fileId:{}, file:{}", i + 1, fileId, file.getAbsolutePath());

                PutObjectResult putObjectResult = obsClient.putObject(privateBucketName, fileId, file);
                log.info("key:{},url:{}", putObjectResult.getObjectKey(), putObjectResult.getObjectUrl());
                return putObjectResult.getObjectKey();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    obsClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    @Override
    public String uploadResource(File file) {
        return uploadDoc(file, false);
    }

    @Override
    public String updateResource(String fileId, File file) {
        return updateDoc(fileId, file, false);
    }

    @Override
    public String uploadImg(File file) {
        return uploadDoc(file,true);
    }

    @Override
    public String uploadImg(File file, String bucketName, String visitUrl) {
        return uploadDoc(file,true);
    }

    @Override
    public String uploadTempImg(File file) {
        return uploadDoc(file,true);
    }

    @Override
    public File download(String key) {
        return download(key, privateBucketName);
    }

    @Override
    public void delete(String fileId) {
        ObsClient obsClient = getClient();
        try {
            DeleteObjectResult deleteObjectResult = obsClient.deleteObject(privateBucketName, fileId);
            log.info("key:{},isDeleteMarker:{}", fileId, deleteObjectResult.isDeleteMarker());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                obsClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getUrl(String fileId) {
        return null;
    }

    @Override
    public String uploadDoc(File file, boolean encrypt, String bucketName) {
        return null;
    }

    @Override
    public String updateDoc(String fileId, File file, boolean encrypt, String bucketName) {
        return null;
    }

    @Override
    public File download(String key, String bucketName) {
        log.info("key:{}", key);
        if (key == null || key.trim().equals("")) {
            return null;
        }
        for (int i = 0; i < 3; i++) {
            ObsClient obsClient = getClient();
            log.info("第 {} 次下载, key:{}", i + 1, key);
//            FileOutputStream fileOutputStream = null;
            try {
                ObsObject object = obsClient.getObject(privateBucketName, key);
                InputStream inputStream = object.getObjectContent();
                String suffix = "";
                if (key.indexOf(".") != -1)
                    suffix = "." + key.substring(key.lastIndexOf(".") + 1);
                else
                    suffix = ".pdf";//

                File file = File.createTempFile("temp_", suffix);
//                fileOutputStream = new FileOutputStream(file);
//                IOUtils.copy(inputStream, fileOutputStream);
                FileUtils.copyInputStreamToFile(inputStream, file);
                return file;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                try {
                    obsClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public void deleteImg(String key) {
        delete(key);
    }

    @Override
    public void deleteImg(String key, String bucketName) {
        ObsClient obsClient = getClient();
        try {
            DeleteObjectResult deleteObjectResult = obsClient.deleteObject(bucketName, key);
            log.info("key:{},isDeleteMarker:{}", key, deleteObjectResult.isDeleteMarker());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                obsClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteResource(String key) {

    }

    @Override
    public String uploadDoc(File file, String key, Map<String, Object> meta) {
        String firlUrl = file.getAbsolutePath();
        String suffix = "";
        if (firlUrl.indexOf(".") != -1)
            suffix = "." + firlUrl.substring(firlUrl.lastIndexOf(".") + 1);


        String fileId = "";
        if (!StringUtils.isNullOrEmpty(key)) {

            fileId = createFileKey() + suffix;
        } else {
            fileId = key;
        }
        ObjectMetadata metadata = new ObjectMetadata();
        if (meta != null) {
            for (Map.Entry<String, Object> entry : meta.entrySet()) {
                metadata.addUserMetadata(entry.getKey(), entry.getValue().toString());
            }
        }
        for (int i = 0; i < 3; i++) {
            ObsClient obsClient = getClient();
            try {
                log.info("第 {} 次上传, fileId:{}, file:{}", i + 1, fileId, file.getAbsolutePath());
                PutObjectResult putObjectResult = obsClient.putObject(privateBucketName, fileId, file, metadata);
                log.info("key:{},url:{}", putObjectResult.getObjectKey(), putObjectResult.getObjectUrl());
                return putObjectResult.getObjectKey();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    obsClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    @Override
    public FileObject downloadWithMeta(String key) {
        log.info("key:{}", key);
        if (key == null || key.trim().equals("")) {
            return null;
        }
        for (int i = 0; i < 3; i++) {
            ObsClient obsClient = getClient();
            log.info("第 {} 次下载, key:{}", i + 1, key);

//            FileOutputStream fileOutputStream = null;
            FileObject fileObject = new FileObject();
            try {
                ObsObject object = obsClient.getObject(privateBucketName, key);
                InputStream inputStream = object.getObjectContent();
                String suffix = "";
                if (key.indexOf(".") != -1)
                    suffix = "." + key.substring(key.lastIndexOf(".") + 1);
                else
                    suffix = ".pdf";//

                File file = File.createTempFile("temp_", suffix);
//                fileOutputStream = new FileOutputStream(file);
//                IOUtils.copy(inputStream, fileOutputStream);
                FileUtils.copyInputStreamToFile(inputStream, file);

                fileObject.setFile(file);
                ObjectMetadata metadata = object.getMetadata();
                Map<String, Object> metadata1 = metadata.getMetadata();
                fileObject.setMeta(metadata1);
                return fileObject;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                try {
                    obsClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    @Override
    public String getUploadStaticUrl(String key) {
        return null;
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
}
