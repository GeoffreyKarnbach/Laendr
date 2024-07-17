package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.dto.PageableDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewCountDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewCreationDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReviewDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ValidationErrorRestDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Transaction;
import at.ac.tuwien.sepm.groupphase.backend.exception.AccessForbiddenException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.mapper.ReviewMapper;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReviewLocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReviewRenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TransactionRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.ReputationService;
import at.ac.tuwien.sepm.groupphase.backend.service.ReviewService;
import at.ac.tuwien.sepm.groupphase.backend.util.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewLocationRepository reviewLocationRepository;
    private final ReviewRenterRepository reviewRenterRepository;
    private final TransactionRepository transactionRepository;
    private final ReviewMapper reviewMapper;

    private final ReputationService reputationService;

    @Override
    public PageableDto<ReviewDto> getAllByLocationId(long locationId, int page, int pageSize) {
        var result = reviewLocationRepository.findAllByLocationIdPage(locationId, PageRequest.of(page, pageSize));
        var reviews = result.stream().map(reviewMapper::entityToDto).toList();
        return new PageableDto<>(result.getTotalElements(), result.getTotalPages(), result.getNumberOfElements(), reviews);
    }

    @Override
    public ReviewDto createLocationReview(ReviewCreationDto reviewCreationDto) {
        var transaction = getTransactionFromReviewCreationDto(reviewCreationDto);
        var renter = transaction.getRenter();
        var callingUser = UserUtil.getActiveUser();

        if (callingUser == null || !callingUser.getEmail().equals(renter.getOwner().getEmail())) {
            throw new AccessForbiddenException("Benutzer ist nicht Mieter in der Transaktion.");
        }

        if (transaction.getReviewLocation() != null) {
            throw new ConflictException(ValidationErrorRestDto.builder()
                .message("Bewertung der Location im Kontext der Transaktion existiert bereits").build()
            );
        }

        var review = reviewMapper.creationDtoToLocationEntity(reviewCreationDto);
        var location = transaction.getTimeslot().getOwningLocation();
        var locationReputation = location.getReputation();

        review.setReviewer(renter);
        review.setReviewerKarmaAtReview(renter.getReputation().getKarma());

        review.setReputation(locationReputation);
        review.setRevieweeKarmaAtReview(locationReputation.getKarma());

        var savedReview = reviewLocationRepository.save(review);

        transaction.setReviewLocation(savedReview);
        transactionRepository.save(transaction);

        reputationService.updateReputationForLocation(location.getId());
        reputationService.updateReputationForLender(location.getOwner().getId());

        return reviewMapper.entityToDto(savedReview);
    }

    @Override
    public ReviewDto createRenterReview(ReviewCreationDto reviewCreationDto) {
        var transaction = getTransactionFromReviewCreationDto(reviewCreationDto);
        var lender = transaction.getTimeslot().getOwningLocation().getOwner();
        var callingUser = UserUtil.getActiveUser();

        if (callingUser == null || !callingUser.getEmail().equals(lender.getOwner().getEmail())) {
            throw new AccessForbiddenException("Benutzer ist nicht Vermieter in der Transaktion.");
        }

        if (transaction.getReviewRenter() != null) {
            throw new ConflictException(ValidationErrorRestDto.builder()
                .message("Bewertung des Mieter im Kontext der Transaktion existiert bereits").build()
            );
        }

        var review = reviewMapper.creationDtoToRenterEntity(reviewCreationDto);
        var renterReputation = transaction.getRenter().getReputation();

        review.setReviewer(lender);
        review.setReviewerKarmaAtReview(lender.getReputation().getKarma());

        review.setReputation(renterReputation);
        review.setRevieweeKarmaAtReview(renterReputation.getKarma());

        var savedReview = reviewRenterRepository.save(review);

        transaction.setReviewRenter(savedReview);
        transactionRepository.save(transaction);

        reputationService.updateReputationForRenter(transaction.getRenter().getId());
        return reviewMapper.entityToDto(savedReview);
    }

    @Override
    public ReviewCountDto getReviewCount() {
        var callingUser = UserUtil.getActiveUser();

        if (callingUser == null) {
            throw new AccessForbiddenException("Benutzer konnte nicht identifiziert werden.");
        }

        var email = callingUser.getEmail();

        var reviewCount = reviewLocationRepository.countAllByReviewerEmail(email);
        reviewCount += reviewRenterRepository.countAllByReviewerEmail(email);
        var completedTransactions = transactionRepository.countAllCompletedByEmail(email);

        return ReviewCountDto.builder()
            .reviews(reviewCount)
            .completedTransactions(completedTransactions)
            .build();
    }

    private Transaction getTransactionFromReviewCreationDto(ReviewCreationDto creationDto) {
        var transactionId = creationDto.getTransactionId();
        var transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new NotFoundException("Transaktion mit Id %s existiert nicht.".formatted(transactionId));
        }

        return transactionOpt.get();
    }
}
