package at.ac.tuwien.sepm.groupphase.backend.mapper;

import at.ac.tuwien.sepm.groupphase.backend.dto.LenderViewDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public abstract class LenderMapper {

    @Named("toDto")
    @Mapping(target = "plz", source = "owner.plz")
    @Mapping(target = "state", source = "owner.state")
    @Mapping(target = "name", source = "owner.name")
    @Mapping(target = "isDeleted", constant = "false")
    protected abstract LenderViewDto entityToDtoBase(Lender lender);

    public LenderViewDto entityToDto(Lender lender) {
        if (lender != null && !lender.isDeleted()) {
            return entityToDtoBase(lender);
        } else {
            return LenderViewDto.builder()
                .id(-1L)
                .name("Gelöschter User")
                .isDeleted(true)
                .build();
        }
    }

}
