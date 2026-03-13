package sk.emaa.service;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.CreditTransactionDto;
import sk.emaa.model.entity.tables.CreditTransaction;
import sk.emaa.model.entity.tables.StudentMartialArt;
import sk.emaa.model.entity.tables.records.CreditTransactionRecord;
import sk.emaa.util.AppConstants;

@Service
@RequiredArgsConstructor
public class PaymentService {
	
    private final DSLContext dsl;
	
    public CreditTransactionRecord payForMonth(CreditTransactionDto creditTransactionDto) {
        boolean exists = dsl.fetchExists(
            dsl.selectOne()
               .from(CreditTransaction.CREDIT_TRANSACTION)
               .where(CreditTransaction.CREDIT_TRANSACTION.STUDENT_ID.eq(creditTransactionDto.studentId()))
               .and(CreditTransaction.CREDIT_TRANSACTION.MARTIAL_ART_ID.eq(creditTransactionDto.martialArtId()))
               .and(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION.eq(creditTransactionDto.description()))
               .and(CreditTransaction.CREDIT_TRANSACTION.PAYMENT_TYPE.eq(AppConstants.paymentType_monthly))
        );

        if (exists) {
            throw new IllegalStateException(
                "Platba za mesiac " + creditTransactionDto.description() + " už bola zadaná."
            );
        }

        return dsl.insertInto(CreditTransaction.CREDIT_TRANSACTION)
                  .set(CreditTransaction.CREDIT_TRANSACTION.STUDENT_ID, creditTransactionDto.studentId())
                  .set(CreditTransaction.CREDIT_TRANSACTION.MARTIAL_ART_ID, creditTransactionDto.martialArtId())
                  .set(CreditTransaction.CREDIT_TRANSACTION.AMOUNT, creditTransactionDto.amount())
                  .set(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION, creditTransactionDto.description())
                  .set(CreditTransaction.CREDIT_TRANSACTION.PAYMENT_TYPE, AppConstants.paymentType_monthly)
                  .returning()
                  .fetchOne();
    }
	
    public CreditTransactionRecord payForYear(CreditTransactionDto creditTransactionDto) {
        boolean exists = dsl.fetchExists(
            dsl.selectOne()
               .from(CreditTransaction.CREDIT_TRANSACTION)
               .where(CreditTransaction.CREDIT_TRANSACTION.STUDENT_ID.eq(creditTransactionDto.studentId()))
               .and(CreditTransaction.CREDIT_TRANSACTION.MARTIAL_ART_ID.eq(creditTransactionDto.martialArtId()))
               .and(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION.eq(creditTransactionDto.description()))
               .and(CreditTransaction.CREDIT_TRANSACTION.PAYMENT_TYPE.eq(AppConstants.paymentType_yearly))
        );

        if (exists) {
            throw new IllegalStateException(
                "Platba za rok " + creditTransactionDto.description() + " už bola zadaná."
            );
        }
		
        return dsl.insertInto(CreditTransaction.CREDIT_TRANSACTION)
                  .set(CreditTransaction.CREDIT_TRANSACTION.STUDENT_ID, creditTransactionDto.studentId())
                  .set(CreditTransaction.CREDIT_TRANSACTION.MARTIAL_ART_ID, creditTransactionDto.martialArtId())
                  .set(CreditTransaction.CREDIT_TRANSACTION.AMOUNT, creditTransactionDto.amount())
                  .set(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION, creditTransactionDto.description())
                  .set(CreditTransaction.CREDIT_TRANSACTION.PAYMENT_TYPE, AppConstants.paymentType_yearly)
                  .returning()
                  .fetchOne();
    }
	
    public CreditTransactionDto getLastPayment(int studentId, int martialArtId) {
        // paymentType čítame zo STUDENT_MARTIAL_ART
        String paymentType = dsl.select(StudentMartialArt.STUDENT_MARTIAL_ART.PAYMENT_TYPE)
            .from(StudentMartialArt.STUDENT_MARTIAL_ART)
            .where(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID.eq(studentId))
            .and(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID.eq(martialArtId))
            .fetchOneInto(String.class);

        if (AppConstants.paymentType_monthly.equals(paymentType)) {
            CreditTransactionRecord record = dsl.selectFrom(CreditTransaction.CREDIT_TRANSACTION)
                .where(CreditTransaction.CREDIT_TRANSACTION.STUDENT_ID.eq(studentId))
                .and(CreditTransaction.CREDIT_TRANSACTION.MARTIAL_ART_ID.eq(martialArtId))
                .and(CreditTransaction.CREDIT_TRANSACTION.PAYMENT_TYPE.eq(AppConstants.paymentType_monthly))
                .orderBy(
                    DSL.substring(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION, 4, 4).cast(Integer.class).desc(),
                    DSL.substring(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION, 1, 2).cast(Integer.class).desc()
                )
                .limit(1)
                .fetchOne();
            return record != null ? mapToDto(record) : null;

        } else if (AppConstants.paymentType_yearly.equals(paymentType)) {
            CreditTransactionRecord record = dsl.selectFrom(CreditTransaction.CREDIT_TRANSACTION)
                .where(CreditTransaction.CREDIT_TRANSACTION.STUDENT_ID.eq(studentId))
                .and(CreditTransaction.CREDIT_TRANSACTION.MARTIAL_ART_ID.eq(martialArtId))
                .and(CreditTransaction.CREDIT_TRANSACTION.PAYMENT_TYPE.eq(AppConstants.paymentType_yearly))
                .orderBy(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION.cast(Integer.class).desc())
                .limit(1)
                .fetchOne();
            return record != null ? mapToDto(record) : null;

        } else {
            CreditTransactionRecord record = dsl.selectFrom(CreditTransaction.CREDIT_TRANSACTION)
                .where(CreditTransaction.CREDIT_TRANSACTION.STUDENT_ID.eq(studentId))
                .and(CreditTransaction.CREDIT_TRANSACTION.MARTIAL_ART_ID.eq(martialArtId))
                .and(CreditTransaction.CREDIT_TRANSACTION.PAYMENT_TYPE.eq(paymentType))
                .orderBy(CreditTransaction.CREDIT_TRANSACTION.CREATED_AT.desc())
                .limit(1)
                .fetchOne();
            return record != null ? mapToDto(record) : null;
        }
    }

    private CreditTransactionDto mapToDto(CreditTransactionRecord record) {
        return new CreditTransactionDto(
            record.getId(),
            record.getStudentId(),
            record.getMartialArtId(),
            record.getAmount(),
            record.getDescription(),
            record.getCreatedAt()
        );
    }
}