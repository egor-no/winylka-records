package winylka.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import winylka.dto.ProductForm;
import winylka.infra.ProductRepository;
import winylka.model.Product;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProductService {

    private final ProductRepository repo;
    private final FileStorageService fileStorageService;

    public ProductService(
            ProductRepository repo,
            FileStorageService fileStorageService
    ) {
        this.repo = repo;
        this.fileStorageService = fileStorageService;
    }

    public List<Product> findAll(
            String q,
            Integer maxPrice,
            String format,
            String sort,
            Integer limit,
            Integer offset
    ) {
        Stream<Product> s = repo.findAll().stream();

        if (q != null && !q.trim().isEmpty()) {
            String needle = q.trim().toLowerCase();

            s = s.filter(product ->
                    (safe(product.getArtist()) + " " + safe(product.getName()))
                            .contains(needle)
            );
        }

        if (maxPrice != null) {
            BigDecimal max = BigDecimal.valueOf(maxPrice);

            s = s.filter(product ->
                    product.getPrice() != null
                            && product.getPrice().compareTo(max) <= 0
            );
        }

        if (format != null && !format.isBlank()) {
            String fmt = format.trim().toLowerCase();

            s = s.filter(product ->
                    product.getFormat() != null
                            && product.getFormat().toLowerCase().equals(fmt)
            );
        }

        List<Product> list = s.collect(Collectors.toList());

        if (sort != null) {
            switch (sort) {
                case "priceAsc" ->
                        list.sort(Comparator.comparing(Product::getPrice));

                case "priceDesc" ->
                        list.sort(
                                Comparator.comparing(Product::getPrice).reversed()
                        );

                case "artistAsc" ->
                        list.sort(
                                Comparator.comparing(product ->
                                        safe(product.getArtist())
                                )
                        );

                case "random" -> Collections.shuffle(list);
            }
        }

        int off = offset == null ? 0 : Math.max(0, offset);
        int lim = limit == null ? list.size() : Math.max(0, limit);

        if (off >= list.size()) {
            return List.of();
        }

        return list.subList(off, Math.min(list.size(), off + lim));
    }

    public Product findById(int id) {
        return repo.findById(id).orElse(null);
    }

    public Map<Integer, Product> mapByIds(Collection<Integer> ids) {
        return repo.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
    }

    @Transactional
    public Product create(ProductForm form) {
        validate(form);

        Product product = new Product();

        copyFormToProduct(form, product);

        if (form.getImage() != null && !form.getImage().isEmpty()) {
            String imagePath =
                    fileStorageService.saveProductImage(form.getImage());

            product.setImg(imagePath);
        }

        return repo.save(product);
    }

    @Transactional
    public Product update(int id, ProductForm form) {
        Product product = repo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Товар с id " + id + " не найден"
                        )
                );

        validate(form);

        String oldImagePath = product.getImg();

        copyFormToProduct(form, product);

        boolean imageReplaced =
                form.getImage() != null && !form.getImage().isEmpty();

        if (imageReplaced) {
            String newImagePath =
                    fileStorageService.saveProductImage(form.getImage());

            product.setImg(newImagePath);
        }

        Product updatedProduct = repo.save(product);

        if (imageReplaced && oldImagePath != null) {
            deleteImageAfterCommit(oldImagePath);
        }

        return updatedProduct;
    }

    @Transactional
    public void delete(int id) {
        Product product = repo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Товар с id " + id + " не найден"
                        )
                );

        String imagePath = product.getImg();

        repo.delete(product);

        if (imagePath != null) {
            deleteImageAfterCommit(imagePath);
        }
    }

    private void deleteImageAfterCommit(String imagePath) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            fileStorageService.deleteProductImage(imagePath);
                        }
                    }
            );
        } else {
            fileStorageService.deleteProductImage(imagePath);
        }
    }

    @Transactional
    public Product addStock(int id, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Количество поступившего товара должно быть больше нуля"
            );
        }

        Product product = repo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Товар с id " + id + " не найден"
                        )
                );

        int currentStock = product.getStockQuantity() == null
                ? 0
                : product.getStockQuantity();

        product.setStockQuantity(currentStock + quantity);

        return repo.save(product);
    }

    private void copyFormToProduct(ProductForm form, Product product) {
        product.setArtist(form.getArtist().trim());
        product.setName(form.getName().trim());
        product.setCatNo(trimToNull(form.getCatNo()));
        product.setFormat(trimToNull(form.getFormat()));
        product.setNote(trimToNull(form.getNote()));
        product.setPrice(form.getPrice());
        product.setDescription(trimToNull(form.getDescription()));
        product.setStockQuantity(form.getStockQuantity());
    }

    private void validate(ProductForm form) {
        if (form.getArtist() == null || form.getArtist().isBlank()) {
            throw new IllegalArgumentException("Не указан исполнитель");
        }

        if (form.getName() == null || form.getName().isBlank()) {
            throw new IllegalArgumentException("Не указано название товара");
        }

        if (form.getPrice() == null
                || form.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Цена должна быть равна нулю или быть больше нуля"
            );
        }

        if (form.getStockQuantity() == null
                || form.getStockQuantity() < 0) {
            throw new IllegalArgumentException(
                    "Остаток не может быть отрицательным"
            );
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}