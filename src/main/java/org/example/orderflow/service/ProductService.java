package org.example.orderflow.service;
import jakarta.transaction.Transactional;
import org.example.orderflow.dto.CreateProduct;
import org.example.orderflow.dto.UpdateProductPrice;
import org.example.orderflow.dto.response.ProductCatalogResponse;
import org.example.orderflow.dto.response.ProductResponse;
import org.example.orderflow.entity.Category;
import org.example.orderflow.entity.Product;
import org.example.orderflow.repository.CategoryRepository;
import org.example.orderflow.repository.ProductRepository;
import org.example.orderflow.exception.NotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;



    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductResponse createProduct(CreateProduct request ){
        Product product = new Product();
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        product.setCategory(category);
        product.setPrice(request.getPrice());
        product.setName(request.getName());
        return new ProductResponse(productRepository.save(product));
    }
    @CacheEvict(value = "productCatalog", key = "#id")
    @Transactional
    public void updateProductPrice(int id, UpdateProductPrice request) {
        Product product = productRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Продукт не найден")
        );
        product.setPrice(request.getPrice());
    }
    @CacheEvict(value = "productCatalog", key = "#id")
    @Transactional
    public void deleteProductById(int id){
        Product product = productRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Продукт не найден")
        );
        productRepository.delete(product);

    }

    public Page<ProductResponse> getAllProduct(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::new);
    }
    public ProductResponse getById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Продукт не найден"));

        return new ProductResponse(product);
    }

    @Cacheable(value = "productCatalog", key = "#id")
    public ProductCatalogResponse getCatalogById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Продукт не найден"));

        return new ProductCatalogResponse(product);
    }
}
