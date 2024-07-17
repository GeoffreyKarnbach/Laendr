package at.ac.tuwien.sepm.groupphase.backend.repository;

import at.ac.tuwien.sepm.groupphase.backend.dto.LocationFilterDistanceDto;
import at.ac.tuwien.sepm.groupphase.backend.dto.LocationFilterDto;
import at.ac.tuwien.sepm.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepm.groupphase.backend.entity.Lender;
import at.ac.tuwien.sepm.groupphase.backend.entity.Location;
import at.ac.tuwien.sepm.groupphase.backend.entity.LocationTag;
import at.ac.tuwien.sepm.groupphase.backend.entity.Timeslot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LocationRepositoryCustomImpl implements LocationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Location> findAllByFilterParams(LocationFilterDto filterDto, Pageable pageable) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        // Build the base query
        CriteriaQuery<Location> query = criteriaBuilder.createQuery(Location.class);
        Root<Location> root = query.from(Location.class);
        query.select(root).distinct(true);

        // Apply the filter conditions based on the provided parameters in the filterDto
        query.where(applyPredicate(criteriaBuilder, root, filterDto));

        // Append the sorting criterion from the pageable
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order order : sort) {
                String property = order.getProperty();

                String[] parts = property.split("\\.");
                Expression orderPath;
                if (parts.length == 1) {
                    orderPath = root.get(property);
                } else {
                    // Handling for nested properties
                    var join = (Join) root.fetch(parts[0]);
                    int i = 1;
                    for (; i < parts.length - 1; i++) {
                        join = (Join) join.fetch(parts[i]);
                    }
                    orderPath = join.get(parts[i]);
                }
                orders.add(order.isAscending() ? criteriaBuilder.asc(orderPath) : criteriaBuilder.desc(orderPath));
            }
            query.orderBy(orders);
        }

        // Apply pagination
        TypedQuery<Location> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Execute the query and return the paginated result
        List<Location> locations = typedQuery.getResultList();
        // Execute the count query
        int totalCount = executeCountQuery(filterDto, pageable, locations.size());

        return new PageImpl<>(locations, pageable, totalCount);
    }

    @Override
    public Page<Location> findAllByFilterParamsSortByPrice(LocationFilterDto filterDto, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        // Build the base query
        var query = criteriaBuilder.createTupleQuery();
        Root<Location> root = query.from(Location.class);

        var timeslotsJoin = ((Join) root.join("timeslots", JoinType.LEFT));
        timeslotsJoin = timeslotsJoin.on(criteriaBuilder.greaterThanOrEqualTo(timeslotsJoin.get("start"), LocalDate.now()));
        var price = timeslotsJoin.get("price");
        // Append the sorting criterion from the pageable
        Sort sort = pageable.getSort();
        List<Order> orders = new ArrayList<>();
        Expression orderCriteria = null;
        for (Sort.Order order : sort) {
            String property = order.getProperty();

            String[] parts = property.split("\\.");
            Expression orderPath;
            if (parts.length == 1) {
                orderPath = root.get(property);
            } else {
                // Handling for special case "price"
                orderCriteria = orderPath = switch (parts[1]) {
                    case "min" -> criteriaBuilder.min(price);
                    case "max" -> criteriaBuilder.max(price);
                    default -> criteriaBuilder.avg(price);
                };
            }
            orders.add(order.isAscending() ? criteriaBuilder.asc(orderPath) : criteriaBuilder.desc(orderPath));
        }

        query.select(criteriaBuilder.tuple(root, orderCriteria));

        // Apply the filter conditions based on the provided parameters in the filterDto
        query.where(applyPredicate(criteriaBuilder, root, filterDto));

        query.groupBy(root.get("id"));

        query.orderBy(orders);

        // Apply pagination
        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Execute the query and return the paginated result
        List<Location> locations = typedQuery.getResultList().stream().map(tuple -> (Location) tuple.get(0)).toList();
        // Execute the count query
        int totalCount = executeCountQuery(filterDto, pageable, locations.size());

        return new PageImpl<>(locations, pageable, totalCount);
    }

    @Override
    public Page<Location> findAllByOwnerId(Long ownerId, boolean includeRemoved, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        // Build the base query
        CriteriaQuery<Location> query = criteriaBuilder.createQuery(Location.class);
        Root<Location> root = query.from(Location.class);
        query.select(root).distinct(true);

        // Perform filtering
        Join<Location, Lender> ownerIdJoin = root.join("owner");
        var ownerPred = criteriaBuilder.equal(ownerIdJoin.get("id"), ownerId);
        if (includeRemoved) {
            query.where(ownerPred);
        } else {
            query.where(ownerPred, criteriaBuilder.equal(root.get("removed"), false));
        }

        // Append the sorting criterion from the pageable
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order order : sort) {
                String property = order.getProperty();

                String[] parts = property.split("\\.");
                Expression orderPath;
                if (parts.length == 1) {
                    orderPath = root.get(property);
                } else {
                    // Handling for nested properties
                    var join = (Join) root.fetch(parts[0]);
                    int i = 1;
                    for (; i < parts.length - 1; i++) {
                        join = (Join) join.fetch(parts[i]);
                    }
                    orderPath = join.get(parts[i]);
                }
                orders.add(order.isAscending() ? criteriaBuilder.asc(orderPath) : criteriaBuilder.desc(orderPath));
            }
            query.orderBy(orders);
        }

        // Apply pagination
        TypedQuery<Location> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Execute the query and return the paginated result
        List<Location> locations = typedQuery.getResultList();
        // Execute the count query
        int totalCount = executeCountQueryOwner(ownerId, includeRemoved, pageable, locations.size());

        return new PageImpl<>(locations, pageable, totalCount);
    }

    @Override
    public Page<Location> findAllByOwnerIdSortByPrice(Long ownerId, boolean includeRemoved, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        // Build the base query
        var query = criteriaBuilder.createTupleQuery();
        Root<Location> root = query.from(Location.class);

        var price = ((Join) root.join("timeslots", JoinType.LEFT)).get("price");
        // Append the sorting criterion from the pageable
        Sort sort = pageable.getSort();
        List<Order> orders = new ArrayList<>();
        Expression orderCriteria = null;
        for (Sort.Order order : sort) {
            String property = order.getProperty();

            String[] parts = property.split("\\.");
            Expression orderPath;
            if (parts.length == 1) {
                orderPath = root.get(property);
            } else {
                // Handling for special case "price"
                orderCriteria = orderPath = switch (parts[1]) {
                    case "min" -> criteriaBuilder.min(price);
                    case "max" -> criteriaBuilder.max(price);
                    default -> criteriaBuilder.avg(price);
                };
            }
            orders.add(order.isAscending() ? criteriaBuilder.asc(orderPath) : criteriaBuilder.desc(orderPath));
        }

        query.select(criteriaBuilder.tuple(root, orderCriteria));

        // Perform filtering
        Join<Location, Lender> ownerIdJoin = root.join("owner");
        var ownerPred = criteriaBuilder.equal(ownerIdJoin.get("id"), ownerId);
        if (includeRemoved) {
            query.where(ownerPred);
        } else {
            query.where(ownerPred, criteriaBuilder.equal(root.get("removed"), false));
        }

        query.groupBy(root.get("id"));

        query.orderBy(orders);

        // Apply pagination
        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        // Execute the query and return the paginated result
        List<Location> locations = typedQuery.getResultList().stream().map(tuple -> (Location) tuple.get(0)).toList();
        // Execute the count query
        int totalCount = executeCountQueryOwner(ownerId, includeRemoved, pageable, locations.size());

        return new PageImpl<>(locations, pageable, totalCount);
    }

    private int executeCountQuery(LocationFilterDto filterDto, Pageable pageable, int resultSize) {
        int totalCount = resultSize;
        if (totalCount >= pageable.getPageSize()) {
            CriteriaBuilder criteriaBuilderCount = entityManager.getCriteriaBuilder();
            // Create a count query to retrieve the total count of locations with the same criteria
            CriteriaQuery<Long> countQuery = criteriaBuilderCount.createQuery(Long.class);
            Root<Location> countRoot = countQuery.from(Location.class);
            countQuery.select(criteriaBuilderCount.countDistinct(countRoot));

            countQuery.where(applyPredicate(criteriaBuilderCount, countRoot, filterDto));
            totalCount = entityManager.createQuery(countQuery).getSingleResult().intValue();
        }

        return totalCount;
    }

    private int executeCountQueryOwner(Long ownerId, boolean includeRemoved, Pageable pageable, int resultSize) {
        int totalCount = resultSize;

        if (totalCount >= pageable.getPageSize()) {
            CriteriaBuilder criteriaBuilderCount = entityManager.getCriteriaBuilder();
            // Create a count query to retrieve the total count of locations from the given owner
            CriteriaQuery<Long> countQuery = criteriaBuilderCount.createQuery(Long.class);
            Root<Location> countRoot = countQuery.from(Location.class);
            countQuery.select(criteriaBuilderCount.countDistinct(countRoot));

            var ownerJoin = countRoot.join("owner", JoinType.LEFT);
            var ownerPred = criteriaBuilderCount.equal(ownerJoin.get("id"), ownerId);
            if (includeRemoved) {
                countQuery.where(ownerPred);
            } else {
                countQuery.where(ownerPred, criteriaBuilderCount.equal(countRoot.get("removed"), false));
            }
            totalCount = entityManager.createQuery(countQuery).getSingleResult().intValue();
        }

        return totalCount;
    }

    private Predicate applyPredicate(CriteriaBuilder criteriaBuilder, Root<Location> root, LocationFilterDto filterDto) {
        // Create joins with timeslots and reputation entities
        Join<Location, Lender> ownerJoin = root.join("owner");
        Join<Lender, ApplicationUser> userJoin = ownerJoin.join("owner");
        Join<Location, Timeslot> timeslotJoin = root.join("timeslots", JoinType.LEFT);

        Predicate predicate = criteriaBuilder.conjunction();
        if (filterDto.getSearchString() != null) {
            predicate = criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + filterDto.getSearchString().toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("name")), "%" + filterDto.getSearchString().toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + filterDto.getSearchString().toLowerCase() + "%")
            );
        }
        if (filterDto.getPlz() != null && !filterDto.getPlz().getPlz().isEmpty()) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("plz"), filterDto.getPlz().getPlz()));
        }
        if (filterDto.getState() != null && !filterDto.getState().getName().isEmpty()) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("state"), filterDto.getState()));
        }
        if (filterDto.getAddress() != null && !filterDto.getAddress().isEmpty()) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), "%" + filterDto.getAddress().toLowerCase() + "%"));
        }
        if (filterDto.getPriceFrom() != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(timeslotJoin.get("price"), filterDto.getPriceFrom()));
        }
        if (filterDto.getPriceTo() != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(timeslotJoin.get("price"), filterDto.getPriceTo()));
        }
        if (filterDto.getTimeFrom() != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(timeslotJoin.get("start"), filterDto.getTimeFrom()));
        }
        if (filterDto.getTimeTo() != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(timeslotJoin.get("end"), filterDto.getTimeTo()));
        }
        if (filterDto.getPosition() != null && filterDto.getPosition().getDistance() != null && filterDto.getPosition().getCoord() != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.isNotNull(root.get("coordLat")));
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.isNotNull(root.get("coordLng")));
            predicate = criteriaBuilder.and(predicate, getPositionPredicateExpression(criteriaBuilder, root, filterDto.getPosition()));
        }
        predicate = criteriaBuilder.and(predicate, criteriaBuilder.not(root.get("removed")));

        if (filterDto.getTags() != null && filterDto.getTags().length != 0) {
            log.info("Tags: {}", filterDto.getTags());
            // Join table "location_to_location_tag" to get the tags

            List<Predicate> tagPredicates = new ArrayList<>();

            for (String tag : filterDto.getTags()) {
                Join<Location, LocationTag> joinTags = root.joinSet("tags");
                Predicate tagPredicate = criteriaBuilder.equal(joinTags.get("name"), tag);
                tagPredicates.add(tagPredicate);
                log.info("Tag: {}", tag);
            }

            Predicate finalPredicate = criteriaBuilder.and(tagPredicates.toArray(new Predicate[0]));

            predicate = criteriaBuilder.and(predicate, finalPredicate);
        }

        return predicate;
    }

    private Expression<Boolean> getPositionPredicateExpression(CriteriaBuilder cb, Root<Location> root, LocationFilterDistanceDto distanceDto) {
        var lat = distanceDto.getCoord().getLat();
        var lng = distanceDto.getCoord().getLng();

        var r = 6371;
        var phiEntity = cb.prod(root.get("coordLat"), Math.PI / 180);
        var phiDto = lat.doubleValue() * Math.PI / 180;
        var deltaPhi = cb.prod(cb.diff(lat, root.get("coordLat")), Math.PI / 180);
        var deltaLambda = cb.prod(cb.diff(lng, root.get("coordLng")), Math.PI / 180);

        var a1 = cb.prod(cb.function("SIN", Double.class, cb.quot(deltaPhi, 2)), cb.function("SIN", Double.class, cb.quot(deltaPhi, 2)));
        var a2 = cb.prod(cb.function("COS", Double.class, phiEntity), Math.cos(phiDto));
        var a3 = cb.prod(cb.function("SIN", Double.class, cb.quot(deltaLambda, 2)), cb.function("SIN", Double.class, cb.quot(deltaLambda, 2)));
        var a = cb.sum(a1, cb.prod(a2, a3));

        var c = cb.prod(2, cb.function("ATAN2", Double.class, cb.sqrt(a), cb.sqrt(cb.diff(1, a))));

        return cb.lessThanOrEqualTo(((Expression) cb.prod(r, c)), distanceDto.getDistance());
    }
}
