package cn.linksign.cloud;

import java.io.File;
import java.util.Map;

/**
 * @author wz
 */
public interface FileCloud {

    /**
     * 上传文档
     *
     * @param file    文件
     * @param encrypt 是否加密
     * @return 文件ID
     */
    String uploadDoc(File file, boolean encrypt);

    /**
     * 更新文档
     *
     * @param fileId  文档ID
     * @param file    文件
     * @param encrypt 是否加密
     * @return 文件ID
     */
    String updateDoc(String fileId, File file, boolean encrypt);

    /**
     * 存储资源（静态资源、公章、签字、头像之类的）文件，不会清理
     *
     * @param file 文件
     * @return https://xxx/xxx
     */
    String uploadResource(File file);

    /**
     * 修改资源
     *
     * @param fileId 文件 ID (格式：al81c305fb312a40c3fdaf04a2b4d43a00.pdf 或 https://xxx/al60b4ad7306bd55dc22560297b20b7979.jpg)
     * @param file   文件
     * @return https://xxx/xxx
     */
    String updateResource(String fileId, File file);

    /**
     * 上传图片
     *
     * @param file 文件
     * @return 图片完整路径url
     */
    String uploadImg(File file);
    String uploadImg(File file,String bucketName, String visitUrl);

    /**
     * 上传临时图片
     *
     * @param file 文件
     * @return 图片完整路径url
     */
    String uploadTempImg(File file);

    /**
     * 下载文件
     *
     * @param key
     * @return
     */
    File download(String key);

    /**
     * 删除文件
     *
     * @param fileId
     */
    void delete(String fileId);

    /**
     * 获取文件访问路径
     *
     * @param fileId
     * @return
     */
    String getUrl(String fileId);

    /**
     * @param file
     * @param encrypt
     * @param bucketName
     * @return
     */
    String uploadDoc(File file, boolean encrypt, String bucketName);

    /**
     * @param fileId
     * @param file
     * @param encrypt
     * @param bucketName
     * @return
     */
    String updateDoc(String fileId, File file, boolean encrypt, String bucketName);

    /**
     * @param key
     * @param bucketName
     * @return
     */
    File download(String key, String bucketName);

    /**
     * 删除图片
     *
     * @param key
     */
    void deleteImg(String key);

    /**
     * 删除图片
     *
     * @param key
     * @param bucketName
     */
    void deleteImg(String key, String bucketName);

    /**
     * 删除资源文件
     *
     * @param key
     */
    void deleteResource(String key);


    /**
     * @param file
     * @param meta
     * @return
     */
    String uploadDoc(File file, String key, Map<String, Object> meta);


    /**
     * @param key
     * @return
     */
    FileObject downloadWithMeta(String key);

    String getUploadStaticUrl(String key);
    int restoreFile(String key);
    int restoreFile(String key,int restoreType);
    int restoreFile(String bucketName, String key);
    int restoreFile(String bucketName, String key,int restoreType);
    int isArchive(String bucketName, String key);
    int isArchive(String key);

}
