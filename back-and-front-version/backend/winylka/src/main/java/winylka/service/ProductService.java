package winylka.service;

import winylka.infra.ProductRepository;
import winylka.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<Product> findAll(String q, Integer maxPrice, String format,
                                 String sort, Integer limit, Integer offset) {
        Stream<Product> s = repo.findAll().stream();

        if (q != null && !q.trim().isEmpty()) {
            String needle = q.trim().toLowerCase();
            s = s.filter(p -> (p.getArtist() + " " + p.getName()).toLowerCase().contains(needle));
        }

        if (maxPrice != null) {
            BigDecimal max = BigDecimal.valueOf(maxPrice);
            s = s.filter(p -> p.getPrice().compareTo(max) <= 0);
        }

        if (format != null && !format.isBlank()) {
            String fmt = format.trim().toLowerCase();
            s = s.filter(p -> p.getFormat() != null && p.getFormat().toLowerCase().equals(fmt));
        }

        List<Product> list = s.collect(Collectors.toList());

        // сортировка
        if (sort != null) {
            switch (sort) {
                case "priceAsc" -> list.sort(Comparator.comparing(Product::getPrice));
                case "priceDesc" -> list.sort(Comparator.comparing(Product::getPrice).reversed());
                case "artistAsc" -> list.sort(Comparator.comparing(p -> safe(p.getArtist())));
                case "random" -> Collections.shuffle(list);
            }
        }

        int off = offset == null ? 0 : Math.max(0, offset);
        int lim = limit == null ? list.size() : Math.max(0, limit);

        if (off >= list.size()) return List.of();
        return list.subList(off, Math.min(list.size(), off + lim));
    }

    public Product findById(int id) {
        return repo.findById(id).orElse(null);
    }

    public Map<Integer, Product> mapByIds(Collection<Integer> ids) {
        return repo.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
    }

    private static String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }
}

