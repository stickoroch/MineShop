package ru.stickoroch.mineshop.web.media;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.stickoroch.mineshop.core.model.ProductModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class CoverFileStorage {

    private final Path coverRoot;

    public CoverFileStorage(
            @Value("${app.media.cover-root}") Path coverRoot
    ) {
        this.coverRoot = coverRoot;
    }

    @SneakyThrows
    public String load(@NonNull MultipartFile cover, long productId) {
        Path dir = coverRoot.resolve("products").resolve(String.valueOf(productId)).normalize();
        Files.createDirectories(dir);

        Path dest = dir.resolve(UUID.randomUUID() + ".jpg").normalize();
        cover.transferTo(dest);

        return coverRoot.relativize(dest).normalize().toString().replace('\\', '/');
    }

    public File getCoverAsFile(ProductModel productCoverModel) {
        return coverRoot
                .resolve(productCoverModel.getImageCover())
                .normalize().toFile();
    }

    public ResponseEntity<Resource> getCoverAsResource(String storageKey) {
        try {
            Path path = coverRoot
                    .resolve(storageKey)
                    .normalize();

            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Файл не найден"
                );
            }

            Resource resource = new UrlResource(path.toUri());

            String contentType = Files.probeContentType(path);

            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(contentType)
                    )
                    .body(resource);

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Не удалось прочитать файл",
                    e
            );
        }
    }

    @SneakyThrows
    public void deleteCover(long productId) {
        Path dir = coverRoot.resolve("products").resolve(String.valueOf(productId)).normalize();
        Path dest = dir.resolve(UUID.randomUUID() + ".jpg").normalize();

        Files.deleteIfExists(dest);
    }


}