package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext.Node;
import at.ac.tuwien.sepm.groupphase.backend.entity.Renter;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewLocation;
import at.ac.tuwien.sepm.groupphase.backend.entity.ReviewRenter;
import at.ac.tuwien.sepm.groupphase.backend.entity.Timeslot;
import at.ac.tuwien.sepm.groupphase.backend.entity.Transaction;
import at.ac.tuwien.sepm.groupphase.backend.enums.AppRole;
import at.ac.tuwien.sepm.groupphase.backend.enums.CancelReason;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReviewLocationRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ReviewRenterRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TimeslotRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TransactionRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.ReputationService;

import java.util.List;
import java.util.Random;

public class TransactionDataGenerator {

    public static void generateTransactions(
        Random random,
        List<UserDataGenerator.TestRenter> renters,
        List<LocationDataGenerator.TestLocation> locations,
        TransactionRepository transactionRepository,
        ReviewLocationRepository reviewLocationRepository,
        ReviewRenterRepository reviewRenterRepository,
        TimeslotRepository timeslotRepository,
        ReputationService reputationService
    ) {
        for (var location : locations) {
            var pastTimeslots = location.pastTimeslots();
            var maxPast = Math.min(pastTimeslots.size() / 2, random.nextInt(pastTimeslots.size() + 1));
            if (random.nextInt(4) != 0) {
                for (var slot : pastTimeslots) {
                    if (--maxPast < 0) {
                        break;
                    }
                    var completionDate = slot.getEnd().plusDays(1);
                    var renter = DatagenUtil.randomElement(random, renters).renterEntity();
                    var rating = random.nextInt(5);
                    var commentTemplate = getReviewTemplate(rating, random);

                    ReviewLocation reviewLocation;
                    if (random.nextBoolean()) {
                        reviewLocation = reviewLocationRepository.save(ReviewLocation.builder()
                            .rating(rating)
                            .reviewerKarmaAtReview(reputationService.getReputationForRenter(renter.getId()).getKarma())
                            .revieweeKarmaAtReview(reputationService.getReputationForLocation(location.locationEntity().getId()).getKarma())
                            .comment(commentTemplate.generateString(random))
                            .reputation(location.locationEntity().getReputation())
                            .reviewer(renter)
                            .build());
                        reviewLocation.setCreatedAt(completionDate);
                        reviewLocationRepository.save(reviewLocation);
                    } else {
                        reviewLocation = null;
                    }

                    ReviewRenter reviewRenter = null;
                    if (random.nextBoolean()) {
                        rating = random.nextInt(5);
                        commentTemplate = getReviewTemplate(rating, random);
                        var lender = slot.getOwningLocation().getOwner();

                        reviewRenter = reviewRenterRepository.save(
                            ReviewRenter.builder()
                                .rating(rating)
                                .reviewerKarmaAtReview(reputationService.getReputationForLender(lender.getId()).getKarma())
                                .revieweeKarmaAtReview(reputationService.getReputationForRenter(renter.getId()).getKarma())
                                .comment(commentTemplate.generateString(random))
                                .reputation(renter.getReputation())
                                .reviewer(lender)
                                .build()
                        );
                        reviewRenter.setCreatedAt(completionDate);
                        reviewRenterRepository.save(reviewRenter);
                    }

                    var transactionEntity = transactionRepository.save(Transaction.builder()
                        .initialMessage(TransactionTemplate.MESSAGE.generateString(random))
                        .timeslot(slot)
                        .renter(renter)
                        .completedAt(completionDate)
                        .amountPaid(slot.getPrice())
                        .reviewLocation(reviewLocation)
                        .reviewRenter(reviewRenter)
                        .build());
                    transactionEntity.setCreatedAt(completionDate);
                    transactionRepository.save(transactionEntity);

                    reputationService.updateReputationForLocation(location.locationEntity().getId());
                    reputationService.updateReputationForRenter(renter.getId());
                    reputationService.updateReputationForLender(location.locationEntity().getOwner().getId());
                }
            }

            var currentTimeslots = location.currentTimeslots();
            var maxCurrent = Math.min(currentTimeslots.size() / 2, random.nextInt(currentTimeslots.size() + 1));
            for (var slot : currentTimeslots) {
                if (--maxCurrent < 0) {
                    break;
                }

                if (random.nextInt(4) < 1) {
                    var renter = DatagenUtil.randomElement(random, renters).renterEntity();
                    Transaction transactionEntity = getTransaction(random, renter, transactionRepository, slot);
                    slot.setUsed(true);
                    timeslotRepository.save(slot);
                    transactionRepository.save(transactionEntity);
                } else {

                    var amountOfTransactions = random.nextInt(4) + 1;
                    for (int i = 0; i < amountOfTransactions; i++) {
                        var renterEntites = DatagenUtil.randomSubset(amountOfTransactions, renters).stream().map(r -> r.renterEntity()).toList();

                        Transaction transactionEntity = getTransaction(random, renterEntites.get(i), transactionRepository, slot);
                        if (random.nextBoolean()) {
                            transactionEntity.setCancelled(true);
                            transactionEntity.setCancelByRole(AppRole.ROLE_LENDER);
                            transactionEntity.setCancelNotified(false);
                            transactionEntity.setCancelReason(CancelReason.NO_INTEREST);
                            transactionEntity.setCancelDescription("Leider kein Interesse :(");
                        }

                        transactionRepository.save(transactionEntity);
                    }
                }
            }
        }
    }

    private static Transaction getTransaction(Random random, Renter renter, TransactionRepository transactionRepository, Timeslot slot) {
        var transactionEntity = transactionRepository.save(Transaction.builder()
            .initialMessage(TransactionTemplate.MESSAGE.generateString(random))
            .timeslot(slot)
            .renter(renter)
            .build());
        transactionEntity.setCreatedAt(slot.getStart().minusDays(4));
        return transactionEntity;
    }

    private static Node getReviewTemplate(int rating, Random random) {
        return switch (rating) {
            case 0 -> TransactionTemplate.REVIEW_BAD;
            case 1 -> random.nextBoolean() ? TransactionTemplate.REVIEW_MEDIUM : TransactionTemplate.REVIEW_BAD;
            case 2 -> TransactionTemplate.REVIEW_MEDIUM;
            case 3 -> random.nextBoolean() ? TransactionTemplate.REVIEW_MEDIUM : TransactionTemplate.REVIEW_GOOD;
            case 4 -> TransactionTemplate.REVIEW_GOOD;
            default -> throw new IllegalStateException();
        };
    }

}
