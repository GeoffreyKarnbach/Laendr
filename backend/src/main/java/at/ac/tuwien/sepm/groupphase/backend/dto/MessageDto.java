package at.ac.tuwien.sepm.groupphase.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

    private Long id;

    private LocalDateTime publishedAt;

    @NotNull(message = "Title must not be null")
    @Size(max = 100)
    private String title;

    @NotNull(message = "Summary must not be null")
    @Size(max = 500)
    private String summary;

    @NotNull(message = "Text must not be null")
    @Size(max = 10000)
    private String text;

}