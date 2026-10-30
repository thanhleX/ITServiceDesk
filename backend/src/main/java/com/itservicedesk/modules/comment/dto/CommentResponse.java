package com.itservicedesk.modules.comment.dto;

import com.itservicedesk.modules.comment.entity.TicketComment;
import com.itservicedesk.modules.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long ticketId;
    private Long userId;
    private String userFullName;
    private String userRole;
    private String content;
    private LocalDateTime createdAt;

    public static CommentResponse fromEntity(TicketComment comment) {
        String roles = comment.getUser().getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(", "));

        return CommentResponse.builder()
                .id(comment.getId())
                .ticketId(comment.getTicket().getId())
                .userId(comment.getUser().getId())
                .userFullName(comment.getUser().getFullName())
                .userRole(roles)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
