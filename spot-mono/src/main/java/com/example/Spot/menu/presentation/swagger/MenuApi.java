package com.example.Spot.menu.presentation.swagger;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.menu.presentation.dto.request.CreateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuHiddenRequestDto;
import com.example.Spot.menu.presentation.dto.request.UpdateMenuRequestDto;
import com.example.Spot.menu.presentation.dto.response.CreateMenuResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuAdminResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuPublicResponseDto;
import com.example.Spot.menu.presentation.dto.response.MenuResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "메뉴", description = "메뉴 관리 API")
public interface MenuApi {

    @Operation(summary = "메뉴 전체 조회", description = "특정 매장의 모든 메뉴를 조회합니다. 권한에 따라 숨김 메뉴 포함 여부가 결정됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    ApiResponse<List<? extends MenuResponseDto>> getMenus(
            @Parameter(description = "매장 ID") @PathVariable UUID storeId,
            @AuthenticationPrincipal CustomUserDetails user);

    @Operation(summary = "메뉴 상세 조회", description = "특정 메뉴의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없음")
    })
    ApiResponse<MenuPublicResponseDto> getMenuDetail(
            @Parameter(description = "메뉴 ID") @PathVariable UUID menuId);

    @Operation(summary = "메뉴 생성", description = "새로운 메뉴를 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ApiResponse<CreateMenuResponseDto> createMenu(
            @Parameter(description = "매장 ID") @PathVariable UUID storeId,
            @RequestBody CreateMenuRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user);

    @Operation(summary = "메뉴 수정", description = "메뉴 정보를 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없음")
    })
    ApiResponse<MenuAdminResponseDto> updateMenu(
            @Parameter(description = "메뉴 ID") @PathVariable UUID menuId,
            @RequestBody UpdateMenuRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user);

    @Operation(summary = "메뉴 삭제", description = "메뉴를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없음")
    })
    ApiResponse<String> deleteMenu(
            @Parameter(description = "메뉴 ID") @PathVariable UUID menuId,
            @AuthenticationPrincipal CustomUserDetails user);

    @Operation(summary = "메뉴 숨김 처리", description = "메뉴를 숨김/숨김 해제 처리합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없음")
    })
    ApiResponse<String> hiddenMenu(
            @Parameter(description = "메뉴 ID") @PathVariable UUID menuId,
            @RequestBody UpdateMenuHiddenRequestDto request,
            @AuthenticationPrincipal CustomUserDetails user);
}
