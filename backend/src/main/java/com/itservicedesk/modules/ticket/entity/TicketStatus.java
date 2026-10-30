package com.itservicedesk.modules.ticket.entity;

import lombok.Getter;

@Getter
public enum TicketStatus {
    OPEN("Mới tạo"),
    ASSIGNED("Đã phân công"),
    IN_PROGRESS("Đang xử lý"),
    PENDING("Tạm dừng"),
    RESOLVED("Đã giải quyết"),
    CLOSED("Đã đóng"),
    REOPENED("Mở lại xử lý");

    private final String description;

    TicketStatus(String description) {
        this.description = description;
    }
}
