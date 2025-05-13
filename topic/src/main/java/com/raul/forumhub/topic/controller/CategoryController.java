package com.raul.forumhub.topic.controller;

import com.raul.forumhub.topic.dto.response.CategoryResponseCollection;
import com.raul.forumhub.topic.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forumhub.io/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PreAuthorize("hasRole('ADM') and hasAuthority('SCOPE_category:readAll')")
    @GetMapping("/listAll")
    public ResponseEntity<CategoryResponseCollection> getAllCategories() {
        return ResponseEntity.ok(new CategoryResponseCollection(this.categoryService.getCategories()));
    }
}
