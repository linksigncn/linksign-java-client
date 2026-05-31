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
    public static void main(String[] args) {
//
//        TreeMap<String, Object> config = new TreeMap<String, Object>();
//
        try {
//            System.out.println(FSUtils.uploadTempImg( new File("/Users/lamwinking/Downloads/【体验】合同协议 (2).pdf")));
//            System.out.println(FSUtils.uploadDoc( new File("/Users/lamwinking/Downloads/【体验】合同协议 (2).pdf"), false));
//            System.out.println(FSUtils.updateDoc("2019050915/loc554c91b592c042e98f64da86e6db01d8.pdf",new File("/Users/lamwinking/Documents/A公司.pdf"),true));
//            System.out.println(FSUtils.uploadTempImg(new File("/Users/lamwinking/Downloads/【体验】合同协议 (2).pdf")));

            //getFileInfo
//            System.out.println(FSUtils.restoreFile("2021090215/al8de9da43f8384ba290a062647ccf718b.pdf"));
            System.out.println(FSUtils.isArchive("2021080821/al7ddb879a8a8a4e589aed92d2319f5006.pdf"));
//            System.out.println(FSUtils.uploadImg(new File("/Users/lamwinking/app/temp/ly/loc39efccc0ad9744f99d052ed6faa0be48.png")));
//            System.out.println(FSUtils.uploadImg(new File("/Users/lamwinking/Downloads/印章.png")));
//            FSUtils.delete("https://linkresource-1254454720.cos.ap-guangzhou.myqcloud.com/2019050515/txb47869f3491449f3a5aac13d7c45f767.jpg");
//            // 替换为您的 SecretId
//            config.put("SecretId", "AKIDtd8NN7rqpY1EBssrTpvlMhmbATXMFJPG");
//            // 替换为您的 SecretKey
//            config.put("SecretKey", "mJbeS3UijV3MSKyNYVDfud6JGzSfcJn2");
//
//            // 临时密钥有效时长，单位是秒
//            config.put("durationSeconds", 60*60);
//
//            // 换成您的 bucket
//            config.put("bucket", "linkstatic-1254454720");
//            // 换成 bucket 所在地区
//            config.put("region", "ap-guangzhou");
//
//            // 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的目录，例子：* 或者 doc/* 或者 picture.jpg
//            config.put("allowPrefix", "*");
//
//            // 密钥的权限列表。简单上传、表单上传和分片上传需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
//            String[] allowActions = new String[] {
//                    // 简单上传
//                    "name/cos:PutObject",
//                    // 表单上传、小程序上传
//                    "name/cos:PostObject",
//                    // 分片上传
//                    "name/cos:InitiateMultipartUpload",
//                    "name/cos:ListMultipartUploads",
//                    "name/cos:ListParts",
//                    "name/cos:UploadPart",
//                    "name/cos:CompleteMultipartUpload"
//            };
//            config.put("allowActions", allowActions);
//
//            JSONObject credential = CosStsClient.getCredential(config);
//            //成功返回临时密钥信息，如下打印密钥信息
//            System.out.println(credential);
        } catch (OSSException e) {
            //失败抛出异常
            System.out.println(e.getMessage());
            //throw new IllegalArgumentException("no valid secret !"+e.getMessage());
        }

    }
}
