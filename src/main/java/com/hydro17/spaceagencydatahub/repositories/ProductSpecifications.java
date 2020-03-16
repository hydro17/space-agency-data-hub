package com.hydro17.spaceagencydatahub.repositories;

import com.hydro17.spaceagencydatahub.models.Product;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class ProductSpecifications {

    public static Specification<Product> missionNameEquals(String missionName) {
        return (root, query, builder) -> builder.equal(root.get("missionName"), missionName);
    }

    public static  Specification<Product> beforeDate(LocalDateTime beforeDate) {
        return (root, query, builder) -> builder.lessThan(root.get("acquisitionDate"), beforeDate);
    }

    public static  Specification<Product> afterDate(LocalDateTime afterDate) {
        return (root, query, builder) -> builder.greaterThan(root.get("acquisitionDate"), afterDate);
    }

    public static  Specification<Product> footprintEndCoordinateLatitudeGreaterThanOrEqualTo(Double latitude) {
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.join("footprint").get("endCoordinateLatitude"), latitude);
    }

    public static  Specification<Product> footprintStartCoordinateLatitudeLessThanOrEqualTo(Double latitude) {
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.join("footprint").get("startCoordinateLatitude"), latitude);
    }

    public static  Specification<Product> footprintEndCoordinateLongitudeGreaterThanOrEqualTo(Double longitude) {
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.join("footprint").get("endCoordinateLongitude"), longitude);
    }

    public static  Specification<Product> footprintStartCoordinateLongitudeLessThanOrEqualTo(Double longitude) {
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.join("footprint").get("startCoordinateLongitude"), longitude);
    }

    public static Specification<Product> getSpecifications(String missionName, LocalDateTime beforeDate, LocalDateTime afterDate, Double latitude, Double longitude) {
        return Specification
                .where(missionName == null ? null : missionNameEquals(missionName))
                .and(
//                      if both dates are given then:
//                          - if beforeDate is less than or equal to afterDate then
//                            we get products before beforeDate and after afterDate
//                          - if beforeDate is greater than afterDate then
//                            we get products between these dates
                        beforeDate != null && afterDate != null ?
                                beforeDate.compareTo(afterDate) <= 0 ?
                                Specification
                                        .where(beforeDate == null ? null : beforeDate(beforeDate))
                                        .or(afterDate == null ? null : afterDate(afterDate))
                                :
                                Specification
                                        .where(beforeDate == null ? null : beforeDate(beforeDate))
                                        .and(afterDate == null ? null : afterDate(afterDate))
                        :
//                      if only one date is given then:
//                          - if given date is beforeDate we get products before this date
//                          - if given date is afterDate we get products after this date
                        Specification
                                .where(beforeDate == null ? null : beforeDate(beforeDate))
                                .or(afterDate == null ? null : afterDate(afterDate))
                )
                .and(
//                      only if both longitude and altitude are given use them in filtering
                        latitude == null || longitude == null ? null
                                :
                                Specification
                                    .where(footprintStartCoordinateLatitudeLessThanOrEqualTo(latitude))
                                    .and(footprintEndCoordinateLatitudeGreaterThanOrEqualTo(latitude))
                                    .and(footprintStartCoordinateLongitudeLessThanOrEqualTo(longitude))
                                    .and(footprintEndCoordinateLongitudeGreaterThanOrEqualTo(longitude))
                );
    }
}
