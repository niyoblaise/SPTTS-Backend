package com.blaise.SPTTS.Mapper;

import com.blaise.SPTTS.DTOs.*;
import com.blaise.SPTTS.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransportMapper {

    /* Bus */
    @Mapping(source = "route.routeName", target = "routeName")
    @Mapping(source = "operator.userId", target = "driverId")
    @Mapping(source = "route.routeId", target = "routeId")
    @Mapping(source = "licensePlate", target = "plateNumber")
    BusDto toDto(Bus bus);

    Bus toEntity(BusDto dto);

    /* Route */
    RouteDto toDto(Route route);

    @Mapping(target = "stops", ignore = true)
    Route toEntity(RouteDto dto);

    /* BusStop */
    @Mapping(source = "route.routeId", target = "routeId")
    StopDto toDto(BusStop stop);

    @Mapping(source = "routeId", target = "route.routeId")
    BusStop toEntity(StopDto dto);

    /* BusLocation */
    @Mapping(source = "bus.busId", target = "busId")
    BusLocationDto toDto(BusLocation loc);

    @Mapping(source = "busId", target = "bus.busId")
    BusLocation toEntity(BusLocationDto dto);

    /* Trip */
    @Mapping(source = "passenger.userId", target = "passengerId")
    @Mapping(source = "bus.busId", target = "busId")
    @Mapping(source = "route.routeId", target = "routeId")
    @Mapping(source = "passenger.fullName", target = "passengerName")
    @Mapping(source = "passenger.email", target = "passengerEmail")
    TripDto toDto(Trip trip);

    @Mapping(source = "passengerId", target = "passenger.userId")
    @Mapping(source = "busId", target = "bus.busId")
    @Mapping(source = "routeId", target = "route.routeId")
    Trip toEntity(TripDto dto);

    /* Fare */
    FareDto toDto(Fare fare);

    Fare toEntity(FareDto dto);

    /* Notification */
    NotificationDto toDto(Notification n);

    Notification toEntity(NotificationDto dto);

    /* Payment */
    @Mapping(source = "passenger.userId", target = "passengerId")
    @Mapping(source = "trip.tripId", target = "tripId")
    PaymentDto toDto(Payment p);

    @Mapping(source = "passengerId", target = "passenger.userId")
    @Mapping(source = "tripId", target = "trip.tripId")
    Payment toEntity(PaymentDto dto);

    /* User */
    UserDto toDto(User user);
}