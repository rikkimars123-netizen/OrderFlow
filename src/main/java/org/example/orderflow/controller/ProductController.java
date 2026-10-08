package org.example.orderflow.controller;


import jakarta.validation.Valid;
import org.example.orderflow.dto.CreateProduct;
import org.example.orderflow.dto.UpdateProductPrice;
import org.example.orderflow.dto.response.ProductCatalogResponse;
import org.example.orderflow.dto.response.ProductResponse;
import org.example.orderflow.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springdoc.core.annotations.ParameterObject;
@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @GetMapping
    public Page<ProductResponse> getProduct(@ParameterObject Pageable pageable){
        return productService.getAllProduct(pageable);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable int id){
        return  ResponseEntity.ok(productService.getById(id));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable int id,
                                              @Valid @RequestBody UpdateProductPrice request){
        productService.updateProductPrice(id,request);
        return ResponseEntity.ok().build();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProduct request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));

    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id){
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();

    }
    @GetMapping("/{id}/catalog")
    public ResponseEntity<ProductCatalogResponse> getCatalogById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(productService.getCatalogById(id));
    }

}
