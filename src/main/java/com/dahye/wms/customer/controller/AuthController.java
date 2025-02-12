package com.dahye.wms.customer.controller;

import com.dahye.wms.common.domain.ResponseCode;
import com.dahye.wms.common.dto.response.ServerResponse;
import com.dahye.wms.customer.request.EmailRequest;
import com.dahye.wms.customer.request.EmailVerifyRequest;
import com.dahye.wms.customer.request.LoginRequest;
import com.dahye.wms.customer.request.SignRequest;
import com.dahye.wms.customer.service.AuthService;
import com.dahye.wms.customer.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AUTH")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final EmailService emailService;

  @Operation(
          summary = "이메일 비밀번호로 로그인",
          description = "사용자가 이메일과 비밀번호로 로그인합니다. 성공하면 JWT 토큰을 반환합니다."
  )
  @ApiResponse(responseCode = "200", description = "성공적으로 로그인되어 JWT 토큰 반환",
          content = @Content(schema = @Schema(implementation = String.class)))
  @PostMapping("/login")
  public ServerResponse<String> login(
          @Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {

    if (bindingResult != null && bindingResult.hasErrors()) {
      throw new IllegalArgumentException(bindingResult.getAllErrors().get(0).getDefaultMessage());
    }

    return ServerResponse.successResponse(
        authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword()));
  }

  @Operation(
          summary = "이메일로 인증 코드 요청",
          description = "사용자가 이메일로 인증 코드를 요청합니다. API 응답은 인증 번호 (테스트 위함)  "
  )
  @ApiResponse(responseCode = "200", description = "인증 코드가 성공적으로 이메일로 발송되고 반환됨",
          content = @Content(schema = @Schema(implementation = String.class)))
  @PostMapping("/send-verification-code")
  public ServerResponse<String> sendVerificationCode(
          @RequestBody @Valid EmailRequest emailRequest, BindingResult bindingResult) {

    if (bindingResult != null && bindingResult.hasErrors()) {
      throw new IllegalArgumentException(bindingResult.getAllErrors().get(0).getDefaultMessage());
    }

    if(authService.checkEmail(emailRequest.getEmail())) {
      return ServerResponse.errorResponse(ResponseCode.EXIST_EMAIL);
    }

    String code = authService.generateVerificationCode(emailRequest.getEmail());
    emailService.sendMail(emailRequest.getEmail(), code);

    return ServerResponse.successResponse(code);
  }

  @Operation(
          summary = "인증 코드 확인",
          description = "사용자가 입력한 인증 코드를 확인하여 유효한지 검사합니다. 인증 코드가 유효하지 않으면 오류를 반환합니다."
  )
  @ApiResponse(responseCode = "200", description = "인증 코드 확인 성공",
          content = @Content(schema = @Schema(implementation = ResponseCode.class)))
  @ApiResponse(responseCode = "400", description = "인증 코드 오류 (입력한 코드와 일치하지 않음)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @PostMapping("/verify-code")
  public ServerResponse<ResponseCode> checkVerificationCode(
          @RequestBody @Valid EmailVerifyRequest emailVerifyRequest, BindingResult bindingResult) {

    if (bindingResult != null && bindingResult.hasErrors()) {
      throw new IllegalArgumentException(bindingResult.getAllErrors().get(0).getDefaultMessage());
    }

    final boolean verify =
        authService.verifyCertCode(emailVerifyRequest.getEmail(), emailVerifyRequest.getCertCode());

    if (!verify) {
      throw new IllegalArgumentException("VERIFIED_CODE_ERROR");
    }

    return ServerResponse.successResponse(ResponseCode.SUCCESS);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
          summary = "회원가입 응답 : jwt",
          description = "사용자가 회원가입을 하면 JWT 토큰이 발급됩니다."
  )
  @ApiResponse(
          responseCode = "201",
          description = "회원가입 성공. JWT 토큰이 발급됨.",
          content = @Content(schema = @Schema(implementation = String.class))
  )
  @ApiResponse(
          responseCode = "400",
          description = "인증 받지 않은 이메일로 가입 시도",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
  )
  @ApiResponse(
          responseCode = "409",
          description = "이미 존재하는 이메일로 가입 시도",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
  )
  @PostMapping("/sign")
  public ServerResponse<String> sign(
          @Valid @RequestBody SignRequest signRequest, BindingResult bindingResult) {

    if (bindingResult != null && bindingResult.hasErrors()) {
      throw new IllegalArgumentException(bindingResult.getAllErrors().get(0).getDefaultMessage());
    }

    return ServerResponse.successResponse(authService.sign(signRequest));
  }
}
