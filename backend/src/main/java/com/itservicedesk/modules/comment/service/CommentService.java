package com.itservicedesk.modules.comment.service;

import com.itservicedesk.modules.comment.dto.CommentResponse;
import com.itservicedesk.modules.comment.dto.CreateCommentRequest;

import java.util.List;

public interface CommentService {

    CommentResponse addComment(Long ticketId, CreateCommentRequest request);

    List<CommentResponse> getTicketComments(Long ticketId);
}
