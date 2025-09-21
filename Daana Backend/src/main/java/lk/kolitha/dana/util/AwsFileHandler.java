package lk.kolitha.dana.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Log4j2
public class AwsFileHandler {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    @Value("${aws.s3.bucket-url}")
    private String bucketUrl;
    @Value("${aws.s3.project-folder}")
    private String projectBucketFolder;

    private final AmazonS3 s3Client;

    public Optional<String> uploadToS3Bucket(MultipartFile file, String name, String folder) {
        try {
            String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
            assert fileExtension != null;
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            String fileName = projectBucketFolder + folder + name +"."+ fileExtension;
            s3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);
            String fileUrl = bucketUrl + fileName;
            log.info("file file Url: {}", fileUrl);
            return Optional.of(bucketUrl + fileName);
        } catch (IOException e) {
            log.trace("Error occurred while uploading image to s3: {}", e.getMessage());
            return Optional.empty();
        }
    }

}
