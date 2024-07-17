package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.dto.LightLenderDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationCreationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationFilterDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationForLenderSearchDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationTagCollectionDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.TransactionCancelDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorRestDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.LocationTag;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationLender;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationLocation;
import at.ac.tuwien.sepm.groupphase.backend.enums.CancelReason;
import at.ac.tuwien.sepm.groupphase.backend.enums.LocationSortingCriterion;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.mapper.LocationMapper;
import at.ac.tuwien.sepm.groupphase.backend.mapper.ReputationMapper;
import at.ac.tuwien.sepm.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.LocationTagRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.PlzRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationLocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.LocationService;
import at.ac.tuwien.sepm.groupphase.backend.service.ReputationService;
import at.ac.tuwien.sepm.groupphase.backend.service.TransactionService;
import at.ac.tuwien.sepm.groupphase.backend.service.UserService;
import at.ac.tuwien.sepm.groupphase.backend.service.validator.LocationValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final UserService userService;
    private final LenderRepository lenderRepository;
    private final LocationTagRepository locationTagRepository;
    private final LocationMapper locationMapper;
    private final ReputationLocationRepository reputationRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final PlzRepository plzRepository;
    private final ReputationService reputationService;
    private final ReputationMapper reputationMapper;
    private final TransactionService transactionService;

    private final LocationValidator locationValidator;

    @Override
    public PageableDto<LocationDto> searchByName(String name, int page, int pageSize, LocationSortingCriterion sortingCriterion) {
        var pageRequest = PageRequest.of(
            page, pageSize,
            Sort.by(sortingCriterion.getDirection(), sortingCriterion.getAttribute()).and(
                Sort.by(Sort.Direction.ASC, "createdAt") // tie breaking
            ).and(
                Sort.by(Sort.Direction.ASC, "name") // tie breaking
            )
        );
        Page<Location> result;
        if (sortingCriterion.getAttribute().startsWith("price")) {
            result = locationRepository.findAllByFilterParamsSortByPrice(LocationFilterDto.builder().searchString(name).build(), pageRequest);
        } else {
            result = locationRepository.findAllByNameOrOwnerNameContaining(name, pageRequest);
        }
        var locations = result.stream().map(locationMapper::entityToDto).toList();
        return new PageableDto<>(result.getTotalElements(), result.getTotalPages(), result.getNumberOfElements(), locations);
    }

    @Override
    public PageableDto<LocationDto> filter(LocationFilterDto locationFilterDto, int page, int pageSize, LocationSortingCriterion sortingCriterion) {
        var pageRequest = PageRequest.of(
            page, pageSize,
            Sort.by(sortingCriterion.getDirection(), sortingCriterion.getAttribute()).and(
                Sort.by(Sort.Direction.ASC, "createdAt") // tie breaking
            ).and(
                Sort.by(Sort.Direction.ASC, "name") // tie breaking
            )
        );
        locationValidator.validateLocationForFilter(locationFilterDto);
        Page<Location> result;
        if (sortingCriterion.getAttribute().startsWith("price")) {
            result = locationRepository.findAllByFilterParamsSortByPrice(locationFilterDto, pageRequest);
        } else {
            result = locationRepository.findAllByFilterParams(locationFilterDto, pageRequest);
        }
        var locations = result.stream().map(locationMapper::entityToDto).toList();
        return new PageableDto<>(result.getTotalElements(), result.getTotalPages(), result.getNumberOfElements(), locations);
    }

    @Override
    public PageableDto<LocationDto> searchByLender(LocationForLenderSearchDto searchDto) {
        var lenderId = searchDto.getId();
        locationValidator.validateLocationsForLenderRequest(lenderId, searchDto.isIncludeRemovedLocations());

        Page<Location> result;
        var sortingCriterion = searchDto.getSort();
        var pageRequest = PageRequest.of(
            searchDto.getPage(), searchDto.getPageSize(),
            Sort.by(sortingCriterion.getDirection(), sortingCriterion.getAttribute()).and(
                Sort.by(Sort.Direction.ASC, "createdAt") // tie breaking
            ).and(
                Sort.by(Sort.Direction.ASC, "name") // tie breaking
            )
        );

        if (sortingCriterion.getAttribute().startsWith("price")) {
            result = locationRepository.findAllByOwnerIdSortByPrice(lenderId, searchDto.isIncludeRemovedLocations(), pageRequest);
        } else {
            result = locationRepository.findAllByOwnerId(lenderId, searchDto.isIncludeRemovedLocations(), pageRequest);
        }

        var locations = result.stream().map(locationMapper::entityToDto).toList();
        return new PageableDto<>(result.getTotalElements(), result.getTotalPages(), result.getNumberOfElements(), locations);
    }

    private void setTagsForLocation(Location location, List<String> tagsForLocation) {

        Set<LocationTag> tags = new HashSet<>();
        if (tagsForLocation.size() != 0) {
            for (String tag : tagsForLocation) {
                LocationTag locationTag = locationTagRepository.findByName(tag).get();
                tags.add(locationTag);
            }
        }

        location.setTags(tags);
    }

    @Override
    public LocationDto createLocation(LocationCreationDto locationDto, String user) {

        locationValidator.validateLocationForCreation(locationDto);
        if (locationDto.getTags() != null && locationDto.getTags().size() != 0) {
            locationValidator.validateLocationTags(locationDto.getTags());
        }

        Location location = locationMapper.dtoToEntity(locationDto);
        location.setPlz(plzRepository.findById(locationDto.getPlz().getPlz()).get());

        ApplicationUser applicationUser = userService.findApplicationUserByEmail(user);
        Lender lender = lenderRepository.findById(applicationUser.getId()).orElseThrow(
            () -> new ValidationException(new ValidationErrorRestDto("User with id " + applicationUser.getId() + " is not a lender", null))
        );

        location.setOwner(lender);

        ReputationLocation reputationLocation = reputationService.newLocationReputationEntity();

        reputationLocation = reputationRepository.save(reputationLocation);
        location.setReputation(reputationLocation);

        if (locationDto.getTags() != null) {
            setTagsForLocation(location, locationDto.getTags());
        }

        Location created = locationRepository.save(location);

        return locationMapper.entityToDto(created);
    }

    private Location getLocationEntityFromId(Long id) {
        Optional<Location> location = locationRepository.findById(id);
        if (location.isEmpty() || location.get().isRemoved()) {
            throw new NotFoundException("Location with id " + id + " not found");
        }
        return location.get();
    }

    @Override
    public LocationDto getLocationById(Long id, String user) {
        Location location = getLocationEntityFromId(id);

        LocationDto toReturn = locationMapper.entityToDto(location);
        toReturn.setCallerIsOwner(false);
        Optional<ApplicationUser> applicationUser = applicationUserRepository.findApplicationUserByEmail(user);
        if (applicationUser.isPresent()) {
            if (location.getOwner().getId().equals(applicationUser.get().getId())) {
                toReturn.setCallerIsOwner(true);
            }
        }

        ReputationLender reputationLender = location.getOwner().getReputation();
        Lender owner = location.getOwner();
        if (owner.isDeleted()) {
            toReturn.setLender(LightLenderDto.builder()
                .id(-1L)
                .name("Gelöschter User")
                .isDeleted(true)
                .build());
        } else {
            LightLenderDto lender = LightLenderDto.builder()
                .id(owner.getId())
                .name(owner.getOwner().getName())
                .email(owner.getEmail())
                .phone(owner.getPhone())
                .reputation(
                    reputationLender != null ? reputationMapper.entityToDto(reputationLender) : null
                )
                .build();
            toReturn.setLender(lender);
        }

        Set<LocationTag> tags = location.getTags();

        List<String> tagNames = new ArrayList<>();
        if (tags != null && tags.size() != 0) {
            for (LocationTag tag : tags) {
                tagNames.add(tag.getName());
            }
        }

        toReturn.setTags(tagNames);

        return toReturn;
    }

    @Override
    public LocationDto updateLocation(Long id, String user, LocationDto locationDto) {
        // Verify that user is owner of location
        Location currentLocation = getLocationEntityFromId(id);
        ApplicationUser applicationUser = userService.findApplicationUserByEmail(user);

        if (!currentLocation.getOwner().getId().equals(applicationUser.getId())) {
            throw new ValidationException(new ValidationErrorRestDto("User with id " + applicationUser.getId() + " is not the owner of the location to update (id= " + id + ")", null));
        }

        locationValidator.validateLocationForUpdate(locationDto);
        if (locationDto.getTags() != null && locationDto.getTags().size() != 0) {
            locationValidator.validateLocationTags(locationDto.getTags());
        }

        Location updatedLocation = locationMapper.mapUpdateToLocation(currentLocation, locationDto);


        if (locationDto.getTags() != null) {
            setTagsForLocation(updatedLocation, locationDto.getTags());
        }

        Location savedLocation = locationRepository.save(updatedLocation);
        return locationMapper.entityToDto(savedLocation);
    }

    @Override
    @Transactional
    public LocationDto removeLocation(Long id, String user) {
        // Verify that user is owner of location
        Location location = getLocationEntityFromId(id);
        ApplicationUser applicationUser = userService.findApplicationUserByEmail(user);

        if (!location.getOwner().getId().equals(applicationUser.getId())) {
            throw new ValidationException(new ValidationErrorRestDto("Benutzer mit der id " + applicationUser.getId() + " ist nicht der Besitzer der zu löschenden Location (id= " + id + ")", null));
        }

        var timeslots = location.getTimeslots();
        if (timeslots != null) {
            for (var timeslot : timeslots) {

                var transactions = timeslot.getTransaction();
                if (transactions != null) {
                    var list = transactions.stream().toList();
                    for (int i = 0; i < list.size(); i++) {
                        var transaction = list.get(i);
                        if ((transaction.getCancelled() == null || !transaction.getCancelled()) && transaction.getCompletedAt() == null) {
                            transactionService.recordTransactionCancelation(TransactionCancelDto.builder()
                                .cancelMessage("Die dazugehörige Location wurde gelöscht.")
                                .transactionId(transaction.getId())
                                .cancelReason(CancelReason.LOCATION_REMOVED)
                                .build());
                        }
                    }
                }
            }
        }

        location.setRemoved(true);

        Location returnedLocation = locationRepository.save(location);

        return locationMapper.entityToDto(returnedLocation);
    }

    @Override
    public void removeAllExistingLocationsForLender(String email) {
        locationRepository.findAllExistingForLender(email).forEach((location) -> removeLocation(location.getId(), email));
    }

    @Override
    public LocationTagCollectionDto getAllTags() {
        List<String> tags = locationTagRepository.findAll().stream().map(LocationTag::getName).toList();
        return LocationTagCollectionDto.builder().tags(tags).build();
    }
}
