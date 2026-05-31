package cn.linksign.cloud;

import cn.linksign.utils.PropertiesUtils;
import com.aliyun.oss.OSSException;

import java.io.File;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 文件
 *
 * @author wz
 */
public class FSUtils {

    private static String uploadDocs = null;

    static {
        Properties properties = PropertiesUtils.loadProperties("filecloud.properties");
        uploadDocs = properties.getProperty("cloudProvideDoc");

    }


    /**
     * 上传文档
     *
     * @param file
     * @param encrypt 是否加密
     * @return
     */
    public static String uploadDoc(File file, boolean encrypt) {
        FileCloud fileCloud = null;
        fileCloud = getDocFileCloud();

        return fileCloud.uploadDoc(file, encrypt);
    }

    /**
     * 上传文档
     *
     * @param file
     * @param encrypt    是否加密
     * @param bucketName bucketName名
     * @return
     */
    public static String uploadDoc(File file, boolean encrypt, String bucketName) {
        FileCloud fileCloud = null;
        fileCloud = getDocFileCloud();

        return fileCloud.uploadDoc(file, encrypt, bucketName);
    }

    /**
     * 更新文档
     *
     * @param fileId  文档ID
     * @param file    文件
     * @param encrypt 是否加密
     * @return 文件ID
     */
    public static String updateDoc(String fileId, File file, boolean encrypt) {
        FileCloud fileCloud = getCloudByFileId(fileId);

        return fileCloud.updateDoc(fileId, file, encrypt);
    }

    /**
     * 更新文档
     *
     * @param fileId     文档ID
     * @param file       文件
     * @param encrypt    是否加密
     * @param bucketName bucketName名
     * @return 文件ID
     */
    public static String updateDoc(String fileId, File file, boolean encrypt, String bucketName) {
        FileCloud fileCloud = getCloudByFileId(fileId);

        return fileCloud.updateDoc(fileId, file, encrypt, bucketName);
    }

    /**
     * 上传图片
     *
     * @param file 文件
     * @return 图片完整路径url
     */
    public static String uploadImg(File file) {
        FileCloud fileCloud = getImgFileCloud();

        return fileCloud.uploadImg(file);
    }

    /**
     * 上传资源（静态资源、公章、签字、头像之类的）文件，不会清理
     *
     * @param file
     * @return
     */
    public static String uploadResource(File file) {
        FileCloud fileCloud = getImgFileCloud();
        return fileCloud.uploadResource(file);
    }

    /**
     * 修改资源
     *
     * @param fileId 文件 ID (格式：al81c305fb312a40c3fdaf04a2b4d43a00.pdf 或 https://xxx/al60b4ad7306bd55dc22560297b20b7979.jpg)
     * @param file   文件
     * @return https://xxx/xxx
     */
    public static String updateResource(String fileId, File file) {
        FileCloud fileCloud = getImgFileCloud();
        return fileCloud.updateResource(fileId, file);
    }

    /**
     * 下载文件
     *
     * @param key
     * @return
     */
    public static File download(String key) {
        FileCloud fileCloud = getCloudByFileId(key);
        return fileCloud.download(key);
    }

    /**
     * 下载文件
     *
     * @param key
     * @return
     */
    public static File download(String key, String bucketName) {
        FileCloud fileCloud = getCloudByFileId(key);
        return fileCloud.download(key, bucketName);
    }

    /**
     * 删除文件
     *
     * @param key
     */
    public static void delete(String key) {
        FileCloud fileCloud = null;
        fileCloud = getCloudByFileId(key);
        fileCloud.delete(key);
    }

    /**
     * 删除图片，默认是publicKey
     *
     * @param key
     */
    public static void deleteImg(String key) {
        FileCloud fileCloud = getCloudByFileId(key);
        fileCloud.deleteImg(key);
    }

    /**
     * 删除图片
     *
     * @param key
     * @param bucketName
     */
    public static void deleteImg(String key, String bucketName) {
        FileCloud fileCloud = getCloudByFileId(key);
        fileCloud.deleteImg(key, bucketName);
    }

    /**
     * 随机返回文档云提供商
     *
     * @return
     */
    private static FileCloud getDocFileCloud() {
        if(uploadDocs.equalsIgnoreCase("aliyun")) {
            return new OSSCloud();
        }
        else if (uploadDocs.equalsIgnoreCase("qcloud")) {
            return new CosCloud();
        }else if (uploadDocs.equalsIgnoreCase("local")) {
            return new LocalCloud();
        }else {
            return new OSSCloud();
        }
    }

    /**
     * 随机返回图片云提供商
     *
     * @return
     */
    private static FileCloud getImgFileCloud() {
        return getDocFileCloud();
    }
    private static Pattern ossPattern = Pattern.compile("^al.*|.*/al.*");
    private static Pattern cosPattern = Pattern.compile("^tx.*|.*/tx.*");
    private static Pattern locPattern = Pattern.compile("^loc.*|.*/loc.*");

    private static FileCloud getCloudByFileId(String key) {
        boolean isOss = ossPattern.matcher(key).matches();
        boolean isCos = cosPattern.matcher(key).matches();
        boolean isLoc = locPattern.matcher(key).matches();
        if(isOss)
            return new OSSCloud();
        else if (isCos) {
            return new CosCloud();
        }
        else if (isLoc) {
            return new LocalCloud();
        }
        else
        {
            return new OSSCloud();
        }
     }

     public static String getStaticUploadUrl(String key)
     {
         FileCloud fileCloud = getCloudByFileId(key);

         return fileCloud.getUploadStaticUrl(key);
     }

    /**
     * 上传临时文件
     *
     * @param file
     * @return
     */
    public static String uploadTempImg(File file) {
        FileCloud fileCloud = getDocFileCloud();
        return fileCloud.uploadTempImg(file);
    }

    public static int restoreFile(String key) {
        FileCloud fileCloud = getDocFileCloud();
        return fileCloud.restoreFile(key);
    }

    public static int restoreFile(String bucketName, String key) {
        FileCloud fileCloud = getDocFileCloud();
        return fileCloud.restoreFile(bucketName, key);
    }

    public static int restoreFile(String key, int restoreType) {
        FileCloud fileCloud = getDocFileCloud();
        return fileCloud.restoreFile(key,restoreType);
    }

    public static int restoreFile(String bucketName, String key, int restoreType) {
        FileCloud fileCloud = getDocFileCloud();
        return fileCloud.restoreFile(bucketName, key,restoreType);
    }

    public static int isArchive(String key) {
        FileCloud fileCloud = getDocFileCloud();
        return fileCloud.isArchive(key);
    }
    public static int isArchive(String bucketName, String key) {
        FileCloud fileCloud = getDocFileCloud();
        return fileCloud.isArchive(bucketName, key);
    }
    
}
