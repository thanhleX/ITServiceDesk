package com.itservicedesk.modules.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Schema(example = "Tôi đã thử khởi động lại máy nhưng đèn nguồn vẫn nhấp nháy màu cam.")
    private String content;
}
