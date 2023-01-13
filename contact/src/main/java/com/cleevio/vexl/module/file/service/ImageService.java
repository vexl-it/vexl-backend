package com.cleevio.vexl.module.file.service;

import com.cleevio.vexl.module.file.dto.request.ImageRequest;
import com.cleevio.vexl.module.file.exception.FileWriteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {

    @Value("${content.path}")
    private final String contentPath;

    @Value("${content.url}")
    private final String urlPath;

    /**
     * Store file to the storage
     *
     * @return Destination path
     * @throws FileWriteException File could not be stored
     */
    public String save(@NotNull ImageRequest imageRequest) throws FileWriteException {
        try {
            final File dir = new File(this.contentPath);

            if (!dir.exists() && !dir.mkdirs()) {
                throw new FileWriteException();
            }

            final String fileName = UUID.randomUUID().toString();

            final File destination = new File(dir, fileName + "." + imageRequest.getExtension());

            try (FileOutputStream stream = new FileOutputStream(destination)) {
                stream.write(Base64.getDecoder().decode(imageRequest.getData()));
            }

            log.info("created file {}", destination.getPath());

            return String.join(StringUtils.EMPTY, urlPath, destination.getName());
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new FileWriteException();
        }
    }
}
