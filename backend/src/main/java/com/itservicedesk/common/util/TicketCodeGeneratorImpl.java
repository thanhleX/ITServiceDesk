package com.itservicedesk.common.util;

import com.itservicedesk.modules.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TicketCodeGeneratorImpl implements TicketCodeGenerator {

    private final TicketRepository ticketRepository;

    @Override
    public synchronized String generateCode() {
        int currentYear = LocalDate.now().getYear();
        LocalDateTime startOfYear = LocalDateTime.of(currentYear, 1, 1, 0, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(currentYear, 12, 31, 23, 59, 59);

        long count = ticketRepository.countByCreatedAtBetween(startOfYear, endOfYear);
        long sequence = count + 1;

        String code = String.format("TCK-%d-%05d", currentYear, sequence);
        while (ticketRepository.existsByTicketCode(code)) {
            sequence++;
            code = String.format("TCK-%d-%05d", currentYear, sequence);
        }

        return code;
    }
}
