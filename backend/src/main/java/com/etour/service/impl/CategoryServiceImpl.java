package com.etour.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etour.entity.Category;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.CategoryRepository;
import com.etour.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Category createCategory(Category category) {

        if (category.getParentCategoryId() != null) {

            Category parent = categoryRepository.findById(category.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found with id : "
                                    + category.getParentCategoryId()));

            category.setParentCategory(parent);
        }

        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategories() {

        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(Long id) {

        return categoryRepository.findById(id)

                .orElseThrow(() ->

                new ResourceNotFoundException(
                        "Category not found with id : " + id)

                );

    }

    @Override
    public Category updateCategory(
            Long id,
            Category categoryRequest) {

        Category existingCategory = categoryRepository.findById(id)

                .orElseThrow(() ->

                new ResourceNotFoundException(
                        "Category not found with id : " + id)

                );

        if (categoryRequest.getParentCategoryId() != null) {

            Category parent = categoryRepository.findById(categoryRequest.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found"));

            existingCategory.setParentCategory(parent);

        } else {

            existingCategory.setParentCategory(null);
        }

        existingCategory.setCategoryName(
                categoryRequest.getCategoryName());

        existingCategory.setDescription(
                categoryRequest.getDescription());

        existingCategory.setImageUrl(
                categoryRequest.getImageUrl());

        existingCategory.setCategoryCode(
                categoryRequest.getCategoryCode());

        existingCategory.setIsFeatured(
                categoryRequest.getIsFeatured());

        existingCategory.setStatus(
                categoryRequest.getStatus());

        return categoryRepository.save(existingCategory);

    }

    @Override
    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)

                .orElseThrow(() ->

                new ResourceNotFoundException(
                        "Category not found with id : " + id)

                );

        categoryRepository.delete(category);

    }

}
