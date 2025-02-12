package com.dahye.wms.customer.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "휴대폰 인증 요청 모델")
@Getter
@Setter
public class EmailVerifyRequest extends EmailRequest {

  @Schema(description = "인증 코드", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "CHECK_CERT_CODE")
  private String certCode;
}
