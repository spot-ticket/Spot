package com.example.Spot.store.presentation.swagger;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.store.presentation.dto.request.StoreCreateRequest;
import com.example.Spot.store.presentation.dto.request.StoreUpdateRequest;
import com.example.Spot.store.presentation.dto.request.StoreUserUpdateRequest;
import com.example.Spot.store.presentation.dto.response.StoreDetailResponse;
import com.example.Spot.store.presentation.dto.response.StoreListResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "매장", description = "매장 관리 API")
public interface StoreApi {

    @Operation(summary = "매장 생성", description = "새로운 매장을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "매장 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ResponseEntity<UUID> createStore(
            @Valid @RequestBody StoreCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal
    );


    @Operation(summary = "매장 상세 조회", description = "특정 매장의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    ResponseEntity<StoreDetailResponse> getStoreDetails(
            @Parameter(description = "매장 ID") @PathVariable UUID storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal
    );

    @Operation(summary = "매장 전체 조회", description = "모든 매장 목록을 페이지네이션으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<Page<StoreListResponse>> getAllStores(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "50") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal
    );

    @Operation(summary = "매장 정보 수정", description = "매장 기본 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    ResponseEntity<Void> updateStore(
            @Parameter(description = "매장 ID") @PathVariable UUID storeId,
            @Valid @RequestBody StoreUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal
    );

    @Operation(summary = "매장 직원 정보 수정", description = "매장 직원 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    ResponseEntity<Void> updateStoreStaff(
            @Parameter(description = "매장 ID") @PathVariable UUID storeId,
            @Valid @RequestBody StoreUserUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal
    );

    @Operation(summary = "매장 삭제", description = "매장을 삭제합니다. (Soft Delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    ResponseEntity<Void> deleteStore(
            @Parameter(description = "매장 ID") @PathVariable UUID storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal
    );

    @Operation(summary = "매장 검색", description = "매장 이름으로 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    ResponseEntity<Page<StoreListResponse>> searchStores(
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "50") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails principal
    );
}
