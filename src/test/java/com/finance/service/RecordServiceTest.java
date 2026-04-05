package com.finance.service;

import com.finance.dto.request.RecordRequest;
import com.finance.dto.response.RecordResponse;
import com.finance.entity.FinancialRecord;
import com.finance.entity.User;
import com.finance.enums.RecordType;
import com.finance.repository.RecordRepository;
import com.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RecordService recordService;

    private FinancialRecord testRecord;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@example.com").build();
        testRecord = FinancialRecord.builder()
                .id(100L)
                .userId(1L)
                .type(RecordType.INCOME)
                .amount(new BigDecimal("1000"))
                .isDeleted(false)
                .build();
    }

    @Test
    void createRecord_Success() {
        RecordRequest request = new RecordRequest();
        request.setType(RecordType.INCOME);
        request.setAmount(new BigDecimal("1000"));
        request.setCategory("Salary");
        request.setDate(LocalDate.now());

        when(recordRepository.save(any(FinancialRecord.class))).thenReturn(testRecord);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(auditService).logAction(anyString(), anyString(), any(), anyString(), anyString());

        RecordResponse response = recordService.createRecord(request, 1L);

        assertNotNull(response);
        verify(recordRepository, times(1)).save(any(FinancialRecord.class));
        verify(auditService, times(1)).logAction(eq("CREATE"), eq("RECORD"), any(), eq("user@example.com"), anyString());
    }

    @Test
    void softDeleteRecord_Success() {
        when(recordRepository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(testRecord));
        when(recordRepository.save(any(FinancialRecord.class))).thenReturn(testRecord);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(auditService).logAction(anyString(), anyString(), any(), anyString(), anyString());

        recordService.softDeleteRecord(100L);

        assertTrue(testRecord.isDeleted());
        verify(recordRepository, times(1)).save(testRecord);
    }
}
