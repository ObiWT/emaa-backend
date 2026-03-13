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
    public ResponseEntity<Void> payForMonth(@RequestBody CreditTransactionDto creditTransactionDto) throws IllegalStateException {
        try {
            paymentService.payForMonth(creditTransactionDto);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            // duplicitná platba alebo iný biznis problém
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/payment/yearly")
    public ResponseEntity<Void> payForYear(@RequestBody CreditTransactionDto creditTransactionDto) throws IllegalStateException {
        try {
            paymentService.payForYear(creditTransactionDto);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
	
    @GetMapping("/payment/{studentId}/{martialArtId}")
    public CreditTransactionDto getLastPayment(
            @PathVariable int studentId,
            @PathVariable int martialArtId) {
        return paymentService.getLastPayment(studentId, martialArtId);
    }
	
}
