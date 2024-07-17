package at.ac.tuwien.sepm.groupphase.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreationDto {
    @Min(0)
    @Max(4)
    private int rating;

    @Size(max = 200, message = "Kommentar darf nicht länger als 200 Zeichen sein.")
    private String comment;

    @NotNull(message = "Transaktion für Bewertung muss angegeben werden.")
    private Long transactionId;
}
