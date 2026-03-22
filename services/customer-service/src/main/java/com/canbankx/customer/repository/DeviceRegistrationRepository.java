package com.canbankx.customer.repository;

import com.canbankx.customer.domain.DeviceRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRegistrationRepository extends JpaRepository<DeviceRegistration, UUID> {
    List<DeviceRegistration> findByCustomerId(UUID customerId);
    Optional<DeviceRegistration> findByCustomerIdAndDeviceId(UUID customerId, String deviceId);
}