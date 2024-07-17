package at.ac.tuwien.sepm.groupphase.backend.mapper;

import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewCreationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Review;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewLocation;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewRenter;
import org.mapstruct.Mapper;

@Mapper
public interface ReviewMapper {

    ReviewDto entityToDto(Review review);

    ReviewRenter creationDtoToRenterEntity(ReviewCreationDto creationDto);

    ReviewLocation creationDtoToLocationEntity(ReviewCreationDto creationDto);
}
