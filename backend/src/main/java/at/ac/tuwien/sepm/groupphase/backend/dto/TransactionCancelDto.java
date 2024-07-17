package at.ac.tuwien.sepm.groupphase.backend.dto;

import at.ac.tuwien.sepm.groupphase.backend.enums.CancelReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCancelDto {

    private Long transactionId;

    private String cancelMessage;

    private CancelReason cancelReason;
}
