package com.itservicedesk.modules.ticket.service;

import com.itservicedesk.common.dto.PagedResponse;
import com.itservicedesk.modules.ticket.dto.*;
import org.springframework.data.domain.Pageable;

public interface TicketService {

    TicketResponse createTicket(CreateTicketRequest request);

    PagedResponse<TicketResponse> getTickets(TicketFilterCriteria criteria, Pageable pageable);

    TicketDetailResponse getTicketDetail(Long id);

    TicketResponse updateTicket(Long id, UpdateTicketRequest request);

    TicketResponse assignTicket(Long id, AssignTicketRequest request);

    TicketResponse updateTicketStatus(Long id, UpdateTicketStatusRequest request);

    TicketResponse reopenTicket(Long id, ReopenTicketRequest request);
}
