package cn.linksign.cloud;

import cn.linksign.utils.AESUtils;
import cn.linksign.utils.PropertiesUtils;
import cn.linksign.utils.TokenProcessor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class LocalCloud implements FileCloud {

    private static Logger log = LoggerFactory.getLogger(LocalCloud.class);
    private static String localPath;
    private static String localDocPath;
    private static String localPathUrl;

    static {
        Properties p = PropertiesUtils.loadProperties("filecloud.properties");
        localPath = p.getProperty("local.path");
        localDocPath = p.getProperty("local.doc.path");
        localPathUrl = p.getProperty("local.path.url");
    }

    @Override
    public String uploadDoc(File file, boolean encrypt) {
        return uploadDocToLocal(null, file, encrypt,null);
    }

    @Override
    public String updateDoc(String fileId, File file, boolean encrypt) {
        return uploadDocToLocal(fileId, file, encrypt,null);
    }

    private String uploadDocToLocal(String fileId, File file, boolean encrypt,String bucketName) {

        log.info("upload file:{},encrypt:{},bucketName:{}",fileId, file.getAbsolutePath(), encrypt,bucketName);
        String firlUrl = file.getAbsolutePath();
        String suffix = ".pdf";
        if(firlUrl.indexOf(".")!=-1)
            suffix = firlUrl.substring(firlUrl.lastIndexOf(".") + 1);

        if (fileId == null || "".equals(fileId)) {
            fileId = createFileKey() + "." + suffix;
        }

        File newFile = AESUtils.encryptFile(file, suffix);
        if (newFile == null) {
            return null;
        } else {
            File localFile = null;
            if (bucketName != null && !"".equalsIgnoreCase(bucketName)) {
                localFile = new File(bucketName + File.separator + fileId);
                fileId = bucketName + File.separator + fileId;
            }
            else
                localFile = new File(localDocPath + fileId);

            try {
                FileUtils.copyFile(newFile, localFile);
                newFile.delete();
                return fileId;
            } catch (IOException var9) {
                var9.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public String uploadResource(File file) {
        return uploadImgToLocal(file);
    }

    @Override
    public String updateResource(String fileId, File file) {
        return uploadImgToLocal(file);
    }

    @Override
    public String uploadImg(File file) {
        return uploadImgToLocal(file);
    }

    @Override
    public String uploadImg(File file, String bucketName,String visitUrl) {
        return uploadImgToLocal(file);
    }

    @Override
    public String uploadTempImg(File file) {
        return uploadImgToLocal(file);
    }

    private String uploadImgToLocal(File file) {
        String firlUrl = file.getAbsolutePath();
        String d = getDatePath();
        String suffix = firlUrl.substring(firlUrl.lastIndexOf(".") + 1);
        String newfileId = "loc" + TokenProcessor.getUUID() + "." + suffix;
        String newPath = localPath + d + File.separatorChar + newfileId;
        try {
            FileUtils.copyFile(file, new File(newPath));
            return localPathUrl + d + "/" + newfileId;
        } catch (IOException e) {
            log.error(" LocalCloud 上传图片失败：{}", e);
        }
        return null;
    }

    @Override
    public File download(String key) {
        return download(key, null);
     }

    @Override
    public void delete(String fileId) {
        new File(localDocPath + fileId).delete();
    }

    @Override
    public String getUrl(String fileId) {
        return null;
    }

    @Override
    public String uploadDoc(File file, boolean encrypt, String bucketName) {
        return uploadDocToLocal(null, file, encrypt,bucketName);
    }

    @Override
    public String updateDoc(String fileId, File file, boolean encrypt, String bucketName) {
        return uploadDocToLocal(fileId, file, encrypt,bucketName);
    }

    @Override
    public File download(String key, String bucketName) {
        log.info("download key:{},bucketName:{}", key,bucketName);

        File tempFile = new File(localDocPath + key);
        if(tempFile!=null && tempFile.exists()) {
            if(!tempFile.exists())
            {
                log.error("下载时，文件不存在:{}", key);
                return null;
            }
            File deFile = AESUtils.decryptFile(tempFile, "pdf");
            return deFile;
        }else if (bucketName != null && !"".equalsIgnoreCase(bucketName)) {
            tempFile = new File(bucketName + File.separator + key);
            if(!tempFile.exists())
            {
                log.error("下载时，文件不存在:{}", key);
                return null;
            }
            File deFile = AESUtils.decryptFile(tempFile, "pdf");
            return deFile;
        }

//        File tempFile = null;
//        File keyFile = new File(key);
//        if (keyFile != null && keyFile.exists()) {
//            tempFile = keyFile;
//        }else if (bucketName != null && !"".equalsIgnoreCase(bucketName)) {
//            tempFile = new File(bucketName+File.separator + key);
//        }else
//        {
//            tempFile = new File(localDocPath + key);
//        }
////        File target = null;
////        try {
//            if (tempFile.exists()) {
////                target = File.createTempFile("temp_", ".tmp");
////                FileUtils.copyFile(tempFile, target);
//                return tempFile;
//            }else
//            {
//                log.error("下载文件是，文件不存在:"+key);
//            }

//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        tempFile = AESUtils.decryptFile(tempFile, "pdf");
        return null;
    }

    @Override
    public void deleteImg(String key) {
        File img = new File(localPath + key);
        img.delete();
    }

    @Override
    public void deleteImg(String key, String bucketName) {
       if (bucketName != null && !"".equalsIgnoreCase(bucketName)) {
           File tempFile = new File(bucketName+File.separator + key);
           tempFile.delete();
       }else {
           File img = new File(localPath + key);
           img.delete();
       }
    }

    @Override
    public void deleteResource(String key) {
        File img = new File(localPath + key);
        img.delete();
    }

    @Override
    public String uploadDoc(File file, String key, Map<String, Object> meta) {
        return updateDoc(key, file, true);
    }


    @Override
    public FileObject downloadWithMeta(String key) {
        return null;
    }

    @Override
    public String getUploadStaticUrl(String key) {
        return localPathUrl + key;
    }

    @Override
    public int restoreFile(String key) {
        return this.restoreFile(null, key);
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

    /**
     * 文件key
     *
     * @return
     */
    private static String createFileKey() {
        String d = getDatePath();
//        return   "loc" + TokenProcessor.getUUID();
        return d + "/loc" + TokenProcessor.getUUID();
    }

    private static String getDatePath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        String d = sdf.format(new Date());
        return d;
    }
}
