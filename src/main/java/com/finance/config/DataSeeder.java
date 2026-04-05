package com.finance.config;

import com.finance.entity.FinancialRecord;
import com.finance.entity.User;
import com.finance.enums.RecordType;
import com.finance.enums.Role;
import com.finance.repository.RecordRepository;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@finance.com")) {
            User admin = User.builder()
                    .name("System Admin")
                    .email("admin@finance.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            log.info("Default admin user created: admin@finance.com / admin123");

            User analyst = User.builder()
                    .name("Data Analyst")
                    .email("analyst@finance.com")
                    .password(passwordEncoder.encode("analyst123"))
                    .role(Role.ANALYST)
                    .isActive(true)
                    .build();
            userRepository.save(analyst);

            User viewer = User.builder()
                    .name("Report Viewer")
                    .email("viewer@finance.com")
                    .password(passwordEncoder.encode("viewer123"))
                    .role(Role.VIEWER)
                    .isActive(true)
                    .build();
            userRepository.save(viewer);

            LocalDate now = LocalDate.now();
            seedRecord(admin.getId(), RecordType.INCOME, "Salary", "5000.00", now.minusDays(30));
            seedRecord(admin.getId(), RecordType.INCOME, "Freelance", "1500.00", now.minusDays(20));
            seedRecord(admin.getId(), RecordType.EXPENSE, "Rent", "1500.00", now.minusDays(28));
            seedRecord(admin.getId(), RecordType.EXPENSE, "Groceries", "450.50", now.minusDays(25));
            seedRecord(admin.getId(), RecordType.EXPENSE, "Utilities", "200.00", now.minusDays(15));
            seedRecord(admin.getId(), RecordType.EXPENSE, "Entertainment", "150.00", now.minusDays(10));
            
            seedRecord(admin.getId(), RecordType.INCOME, "Salary", "5000.00", now.minusDays(60));
            seedRecord(admin.getId(), RecordType.EXPENSE, "Rent", "1500.00", now.minusDays(58));
            seedRecord(admin.getId(), RecordType.EXPENSE, "Groceries", "420.00", now.minusDays(50));
            
            log.info("Rich seed data created (users + records).");
        }
    }

    private void seedRecord(Long userId, RecordType type, String category, String amount, LocalDate date) {
        FinancialRecord record = FinancialRecord.builder()
                .userId(userId)
                .type(type)
                .category(category)
                .amount(new BigDecimal(amount))
                .date(date)
                .description("Seeded " + category)
                .isDeleted(false)
                .build();
        recordRepository.save(record);
    }
}
