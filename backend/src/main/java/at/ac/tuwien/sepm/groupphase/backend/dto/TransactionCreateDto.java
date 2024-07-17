package at.ac.tuwien.sepm.groupphase.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreateDto {

    private long timeslotId;

    @NotNull(message = "Initial message must not be null")
    private String initialMessage;

}
