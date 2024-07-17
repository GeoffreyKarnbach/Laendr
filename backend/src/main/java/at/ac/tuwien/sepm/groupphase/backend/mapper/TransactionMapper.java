package at.ac.tuwien.sepm.groupphase.backend.mapper;

import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {TimeslotMapper.class, ReviewMapper.class})
public interface TransactionMapper {

    @Mapping(target = "totalPaid", source = "amountPaid")
    @Mapping(target = "lenderId", expression = "java(transaction.getTimeslot().getOwningLocation().getOwner().getId())")
    @Mapping(target = "locationName", expression = "java(transaction.getTimeslot().getOwningLocation().getName())")
    @Mapping(target = "locationId", expression = "java(transaction.getTimeslot().getOwningLocation().getId())")
    @Mapping(target = "totalConcerned", expression = "java(transaction.getTimeslot().getPrice())")
    @Mapping(target = "locationRemoved", expression = "java(transaction.getTimeslot().getOwningLocation().isRemoved())")
    TransactionDto entityToDto(Transaction transaction);

}
