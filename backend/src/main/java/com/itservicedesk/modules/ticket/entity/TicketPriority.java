package com.itservicedesk.modules.ticket.entity;

import lombok.Getter;

@Getter
public enum TicketPriority {
    LOW(72, "Thấp"),
    MEDIUM(48, "Trung bình"),
    HIGH(24, "Cao"),
    CRITICAL(8, "Khẩn cấp");

    private final int slaHours;
    private final String description;

    TicketPriority(int slaHours, String description) {
        this.slaHours = slaHours;
        this.description = description;
    }
}
