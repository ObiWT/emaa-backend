package sk.emaa.service;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.CreditTransactionDto;
import sk.emaa.model.entity.tables.CreditTransaction;
import sk.emaa.model.entity.tables.records.CreditTransactionRecord;
import sk.emaa.util.AppConstants;
@Service
@RequiredArgsConstructor
public class PaymentService {
	
	private final DSLContext dsl;
	
	public CreditTransactionRecord payForMonth(CreditTransactionDto creditTransactionDto) {
        return dsl.insertInto(CreditTransaction.CREDIT_TRANSACTION)
                  .set(CreditTransaction.CREDIT_TRANSACTION.STUDENT_ID, creditTransactionDto.studentId())
                  .set(CreditTransaction.CREDIT_TRANSACTION.AMOUNT, AppConstants.monthlyPayment)
                  .set(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION, creditTransactionDto.description())
                  .returning()
                  .fetchOne();
    }
	
	public CreditTransactionDto getLastPayment(int studentId) {
        CreditTransactionRecord record = dsl.selectFrom(CreditTransaction.CREDIT_TRANSACTION)
            .where(CreditTransaction.CREDIT_TRANSACTION.STUDENT_ID.eq(studentId))
            // ORDER BY YEAR/MONTH extrahovane z DESCRIPTION
            .orderBy(
            	DSL.substring(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION, 4, 4).cast(Integer.class).desc(), // YYYY
                DSL.substring(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION, 1, 2).cast(Integer.class).desc()  // MM
            )
            .limit(1)
            .fetchOne();

        if (record == null) {
            return null; // alebo hodiť výnimku, uvidime
        }

        return mapToDto(record);
    }

    private CreditTransactionDto mapToDto(CreditTransactionRecord record) {
        return new CreditTransactionDto(
            record.getId(),
            record.getStudentId(),
            record.getAmount(),
            record.getDescription(),
            record.getCreatedAt()
        );
    }    

}
