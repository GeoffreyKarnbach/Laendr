package at.ac.tuwien.sepm.groupphase.backend.mapper;

import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Reputation;
import org.mapstruct.Mapper;

@Mapper
public interface ReputationMapper {

    ReputationDto entityToDto(Reputation reputation);

}
