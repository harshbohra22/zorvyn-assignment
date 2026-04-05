package com.finance.service;

import com.finance.dto.request.RecordRequest;
import com.finance.dto.response.RecordResponse;
import com.finance.entity.FinancialRecord;
import com.finance.enums.RecordType;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.RecordRepository;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;
    private final AuditService auditService;
    private final UserRepository userRepository;

    public RecordResponse createRecord(RecordRequest request, Long userId) {
        FinancialRecord record = FinancialRecord.builder()
                .userId(userId)
                .type(request.getType())
                .category(request.getCategory())
                .amount(request.getAmount())
                .date(request.getDate())
                .description(request.getDescription())
                .isDeleted(false)
                .build();

        recordRepository.save(record);

        userRepository.findById(userId).ifPresentOrElse(user -> {
           auditService.logAction("CREATE", "RECORD", record.getId(), user.getEmail(), "Created record of type " + request.getType() + " for " + request.getAmount());
        }, () -> {
           auditService.logAction("CREATE", "RECORD", record.getId(), "Unknown", "Created record of type " + request.getType() + " for " + request.getAmount());
        });

        return toResponse(record);
    }

    public Page<RecordResponse> getRecords(RecordType type, String category,
                                            LocalDate startDate, LocalDate endDate,
                                            int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return recordRepository.findWithFilters(type, category, startDate, endDate, pageable)
                .map(this::toResponse);
    }

    public RecordResponse getRecordById(Long id) {
        FinancialRecord record = recordRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + id));
        return toResponse(record);
    }

    public RecordResponse updateRecord(Long id, RecordRequest request) {
        FinancialRecord record = recordRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + id));

        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setAmount(request.getAmount());
        record.setDate(request.getDate());
        record.setDescription(request.getDescription());

        recordRepository.save(record);

        userRepository.findById(record.getUserId()).ifPresent(user -> {
           auditService.logAction("UPDATE", "RECORD", record.getId(), user.getEmail(), "Updated record " + id);
        });

        return toResponse(record);
    }

    public void softDeleteRecord(Long id) {
        FinancialRecord record = recordRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + id));
        record.setDeleted(true);
        recordRepository.save(record);

        userRepository.findById(record.getUserId()).ifPresent(user -> {
           auditService.logAction("DELETE", "RECORD", record.getId(), user.getEmail(), "Soft deleted record " + id);
        });
    }

    private RecordResponse toResponse(FinancialRecord record) {
        return RecordResponse.builder()
                .id(record.getId())
                .type(record.getType())
                .category(record.getCategory())
                .amount(record.getAmount())
                .date(record.getDate())
                .description(record.getDescription())
                .userId(record.getUserId())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
