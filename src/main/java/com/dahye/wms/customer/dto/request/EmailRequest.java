package com.dahye.wms.customer.dto.request;

import com.dahye.wms.common.util.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "이메일 관련 요청 모델")
@Getter
@Setter
public class EmailRequest {

  @Schema(description = "사용자의 이메일 (예: user@example.com)", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "REQUIRED_EMAIL")
  @Pattern(regexp = ValidationPatterns.EMAIL_REGEX, message = "EMAIL_FORMAT_ERROR")
  private String email;
}
