package com.adityaverma.blood_bank_system.service;

import com.adityaverma.blood_bank_system.model.Donation;
import com.adityaverma.blood_bank_system.model.User;
import com.adityaverma.blood_bank_system.repository.DonationRepository;
import com.adityaverma.blood_bank_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonorService {
    private final UserRepository userRepository;
    private final DonationRepository donationRepository;
    private final NotificationService notificationService;
    private final IdGenerator idGenerator;

    public User updateDonorProfile(String donorId, User updatedInfo) {
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found: " + donorId));

        if (updatedInfo.getFullName() != null) donor.setFullName(updatedInfo.getFullName());
        if (updatedInfo.getPhone() != null) donor.setPhone(updatedInfo.getPhone());
        if (updatedInfo.getAddress() != null) donor.setAddress(updatedInfo.getAddress());
        if (updatedInfo.getCity() != null) donor.setCity(updatedInfo.getCity());
        if (updatedInfo.getWeightKg() != null) donor.setWeightKg(updatedInfo.getWeightKg());

        return userRepository.save(donor);
    }

    @Cacheable(value = "eligibleDonors", key = "#bloodGroup + '-' + #city")
    public List<User> findEligibleDonors(String bloodGroup, String city) {
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);

        return userRepository.findDonorsByBloodGroupAndCity(bloodGroup, city).stream()
                .filter(User::isEligibleForDonation)
                .toList();
    }

    public Donation recordDonation(String donorId, Donation donation) {
        User donor = userRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found: " + donorId));

        if (!donor.isEligibleForDonation()) {
            throw new RuntimeException("Donor is not eligible for donation");
        }

        donation.setDonationId(idGenerator.generateDonationId());
        donation.setDonorId(donorId);
        donation.setDonationDate(LocalDateTime.now());
        donation.setBloodGroup(donor.getBloodGroup());

        Donation savedDonation = donationRepository.save(donation);

        donor.setLastDonationDate(LocalDate.now());
        userRepository.save(donor);

        notificationService.sendThankYouEmail(donor, savedDonation);

        log.info("Recorded donation {} for donor {}", savedDonation.getDonationId(), donorId);

        return savedDonation;
    }

    @Scheduled(cron = "0 0 10 * * *")
    public void sendDonationReminders() {
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
        List<User> eligibleDonors = userRepository.findEligibleDonors(threeMonthsAgo);

        for (User donor : eligibleDonors) {
            notificationService.sendDonationReminder(donor);
        }

        log.info("Sent donation reminders to {} donors", eligibleDonors.size());
    }

    public Map<String, Object> getDonorStats(String donorId) {
        long totalDonations = donationRepository.countDonationsByDonor(donorId);
        List<Donation> donationHistory = donationRepository.findByDonorId(donorId);

        LocalDateTime lastDonation = donationHistory.stream()
                .map(Donation::getDonationDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return Map.of(
                "totalDonations", totalDonations,
                "lastDonation", lastDonation,
                "donationHistory", donationHistory,
                "lifesaved", totalDonations * 3
        );
    }

    public List<Donation> getDonationHistory(String donorId) {
        return donationRepository.findByDonorId(donorId);
    }
}