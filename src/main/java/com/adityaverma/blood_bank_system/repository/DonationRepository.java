package com.adityaverma.blood_bank_system.repository;

import com.adityaverma.blood_bank_system.model.Donation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DonationRepository extends MongoRepository<Donation, String> {

    List<Donation> findByDonorId(String donorId);
    List<Donation> findByBloodBankId(String bloodBankId);

    @Query("{'donorId': ?0, 'donationDate': {$gte: ?1}}")
    List<Donation> findRecentDonations(String donorId, LocalDateTime date);

    @Query(value = "{'donorId': ?0}", count = true)
    long countDonationsByDonor(String donorId);

    @Query("{'donationDate': {$gte: ?0, $lte: ?1}}")
    List<Donation> findByDateRange(LocalDateTime start, LocalDateTime end);
}