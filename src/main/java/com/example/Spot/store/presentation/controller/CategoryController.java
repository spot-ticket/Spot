package com.example.Spot.store.presentation.controller;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.store.application.service.CategoryService;
import com.example.Spot.store.presentation.dto.request.CategoryRequestDTO;
import com.example.Spot.store.presentation.dto.response.CategoryResponseDTO;
import com.example.Spot.store.presentation.swagger.CategoryApi;
import com.example.Spot.user.domain.entity.UserEntity;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController implements CategoryApi {

    private final CategoryService categoryService;

    @Override
    @GetMapping
    public List<CategoryResponseDTO.CategoryItem> getAll() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(auth);

        return categoryService.getAll();
    }

    @Override
    @GetMapping("/{categoryName}/stores")
    public List<CategoryResponseDTO.StoreSummary> getStores(@PathVariable String categoryName) {
        return categoryService.getStoresByCategoryName(categoryName);
    }

    @Override
    @PreAuthorize("hasAnyRole('MASTER','MANAGER','OWNER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDTO.CategoryDetail create(@RequestBody @Valid CategoryRequestDTO.Create request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(auth);

        return categoryService.create(request);
    }

    @Override
    @PreAuthorize("hasAnyRole('MASTER','MANAGER')")
    @PatchMapping("/{categoryId}")
    public CategoryResponseDTO.CategoryDetail update(
            @PathVariable UUID categoryId,
            @RequestBody @Valid CategoryRequestDTO.Update request
    ) {
        return categoryService.update(categoryId, request);
    }

    @Override
    @PreAuthorize("hasAnyRole('MASTER','MANAGER')")
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID categoryId, @AuthenticationPrincipal CustomUserDetails principal) {
        UserEntity user = principal.getUserEntity();
        categoryService.delete(categoryId, user);
    }
}
