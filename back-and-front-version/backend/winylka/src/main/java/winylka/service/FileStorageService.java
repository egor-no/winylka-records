package winylka.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".jpg", ".jpeg", ".png", ".webp");

    private final Path productsDirectory;

    public FileStorageService(
            @Value("${app.files.products-dir}") String productsDirectory
    ) {
        this.productsDirectory = Paths.get(productsDirectory)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.productsDirectory);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Не удалось создать папку для изображений: " + this.productsDirectory,
                    e
            );
        }
    }

    public String saveProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл изображения не выбран");
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Можно загружать только изображения");
        }

        String extension = getExtension(file.getOriginalFilename());

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Разрешены только JPG, JPEG, PNG и WEBP"
            );
        }

        String generatedFileName = UUID.randomUUID() + extension;
        Path target = productsDirectory.resolve(generatedFileName).normalize();

        if (!target.startsWith(productsDirectory)) {
            throw new IllegalArgumentException("Некорректное имя файла");
        }

        try {
            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить изображение", e);
        }

        return "/files/products/" + generatedFileName;
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("У файла отсутствует расширение");
        }

        return fileName.substring(fileName.lastIndexOf("."))
                .toLowerCase(Locale.ROOT);
    }
}