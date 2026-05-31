package cn.linksign.cloud;

import java.io.File;
import java.util.Map;

public class FileObject {
    private File file;
    private Map<String, Object> meta;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }
}
