package org.example.orderflow.service;

import org.example.orderflow.dto.CreateCategory;
import org.example.orderflow.dto.response.CategoryResponse;
import org.example.orderflow.entity.Category;
import org.example.orderflow.exception.NotFoundException;
import org.example.orderflow.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    public CategoryResponse createCategory(CreateCategory request){
        Category category = new Category();
        category.setName(request.getName());
        return new CategoryResponse(categoryRepository.save(category));
    }
    public Page<CategoryResponse> getAllCategory(Pageable pageable){
        return categoryRepository.findAll(pageable).map(CategoryResponse::new);
    }
    public void deleteById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));

        categoryRepository.delete(category);
    }
    public CategoryResponse getById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));

        return new CategoryResponse(category);
    }
}
