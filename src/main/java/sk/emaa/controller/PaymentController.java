package sk.emaa.controller;

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
	
	@PostMapping("/payment")
	public void payForMonth(@RequestBody CreditTransactionDto creditTransactionDto) {
		paymentService.payForMonth(creditTransactionDto);
	}
	
	@GetMapping("/payment/{studentId}")
	public CreditTransactionDto getLastPayment(@PathVariable int studentId) {
		return paymentService.getLastPayment(studentId);
	}
	
}
