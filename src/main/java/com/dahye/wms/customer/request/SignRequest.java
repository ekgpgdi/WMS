package com.dahye.wms.customer.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "회원가입 요청 모델")
@Getter
@Setter
public class SignRequest extends LoginRequest {
  @Schema(description = "이름", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "REQUIRED_NAME")
  private String name;
}
