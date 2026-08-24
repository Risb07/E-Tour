package com.etour.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etour.entity.Category;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.CategoryRepository;
import com.etour.repository.TourRepository;
import com.etour.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TourRepository tourRepository;

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

        List<Category> categories = categoryRepository.findAll();

        // One grouped query for every category, so the tiles can show a real
        // count instead of the 0 they used to get from the @JsonIgnore'd
        // `tours` collection. Counts ACTIVE tours only, matching what the
        // category page will actually list.
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : tourRepository.countActiveToursByCategory()) {
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }

        categories.forEach(c -> c.setActiveTourCount(counts.getOrDefault(c.getCategoryId(), 0L)));
        return categories;
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
