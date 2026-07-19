package winylka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import winylka.service.FileStorageService;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDirectory;

    @Test
    void shouldSaveProductImage() throws Exception {
        FileStorageService storageService =
                new FileStorageService(tempDirectory.toString());

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "cover.png",
                "image/png",
                "test image content".getBytes()
        );

        String resultPath = storageService.saveProductImage(image);

        assertNotNull(resultPath);
        assertTrue(resultPath.startsWith("/files/products/"));
        assertTrue(resultPath.endsWith(".png"));

        String savedFileName = resultPath.substring(
                "/files/products/".length()
        );

        Path savedFile = tempDirectory.resolve(savedFileName);

        assertTrue(Files.exists(savedFile));
        assertArrayEquals(
                image.getBytes(),
                Files.readAllBytes(savedFile)
        );
    }

    @Test
    void shouldRejectNonImageFile() {
        FileStorageService storageService =
                new FileStorageService(tempDirectory.toString());

        MockMultipartFile textFile = new MockMultipartFile(
                "image",
                "document.txt",
                "text/plain",
                "not an image".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> storageService.saveProductImage(textFile)
        );

        assertEquals(
                "Можно загружать только изображения",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectUnsupportedImageExtension() {
        FileStorageService storageService =
                new FileStorageService(tempDirectory.toString());

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "cover.gif",
                "image/gif",
                "gif content".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> storageService.saveProductImage(image)
        );

        assertEquals(
                "Разрешены только JPG, JPEG, PNG и WEBP",
                exception.getMessage()
        );
    }
}