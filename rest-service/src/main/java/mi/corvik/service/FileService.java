package mi.corvik.service;

import mi.corvik.entity.AppDocument;
import mi.corvik.entity.AppPhoto;
import mi.corvik.entity.BinaryContent;
import org.springframework.core.io.FileSystemResource;

public interface FileService {
    AppDocument getDocument(String docId);

    AppPhoto getPhoto(String photoId);

    FileSystemResource getFileSystemResource(BinaryContent binaryContent);
}