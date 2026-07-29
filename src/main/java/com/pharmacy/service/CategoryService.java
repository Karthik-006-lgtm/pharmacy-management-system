package com.pharmacy.service;

import com.pharmacy.dto.CategoryDto;
import com.pharmacy.entity.Category;
import com.pharmacy.exception.DuplicateResourceException;
import com.pharmacy.exception.ResourceNotFoundException;
import com.pharmacy.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
    
    @Transactional
    public Category createCategory(CategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Category already exists");
        }
        
        Category category = Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .active(true)
                .build();
        
        return categoryRepository.save(category);
    }
    
    @Transactional
    public Category updateCategory(Long id, CategoryDto dto) {
        Category category = findById(id);
        
        if (!category.getName().equals(dto.getName()) && categoryRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Category name already exists");
        }
        
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setActive(dto.getActive() != null ? dto.getActive() : true);
        
        return categoryRepository.save(category);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findById(id);
        category.setActive(false);
        categoryRepository.save(category);
    }
}
