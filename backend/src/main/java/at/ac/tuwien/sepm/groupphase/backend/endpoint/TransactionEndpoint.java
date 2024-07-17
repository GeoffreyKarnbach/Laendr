package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;

import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionCancelDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionCreateDto;

import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionDto;
import at.ac.tuwien.sepm.groupphase.backend.enums.AppRole;
import at.ac.tuwien.sepm.groupphase.backend.enums.TransactionStatus;
import at.ac.tuwien.sepm.groupphase.backend.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/transactions")
@Slf4j
@RequiredArgsConstructor
@Validated
public class TransactionEndpoint {

    private final TransactionService transactionService;

    @Secured("ROLE_RENTER")
    @PostMapping
    @Operation(summary = "Create a new transaction", security = @SecurityRequirement(name = "apiKey"))
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDto startTransaction(@RequestBody TransactionCreateDto transaction) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return transactionService.startTransaction(transaction, authentication.getName());
    }

    @Secured("ROLE_USER")
    @GetMapping(value = "/{id}")
    @Operation(summary = "Get detailed information about a specific transaction", security = @SecurityRequirement(name = "apiKey"))
    public TransactionDto find(@PathVariable Long id) {
        log.info("GET /api/v1/transactions/{}", id);
        return transactionService.findOne(id);
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{id}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Accept a transaction as the lender", security = @SecurityRequirement(name = "apiKey"))
    public void acceptTransaction(@PathVariable Long id) {
        log.info("POST /api/v1/transactions/{}/accept", id);
        transactionService.acceptTransaction(id);
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{id}/cancel-notification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark a cancelled transaction as notified", security = @SecurityRequirement(name = "apiKey"))
    public void recordCancelNotified(@PathVariable Long id) {
        log.info("GET /api/v1/transactions/{}/cancel-notification", id);
        transactionService.recordCancelNotified(id);
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark a transaction as completed by the lender", security = @SecurityRequirement(name = "apiKey"))
    public void recordTransactionComplete(@PathVariable Long id, @NotNull @RequestParam("price") BigDecimal price) {
        log.info("POST /api/v1/transactions/{}/complete", id);
        log.info("price: {}", price);

        transactionService.completeTransaction(id, price);
    }

    @Secured("ROLE_USER")
    @GetMapping(value = "/all/{status}")
    @Operation(summary = "Get all transactions with a specific status", security = @SecurityRequirement(name = "apiKey"))
    public PageableDto<TransactionDto> findAllByStatusForRole(@PathVariable TransactionStatus status,
                                                              @NotNull @RequestParam("role") AppRole role,
                                                              @PositiveOrZero @RequestParam("page") int page,
                                                              @PositiveOrZero @RequestParam("pageSize") int pageSize) {
        log.info("GET /api/v1/transactions/{}", status);
        return transactionService.findAllByStatusForRole(status, role, page, pageSize);
    }

    @Secured("ROLE_USER")
    @GetMapping(value = "/all/cancel-notifications")
    @Operation(summary = "Gets the ids for the cancelled notifications that the user was not informed about yet", security = @SecurityRequirement(name = "apiKey"))
    public List<Long> getIdsForOutstandingCancelNotificationsForRole(@NotNull @RequestParam("role") AppRole role) {
        return transactionService.getIdsOutstandingCancelNotificationsForRole(role);
    }

    @Secured("ROLE_USER")
    @GetMapping(value = "/all/not-reviewed")
    @Operation(summary = "Counts the transactions that are completed but not reviewed yet", security = @SecurityRequirement(name = "apiKey"))
    public int countAllNotReviewedForRole(@NotNull @RequestParam("role") AppRole role) {
        return transactionService.countAllNotReviewedForRole(role);
    }

    @Secured("ROLE_RENTER")
    @DeleteMapping(value = "/timeslot/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel all transactions for a timeslot where the calling user is the renter in the transaction", security = @SecurityRequirement(name = "apiKey"))
    public void cancelTransactionsForTimeslotAsRenter(@PathVariable("id") Long timeslotId) {
        transactionService.cancelTransactionsForTimeslotAsRenter(timeslotId);
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cancels a transaction and frees the related timeslot", security = @SecurityRequirement(name = "apiKey"))
    public void recordTransactionCancelation(@PathVariable Long id, @RequestBody TransactionCancelDto cancelDto) {
        log.info("POST /api/v1/transactions/{}/cancel", id);
        log.info("Reason: {}", cancelDto);

        transactionService.recordTransactionCancelation(cancelDto);
    }

    @Secured("ROLE_LENDER")
    @GetMapping(value = "/timeslot/{id}")
    @Operation(summary = "Gets all ongoing transactions for a given timeslot", security = @SecurityRequirement(name = "apiKey"))
    public PageableDto<TransactionDto> findTransactionsForTimeslot(
        @PathVariable("id") Long timeslotId,
        @PositiveOrZero @RequestParam("page") int page,
        @PositiveOrZero @RequestParam("pageSize") int pageSize) {
        return transactionService.findAllTransactionsForTimeslot(timeslotId, page, pageSize);
    }
}
