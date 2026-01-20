package com.example.Spot.user.presentation.swagger;

import org.springframework.web.bind.annotation.RequestBody;

import com.example.Spot.user.presentation.dto.request.JoinDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "회원가입", description = "회원가입 API")
public interface JoinApi {

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    void joinProcess(@RequestBody JoinDTO joinDTO);
}
