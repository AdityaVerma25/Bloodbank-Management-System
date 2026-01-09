package com.adityaverma.blood_bank_system.repository;

import com.adityaverma.blood_bank_system.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);

    @Query("{'email': ?0, 'isActive': true}")
    Optional<User> findActiveByEmail(String email);

    @Query("{'isDonor': true, 'isActive': true}")
    List<User> findAllActiveDonors();

    @Query("{'bloodGroup': ?0, 'isDonor': true, 'isActive': true, 'city': ?1}")
    List<User> findDonorsByBloodGroupAndCity(String bloodGroup, String city);

    @Query("{'lastDonationDate': {$lt: ?0}, 'isDonor': true, 'isActive': true}")
    List<User> findEligibleDonors(LocalDate eligibleSince);

    @Query("{'emailVerificationToken': ?0, 'emailVerificationExpires': {$gt: ?1}}")
    Optional<User> findByEmailVerificationToken(String token, LocalDate now);

    @Query("{'passwordResetToken': ?0, 'passwordResetExpires': {$gt: ?1}}")
    Optional<User> findByPasswordResetToken(String token, LocalDate now);

    @Query(value = "{'isDonor': true, 'city': ?0}", count = true)
    long countDonorsByCity(String city);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}