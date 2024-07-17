package at.ac.tuwien.sepm.groupphase.backend.dto;

import at.ac.tuwien.sepm.groupphase.backend.enums.AustriaState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LenderViewDto {

    private long id;
    private String name;
    private boolean isDeleted;
    private String phone;
    private String email;
    private String description;
    private PlzDto plz;
    private AustriaState state;
    private LocalDateTime createdAt;
    private boolean callerIsThisLender;
    private ReputationDto reputation;

}
