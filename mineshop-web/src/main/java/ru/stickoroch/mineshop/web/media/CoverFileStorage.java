package ru.stickoroch.mineshop.web.media;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.stickoroch.mineshop.core.model.ProductModel;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class CoverFileStorage {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp"
    );

    private final Path coverRoot;

    public CoverFileStorage(
            @Value("${app.media.cover-root}") Path coverRoot
    ) {
        this.coverRoot = coverRoot;
    }

    @SneakyThrows
    public String load(@NonNull MultipartFile cover, long productId) {
        if (cover.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл обложки пуст");
        }

        String contentType = cover.getContentType() == null
                ? ""
                : cover.getContentType().toLowerCase();

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Поддерживаются только JPEG, PNG и WebP"
            );
        }

        String extension = switch (contentType) {
            case MediaType.IMAGE_JPEG_VALUE -> ".jpg";
            case MediaType.IMAGE_PNG_VALUE -> ".png";
            default -> ".webp";
        };

        Path dir = coverRoot.resolve("products").resolve(String.valueOf(productId)).normalize();
        Files.createDirectories(dir);

        Path dest = dir.resolve(UUID.randomUUID() + extension).normalize();
        cover.transferTo(dest);

        return coverRoot.relativize(dest).normalize().toString().replace('\\', '/');
    }

    /**
     * Возвращает файл обложки товара или {@code null}, если обложки нет,
     * она ещё не загружена или путь выходит за пределы каталога обложек.
     */
    public File getCoverAsFileOrNull(ProductModel productCoverModel) {
        if (productCoverModel == null || productCoverModel.getImageCover() == null
                || productCoverModel.getImageCover().isBlank()) {
            return null;
        }

        Path path = coverRoot
                .resolve(productCoverModel.getImageCover())
                .normalize();

        if (!path.startsWith(coverRoot.normalize()) || !Files.isRegularFile(path)) {
            return null;
        }

        return path.toFile();
    }

    public ResponseEntity<Resource> getCoverAsResource(String storageKey) {
        try {
            Path path = coverRoot
                    .resolve(storageKey)
                    .normalize();

            if (!path.startsWith(coverRoot.normalize())
                    || !Files.exists(path)
                    || !Files.isRegularFile(path)) {
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
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                    .lastModified(Files.getLastModifiedTime(path).toMillis())
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

        if (!dir.startsWith(coverRoot.normalize()) || !Files.exists(dir)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }


}