package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationDetailDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.ReputationDetailReviewDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.Renter;
import at.ac.tuwien.sepm.groupphase.backend.entity.Reputation;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationLender;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationLocation;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReputationRenter;
import at.ac.tuwien.sepm.groupphase.backend.entity.Review;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewAverage;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewLocation;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewRenter;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationLenderRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationLocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationRenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReputationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReviewLocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReviewRenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.ReputationService;
import at.ac.tuwien.sepm.groupphase.backend.service.impl.reputationdiscount.AbstractReputationDiscount;
import at.ac.tuwien.sepm.groupphase.backend.service.impl.reputationdiscount.DiscountRevieweeAge;
import at.ac.tuwien.sepm.groupphase.backend.service.impl.reputationdiscount.DiscountReviewerAge;
import at.ac.tuwien.sepm.groupphase.backend.service.impl.reputationdiscount.DiscountReviewerKarma;
import at.ac.tuwien.sepm.groupphase.backend.service.impl.reputationdiscount.DiscountSameReviewer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReputationServiceImpl implements ReputationService {

    private static final BigDecimal DECIMAL_HALF = BigDecimal.ONE.divide(BigDecimal.ONE.add(BigDecimal.ONE));
    private static final BigDecimal DECIMAL_TWO = BigDecimal.ONE.add(BigDecimal.ONE);

    // The maximum possible positive/negative weight associated with a review.
    private static final BigDecimal TOTAL_WEIGHT = DECIMAL_HALF;
    // Number of the latest reviews to consider when calculating a fresh reputation score.
    private static final int REVIEW_LOOKBACK = 100;
    // Base for exponential time decay.
    private static final double LAMBDA = 0.998;
    // Base for exponential discounting based on time since last review from same reviewer
    private static final double DISCOUNT_SAME_BASE = 0.01;
    // Base for exponential discounting based on reviewer account age
    private static final double DISCOUNT_REVIEWER_AGE = 0.795;
    // Base for exponential discounting based on reviewer karma
    private static final double DISCOUNT_REVIEWER_KARMA = 0.02;
    // Base for exponential discounting based on location
    private static final double DISCOUNT_REVIEWEE_AGE = 0.795;
    // Breaks where reputationMapping should plateau
    private static final double[] MAPPING_BREAKS = {0.2, 0.4, 0.6, 0.8};

    private final ReviewLocationRepository reviewLocationRepository;
    private final ReviewRenterRepository reviewRenterRepository;
    private final ReputationLenderRepository reputationLenderRepository;
    private final ReputationLocationRepository reputationLocationRepository;
    private final ReputationRenterRepository reputationRenterRepository;

    @Override
    @Transactional
    public void updateReputationForLender(long lenderId) throws NotFoundException {
        var reputation = getReputationForLender(lenderId);
        var subject = reputation.getLender();
        var reviewStream = reviewLocationRepository.findAllByLenderIdStream(lenderId);
        var average = reviewLocationRepository.calculateAverageForLenderId(lenderId);
        var discounts = newDiscountListForLender();

        applyDiscountsAndUpdate(reputation, subject, lenderId, reputationLenderRepository, reviewStream, ReviewLocation::getReviewer, discounts, average);
    }

    @Override
    @Transactional
    public ReputationDetailDto calculateReputationDetailsForLender(long lenderId) throws NotFoundException {
        var reviewStream = reviewLocationRepository.findAllByLenderIdStream(lenderId);
        var subject = getReputationForLender(lenderId).getLender();
        var discounts = newDiscountListForLender();

        return calculateReputationDetails(discounts, reviewStream, ReviewLocation::getReviewer, (x) -> x.getReviewer().getId(), subject);
    }

    @Override
    @Transactional
    public void updateReputationForLocation(long locationId) throws NotFoundException {
        var reputation = getReputationForLocation(locationId);
        var subject = reputation.getLocation();
        var reviewStream = reviewLocationRepository.findAllByLocationIdStream(locationId);
        var average = reviewLocationRepository.calculateAverageForLocationId(locationId);
        var discounts = newDiscountListForLocation();

        applyDiscountsAndUpdate(reputation, subject, locationId, reputationLocationRepository, reviewStream, ReviewLocation::getReviewer, discounts, average);
    }

    @Override
    @Transactional
    public ReputationDetailDto calculateReputationDetailsForLocation(long locationId) throws NotFoundException {
        var reviewStream = reviewLocationRepository.findAllByLocationIdStream(locationId);
        var subject = getReputationForLocation(locationId).getLocation();
        var discounts = newDiscountListForLocation();

        return calculateReputationDetails(discounts, reviewStream, ReviewLocation::getReviewer, (x) -> x.getReviewer().getId(), subject);
    }

    @Override
    @Transactional
    public void updateReputationForRenter(long renterId) throws NotFoundException {
        var reputation = getReputationForRenter(renterId);
        var subject = reputation.getRenter();
        var reviewStream = reviewRenterRepository.findAllByRenterIdStream(renterId);
        var average = reviewRenterRepository.calculateAverageForRenterId(renterId);
        var discounts = newDiscountListForRenter();

        applyDiscountsAndUpdate(reputation, subject, renterId, reputationRenterRepository, reviewStream, ReviewRenter::getReviewer, discounts, average);
    }

    @Override
    @Transactional
    public ReputationDetailDto calculateReputationDetailsForRenter(long renterId) throws NotFoundException {
        var reviewStream = reviewRenterRepository.findAllByRenterIdStream(renterId);
        var subject = getReputationForRenter(renterId).getRenter();
        var discounts = newDiscountListForRenter();

        return calculateReputationDetails(discounts, reviewStream, ReviewRenter::getReviewer, (x) -> x.getReviewer().getId(), subject);
    }

    private <T, U extends Review, V, W extends Reputation> void applyDiscountsAndUpdate(W reputation, T subject, Long subjectId,
                                                                                        ReputationRepository<W> reputationRepository, Stream<U> reviews,
                                                                                        Function<U, V> reviewerGetter,
                                                                                        List<AbstractReputationDiscount<T, U, V>> discounts, ReviewAverage average) {
        var positive = BigDecimal.ZERO;
        var negative = BigDecimal.ZERO;
        var totalReviews = 0;
        var iterator = reviews.limit(REVIEW_LOOKBACK).iterator();
        var now = LocalDateTime.now();
        while (iterator.hasNext()) {
            var review = iterator.next();
            var reviewer = reviewerGetter.apply(review);
            var discount = BigDecimal.ONE;

            for (var factor : discounts) {
                discount = discount.multiply(factor.calculateDiscount(subject, review, reviewer));
            }
            if (discount.compareTo(BigDecimal.ZERO) == -1 || discount.compareTo(BigDecimal.ONE) == 1) {
                log.warn("Review discount out of bounds for review {} on {} {}. Skipping", review.getId(), subject.getClass().getSimpleName(), subjectId);
                continue;
            }

            var timeDecay = timeDecayFactor(review.getCreatedAt(), now);
            if (timeDecay.compareTo(BigDecimal.ZERO) == -1 || timeDecay.compareTo(BigDecimal.ONE) == 1) {
                log.warn("Time decay out of bounds for review {} on {} {}. Skipping", review.getId(), subject.getClass().getSimpleName(), subjectId);
                continue;
            }

            var weights = ratingToWeights(review.getRating());
            positive = positive.add(weights.getFirst().multiply(timeDecay).multiply(discount));
            negative = negative.add(weights.getSecond().multiply(timeDecay).multiply(discount));

            totalReviews++;
        }

        updateReputation(subject.getClass().getSimpleName(), subjectId, totalReviews, reputation, reputationRepository, positive, negative, average);
    }

    private <T, U extends Review, V> ReputationDetailReviewDto calculateReviewDetails(List<AbstractReputationDiscount<T, U, V>> discounts,
                                                                                      T subject, U review, V reviewer, LocalDateTime now,
                                                                                      Long reviewerId) {
        var weights = ratingToWeights(review.getRating());
        var discountDetails = discounts.stream()
            .map((x) -> x.documentDiscount(subject, review, reviewer))
            .filter(Objects::nonNull)
            .toList();
        return ReputationDetailReviewDto.builder()
            .reviewerId(reviewerId)
            .reviewDate(review.getCreatedAt())
            .positiveBaseWeight(weights.getFirst())
            .negativeBaseWeight(weights.getSecond())
            .discounts(discountDetails)
            .timeDecay(timeDecayFactor(review.getCreatedAt(), now))
            .build();
    }

    private <T, U extends Review, V> ReputationDetailDto calculateReputationDetails(List<AbstractReputationDiscount<T, U, V>> discounts,
                                                                                    Stream<U> reviewStream, Function<U, V> reviewerGetter,
                                                                                    Function<U, Long> reviewerIdGetter, T subject) {
        var now = LocalDateTime.now();
        var reviewDetails = reviewStream
            .limit(REVIEW_LOOKBACK)
            .map((x) -> calculateReviewDetails(discounts, subject, x, reviewerGetter.apply(x), now, reviewerIdGetter.apply(x)))
            .toList();
        return ReputationDetailDto.builder()
            .calculationTime(now)
            .reviews(reviewDetails)
            .build();
    }

    private List<AbstractReputationDiscount<Lender, ReviewLocation, Renter>> newDiscountListForLender() {
        return List.of(
            new DiscountSameReviewer<>(DISCOUNT_SAME_BASE, Renter::getId),
            new DiscountReviewerAge<>(DISCOUNT_REVIEWER_AGE, Renter::getCreatedAt),
            new DiscountReviewerKarma<>(DISCOUNT_REVIEWER_KARMA),
            new DiscountRevieweeAge<>(DISCOUNT_REVIEWEE_AGE, Lender::getCreatedAt)
        );
    }

    private List<AbstractReputationDiscount<Location, ReviewLocation, Renter>> newDiscountListForLocation() {
        return List.of(
            new DiscountSameReviewer<>(DISCOUNT_SAME_BASE, Renter::getId),
            new DiscountReviewerAge<>(DISCOUNT_REVIEWER_AGE, Renter::getCreatedAt),
            new DiscountReviewerKarma<>(DISCOUNT_REVIEWER_KARMA),
            new DiscountRevieweeAge<>(DISCOUNT_REVIEWEE_AGE, Location::getCreatedAt)
        );
    }

    private List<AbstractReputationDiscount<Renter, ReviewRenter, Lender>> newDiscountListForRenter() {
        return List.of(
            new DiscountSameReviewer<>(DISCOUNT_SAME_BASE, Lender::getId),
            new DiscountReviewerAge<>(DISCOUNT_REVIEWER_AGE, Lender::getCreatedAt),
            new DiscountReviewerKarma<>(DISCOUNT_REVIEWER_KARMA),
            new DiscountRevieweeAge<>(DISCOUNT_REVIEWEE_AGE, Renter::getCreatedAt)
        );
    }

    /**
     * Attempts to update a given reputation with the given weights.
     *
     * @param subject              "lender", "location" or "renter". For logging purposes
     * @param id                   ID of the subject. For logging purposes
     * @param totalReviews         number of reviews used to calculate weights. For logging purposes
     * @param reputation           reputation entity to update
     * @param reputationRepository reputation repository for the given reputation
     * @param positive             positive weight solely from reviews
     * @param negative             negative weight solely from reviews
     * @param average              simple review average for public display
     * @param <T>                  type of the reputation entity, subtype of Reputation
     */
    private <T extends Reputation> void updateReputation(String subject, long id, int totalReviews,
                                                         T reputation, ReputationRepository<T> reputationRepository, BigDecimal positive, BigDecimal negative,
                                                         ReviewAverage average) {
        reputation.setAverageRating(average.getAverage());
        reputation.setRatings(average.getCount());
        reputation.setWeightPositive(positive);
        reputation.setWeightNegative(negative);
        BigDecimal karma;
        try {
            karma = positive.add(BigDecimal.ONE).divide(positive.add(negative.add(DECIMAL_TWO)), RoundingMode.HALF_DOWN);
            if (karma.compareTo(BigDecimal.ZERO) == -1 || karma.compareTo(BigDecimal.ONE) == 1) {
                log.warn("Calculated reputation out of bounds for {} {}. Aborting update.", subject, id);
                return;
            } else {
                karma = reputationMapping(karma);
                reputation.setKarma(karma);
            }
        } catch (ArithmeticException e) {
            log.warn("Error during reputation calculation for {} {}. Aborting update.", subject, id);
            return;
        }
        reputationRepository.save(reputation);
        log.info("Updated {} {} reputation with {} reviews (now {})", subject, id, totalReviews, karma);
    }

    /**
     * Maps the raw reputation value to the final reputation value.
     * Returns the original raw reputation when the mapped value is out of bounds.
     *
     * @param karma raw reputation value
     * @return final reputation value
     */
    private BigDecimal reputationMapping(BigDecimal karma) {
        if (karma.compareTo(BigDecimal.ZERO) == 0 || karma.compareTo(BigDecimal.ONE) == 0) {
            return karma;
        }

        double k = 1.0;
        double d = 0.0;
        for (var b : MAPPING_BREAKS) {
            double f = Math.exp(-Math.pow((karma.doubleValue() - b) * 30, 2));
            k *= (1 - f);
            d += f * b;
        }
        var result = karma.multiply(BigDecimal.valueOf(k)).add(BigDecimal.valueOf(d));
        if (result.compareTo(BigDecimal.ZERO) == -1 || result.compareTo(BigDecimal.ONE) == 1) {
            log.warn("Reputation mapping out of bounds for {}. Returning identity.", karma);
            return karma;
        }
        return result;
    }

    /**
     * Maps a rating (integer range [0, 4]) to a pair of reputation weights (decimal range [0, 1]).
     *
     * @param rating the given rating
     * @return a pair of positive (first element) and negative (second element) weights
     */
    private Pair<BigDecimal, BigDecimal> ratingToWeights(int rating) {
        BigDecimal stepFactor = new BigDecimal("0.25");
        BigDecimal positive = stepFactor.multiply(new BigDecimal(rating));
        BigDecimal negative = BigDecimal.ONE.subtract(positive);
        return new Pair<>(positive.multiply(TOTAL_WEIGHT), negative.multiply(TOTAL_WEIGHT));
    }

    /**
     * Computes the factor resulting from exponential time decay.
     *
     * @param then original date
     * @param now  current date
     * @return time decay factor
     */
    private BigDecimal timeDecayFactor(LocalDateTime then, LocalDateTime now) {
        var hoursDiff = Math.abs(((double) then.until(now, ChronoUnit.HOURS)) / 24.0);
        return BigDecimal.valueOf(Math.pow(LAMBDA, hoursDiff));
    }

    /**
     * Attempts to apply time decay to a given reputation.
     *
     * @param reputation reputation to decay
     * @param now        current date
     * @param <T>        type of the reputation entity, subtype of Reputation
     * @return the reputation entity with time decay applied if successful
     */
    private <T extends Reputation> T reputationUpdateTimeDecay(T reputation, LocalDateTime now) {
        var timeDecay = timeDecayFactor(reputation.getUpdatedAt(), now);
        var positive = reputation.getWeightPositive().multiply(timeDecay);
        var negative = reputation.getWeightNegative().multiply(timeDecay);
        try {
            BigDecimal karma = positive.add(BigDecimal.ONE).divide(positive.add(negative.add(DECIMAL_TWO)), RoundingMode.HALF_DOWN);
            if (karma.compareTo(BigDecimal.ZERO) == -1 || karma.compareTo(BigDecimal.ONE) == 1) {
                log.warn("Time decayed reputation out of bounds for reputation {}. Aborting update.", reputation.getId());
                return reputation;
            } else {
                karma = reputationMapping(karma);
                reputation.setKarma(karma);
            }
        } catch (ArithmeticException e) {
            log.warn("Error during time decay for reputation {}. Aborting update.", reputation.getId());
            return reputation;
        }
        return reputation;
    }

    @Override
    @Transactional
    public void updateTimeDecayBefore(LocalDateTime cutoff) {
        var now = LocalDateTime.now();

        var lenderIterator = reputationLenderRepository.findLastUpdatedBeforeDateStream(cutoff).iterator();
        while (lenderIterator.hasNext()) {
            var reputation = lenderIterator.next();
            reputationLenderRepository.save(reputationUpdateTimeDecay(reputation, now));
        }

        var locationIterator = reputationLocationRepository.findLastUpdatedBeforeDateStream(cutoff).iterator();
        while (locationIterator.hasNext()) {
            var reputation = locationIterator.next();
            reputationLocationRepository.save(reputationUpdateTimeDecay(reputation, now));
        }

        var renterIterator = reputationRenterRepository.findLastUpdatedBeforeDateStream(cutoff).iterator();
        while (renterIterator.hasNext()) {
            var reputation = renterIterator.next();
            reputationRenterRepository.save(reputationUpdateTimeDecay(reputation, now));
        }
    }

    @Override
    @Transactional
    public void updateLenderTimeDecay(long lenderId) throws NotFoundException {
        reputationLenderRepository.save(reputationUpdateTimeDecay(getReputationForLender(lenderId), LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void updateLocationTimeDecay(long locationId) throws NotFoundException {
        reputationLocationRepository.save(reputationUpdateTimeDecay(getReputationForLocation(locationId), LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void updateRenterTimeDecay(long renterId) throws NotFoundException {
        reputationRenterRepository.save(reputationUpdateTimeDecay(getReputationForRenter(renterId), LocalDateTime.now()));
    }

    @Override
    public ReputationLender getReputationForLender(long lenderId) throws NotFoundException {
        var existing = reputationLenderRepository.findByLenderId(lenderId);
        if (existing.isPresent()) {
            return existing.get();
        } else {
            throw new NotFoundException("Reputation for lender %d not found".formatted(lenderId));
        }
    }

    @Override
    public ReputationLocation getReputationForLocation(long locationId) throws NotFoundException {
        var existing = reputationLocationRepository.findByLocationId(locationId);
        if (existing.isPresent()) {
            return existing.get();
        } else {
            throw new NotFoundException("Reputation for location %d not found".formatted(locationId));
        }
    }

    @Override
    public ReputationRenter getReputationForRenter(long renterId) throws NotFoundException {
        var existing = reputationRenterRepository.findByRenterId(renterId);
        if (existing.isPresent()) {
            return existing.get();
        } else {
            throw new NotFoundException("Reputation for renter %d not found".formatted(renterId));
        }
    }

    private <C extends Reputation, B extends Reputation.ReputationBuilder<C, ?>> C newGenericReputationEntity(B builder) {
        return builder
            .ratings(0)
            .karma(DECIMAL_HALF)
            .weightPositive(BigDecimal.ZERO)
            .weightNegative(BigDecimal.ZERO)
            .build();
    }

    @Override
    public ReputationLender newLenderReputationEntity() {
        return newGenericReputationEntity(ReputationLender.builder());
    }

    @Override
    public ReputationLocation newLocationReputationEntity() {
        return newGenericReputationEntity(ReputationLocation.builder());
    }

    @Override
    public ReputationRenter newRenterReputationEntity() {
        return newGenericReputationEntity(ReputationRenter.builder());
    }

}
