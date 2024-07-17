package at.ac.tuwien.sepm.groupphase.backend.mapper;

import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationSummaryDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationSummary;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

@Mapper
public interface ReputationSummaryMapper {

    @Named("toDto")
    ReputationSummaryDto entityToDto(ReputationSummary summary);

    @IterableMapping(qualifiedByName = "toDto")
    List<ReputationSummaryDto> entityToDto(List<ReputationSummary> summaries);

}
