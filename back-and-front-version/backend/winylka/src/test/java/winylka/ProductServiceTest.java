package winylka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import winylka.dto.ProductForm;
import winylka.event.ProductRestockedEvent;
import winylka.infra.ProductRepository;
import winylka.model.Product;
import winylka.service.FileStorageService;
import winylka.service.ProductService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository repository;
    private FileStorageService fileStorageService;
    private ApplicationEventPublisher eventPublisher;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        repository = mock(ProductRepository.class);
        fileStorageService = mock(FileStorageService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        productService = new ProductService(
                repository,
                fileStorageService,
                eventPublisher
        );
    }

    @Test
    void shouldCreateProductWithImage() {
        ProductForm form = createValidForm();

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "brat.png",
                "image/png",
                "image content".getBytes()
        );

        form.setImage(image);

        when(fileStorageService.saveProductImage(image))
                .thenReturn("/files/products/generated-brat.png");

        when(repository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.create(form);

        assertEquals("Charli XCX", result.getArtist());
        assertEquals("Brat", result.getName());
        assertEquals("Vinyl", result.getFormat());
        assertEquals(new BigDecimal("34.90"), result.getPrice());
        assertEquals(5, result.getStockQuantity());

        assertEquals(
                "/files/products/generated-brat.png",
                result.getImg()
        );

        verify(fileStorageService).saveProductImage(image);
        verify(repository).save(result);
    }

    @Test
    void shouldCreateProductWithoutImage() {
        ProductForm form = createValidForm();

        when(repository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.create(form);

        assertNull(result.getImg());

        verify(fileStorageService, never())
                .saveProductImage(any());

        verify(repository).save(result);
    }

    @Test
    void shouldAddStockToExistingProduct() {
        Product product = createExistingProduct();
        product.setStockQuantity(5);

        when(repository.findById(1))
                .thenReturn(Optional.of(product));

        when(repository.save(product))
                .thenReturn(product);

        Product result = productService.addStock(1, 7);

        assertEquals(12, result.getStockQuantity());

        verify(repository).findById(1);
        verify(repository).save(product);
    }

    @Test
    void shouldTreatNullStockAsZeroWhenAddingStock() {
        Product product = createExistingProduct();
        product.setStockQuantity(null);

        when(repository.findById(1))
                .thenReturn(Optional.of(product));

        when(repository.save(product))
                .thenReturn(product);

        Product result = productService.addStock(1, 4);

        assertEquals(4, result.getStockQuantity());

        verify(repository).save(product);
    }

    @Test
    void shouldRejectZeroStockAddition() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.addStock(1, 0)
        );

        assertEquals(
                "Количество поступившего товара должно быть больше нуля",
                exception.getMessage()
        );

        verify(repository, never()).findById(anyInt());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectNegativeStockAddition() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.addStock(1, -5)
        );

        assertEquals(
                "Количество поступившего товара должно быть больше нуля",
                exception.getMessage()
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldFailWhenAddingStockToMissingProduct() {
        when(repository.findById(99))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.addStock(99, 5)
        );

        assertEquals(
                "Товар с id 99 не найден",
                exception.getMessage()
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdateProductWithoutReplacingImage() {
        Product existing = createExistingProduct();
        existing.setImg("/files/products/old-cover.jpg");
        existing.setStockQuantity(3);

        ProductForm form = createValidForm();
        form.setName("Brat and It's Completely Different");
        form.setStockQuantity(10);
        form.setImage(null);

        when(repository.findById(1))
                .thenReturn(Optional.of(existing));

        when(repository.save(existing))
                .thenReturn(existing);

        Product result = productService.update(1, form);

        assertEquals(
                "Brat and It's Completely Different",
                result.getName()
        );

        assertEquals(10, result.getStockQuantity());

        assertEquals(
                "/files/products/old-cover.jpg",
                result.getImg()
        );

        verify(fileStorageService, never()).saveProductImage(any());
        verify(repository).save(existing);
        verify(fileStorageService, never()).deleteProductImage(anyString());
    }

    @Test
    void shouldReplaceImageWhenUpdatingProduct() {
        Product existing = createExistingProduct();
        existing.setImg("/files/products/old-cover.jpg");

        ProductForm form = createValidForm();

        MockMultipartFile newImage = new MockMultipartFile(
                "image",
                "new-cover.webp",
                "image/webp",
                "new image".getBytes()
        );

        form.setImage(newImage);

        when(repository.findById(1))
                .thenReturn(Optional.of(existing));

        when(fileStorageService.saveProductImage(newImage))
                .thenReturn("/files/products/new-cover.webp");

        when(repository.save(existing))
                .thenReturn(existing);

        Product result = productService.update(1, form);

        assertEquals(
                "/files/products/new-cover.webp",
                result.getImg()
        );

        verify(fileStorageService).saveProductImage(newImage);
        verify(repository).save(existing);
        verify(fileStorageService).deleteProductImage("/files/products/old-cover.jpg");
    }

    @Test
    void shouldRejectProductWithoutArtist() {
        ProductForm form = createValidForm();
        form.setArtist(" ");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.create(form)
        );

        assertEquals(
                "Не указан исполнитель",
                exception.getMessage()
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectNegativeStockOnCreate() {
        ProductForm form = createValidForm();
        form.setStockQuantity(-1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.create(form)
        );

        assertEquals(
                "Остаток не может быть отрицательным",
                exception.getMessage()
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldDeleteProductAndItsImage() {
        Product product = createExistingProduct();
        product.setImg("/files/products/brat.png");

        when(repository.findById(1))
                .thenReturn(Optional.of(product));

        productService.delete(1);

        verify(repository).delete(product);

        verify(fileStorageService)
                .deleteProductImage(
                        "/files/products/brat.png"
                );
    }

    @Test
    void shouldDeleteProductWithoutImage() {
        Product product = createExistingProduct();
        product.setImg(null);

        when(repository.findById(1))
                .thenReturn(Optional.of(product));

        productService.delete(1);

        verify(repository).delete(product);

        verify(fileStorageService, never())
                .deleteProductImage(anyString());
    }

    @Test
    void shouldFailWhenDeletingMissingProduct() {
        when(repository.findById(99))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.delete(99)
        );

        assertEquals(
                "Товар с id 99 не найден",
                exception.getMessage()
        );

        verify(repository, never()).delete(any());
        verify(fileStorageService, never())
                .deleteProductImage(anyString());
    }

    @Test
    void shouldPublishRestockEventWhenStockChangesFromZero() {
        Product product = new Product();
        product.setId(1);
        product.setStockQuantity(0);

        when(repository.findById(1))
                .thenReturn(Optional.of(product));

        when(repository.save(product))
                .thenReturn(product);

        productService.addStock(1, 3);

        assertEquals(3, product.getStockQuantity());

        verify(eventPublisher).publishEvent(
                new ProductRestockedEvent(1)
        );
    }

    @Test
    void shouldNotPublishRestockEventWhenProductWasAlreadyInStock() {
        Product product = new Product();
        product.setId(1);
        product.setStockQuantity(2);

        when(repository.findById(1))
                .thenReturn(Optional.of(product));

        when(repository.save(product))
                .thenReturn(product);

        productService.addStock(1, 3);

        assertEquals(5, product.getStockQuantity());

        verify(eventPublisher, never()).publishEvent(any());
    }

    private ProductForm createValidForm() {
        ProductForm form = new ProductForm();

        form.setArtist("Charli XCX");
        form.setName("Brat");
        form.setCatNo("075678611696");
        form.setFormat("Vinyl");
        form.setNote("Green vinyl");
        form.setPrice(new BigDecimal("34.90"));
        form.setDescription("Sixth studio album");
        form.setStockQuantity(5);

        return form;
    }

    private Product createExistingProduct() {
        Product product = new Product();

        product.setId(1);
        product.setArtist("Charli XCX");
        product.setName("Brat");
        product.setCatNo("075678611696");
        product.setFormat("Vinyl");
        product.setPrice(new BigDecimal("34.90"));
        product.setDescription("Sixth studio album");
        product.setStockQuantity(5);

        return product;
    }
}