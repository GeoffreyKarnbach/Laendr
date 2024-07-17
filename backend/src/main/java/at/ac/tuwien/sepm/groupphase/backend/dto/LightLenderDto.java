package at.ac.tuwien.sepm.groupphase.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LightLenderDto {
    private long id;
    private String name;
    private boolean isDeleted;
    private String phone;
    private String email;
    private ReputationDto reputation;
}
