package com.example.Spot.store.presentation.swagger;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.store.presentation.dto.request.CategoryRequestDTO;
import com.example.Spot.store.presentation.dto.response.CategoryResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "카테고리", description = "카테고리 관리 API")
public interface CategoryApi {

    @Operation(summary = "카테고리 전체 조회", description = "모든 카테고리 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    List<CategoryResponseDTO.CategoryItem> getAll();

    @Operation(summary = "카테고리별 매장 조회", description = "특정 카테고리에 속한 매장 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    List<CategoryResponseDTO.StoreSummary> getStores(
            @Parameter(description = "카테고리 이름") @PathVariable String categoryName);

    @Operation(summary = "카테고리 생성", description = "새로운 카테고리를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    CategoryResponseDTO.CategoryDetail create(
            @Valid @RequestBody CategoryRequestDTO.Create request);

    @Operation(summary = "카테고리 수정", description = "카테고리 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    CategoryResponseDTO.CategoryDetail update(
            @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryRequestDTO.Update request);

    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다. (Soft Delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    void delete(
            @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId,
            @AuthenticationPrincipal CustomUserDetails principal);
}
