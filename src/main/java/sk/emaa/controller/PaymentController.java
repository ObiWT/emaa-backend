package sk.emaa.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.CreditTransactionDto;
import sk.emaa.service.PaymentService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {
	
	private final PaymentService paymentService;
	
	@PostMapping("/payment/monthly")
    public ResponseEntity<String> payForMonth(@RequestBody CreditTransactionDto creditTransactionDto) throws IllegalStateException {
        try {
            paymentService.payForMonth(creditTransactionDto);
            return ResponseEntity.ok("Platba za mesiac " + creditTransactionDto.description() + " bola úspešne zadaná");
        } catch (IllegalStateException e) {
            // duplicitná platba alebo iný biznis problém
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/payment/yearly")
    public ResponseEntity<String> payForYear(@RequestBody CreditTransactionDto creditTransactionDto) throws IllegalStateException {
        try {
            paymentService.payForYear(creditTransactionDto);
            return ResponseEntity.ok("Platba za rok " + creditTransactionDto.description() + " bola úspešne zadaná.");
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }
	
	@GetMapping("/payment/{studentId}")
	public CreditTransactionDto getLastPayment(@PathVariable int studentId) {
		return paymentService.getLastPayment(studentId);
	}
	
}
