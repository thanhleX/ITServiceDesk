package com.itservicedesk.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterCriteria {

    @Schema(description = "Từ khóa tìm kiếm (username, họ tên, email)", example = "nam")
    private String keyword;

    @Schema(description = "Lọc theo phòng ban", example = "1")
    private Long departmentId;

    @Schema(description = "Lọc theo vai trò", example = "ROLE_IT_STAFF")
    private String roleCode;

    @Schema(description = "Lọc theo trạng thái (ACTIVE, LOCKED, INACTIVE)", example = "ACTIVE")
    private String status;
}
