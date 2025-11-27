package sk.emaa.service;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.CreditTransactionDto;
import sk.emaa.model.entity.tables.CreditTransaction;
import sk.emaa.model.entity.tables.Student;
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
                  .set(CreditTransaction.CREDIT_TRANSACTION.PAYMENT_TYPE, AppConstants.paymentType_monthly)
                  .returning()
                  .fetchOne();
    }
	
	public CreditTransactionDto getLastPayment(int studentId) {
		// najprv zisti typ platby študenta
		String paymentType = dsl.select(Student.STUDENT.PAYMENT_TYPE)
			.from(Student.STUDENT)
			.where(Student.STUDENT.ID.eq(studentId))
			.fetchOneInto(String.class);

		if (AppConstants.paymentType_monthly.equals(paymentType)) {
		    // pre mesačných platcov triedime podľa MM/RRRR z description
		    CreditTransactionRecord record = dsl.selectFrom(CreditTransaction.CREDIT_TRANSACTION)
		        .where(CreditTransaction.CREDIT_TRANSACTION.STUDENT_ID.eq(studentId)
		        .and(CreditTransaction.CREDIT_TRANSACTION.PAYMENT_TYPE.eq("MONTHLY")))
		        .orderBy(
		            DSL.substring(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION, 4, 4).cast(Integer.class).desc(), // YYYY
		            DSL.substring(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION, 1, 2).cast(Integer.class).desc()  // MM
		        )
		        .limit(1)
		        .fetchOne();

		    return record != null ? mapToDto(record) : null;
		} else {
		    // pre kreditových a iných berieme poslednú transakciu podľa created_at
		    CreditTransactionRecord record = dsl.selectFrom(CreditTransaction.CREDIT_TRANSACTION)
		        .where(CreditTransaction.CREDIT_TRANSACTION.STUDENT_ID.eq(studentId)
		               .and(CreditTransaction.CREDIT_TRANSACTION.PAYMENT_TYPE.eq(paymentType)))
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
            record.getAmount(),
            record.getDescription(),
            record.getCreatedAt()
        );
    }    

}
