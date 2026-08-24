package com.etour.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.entity.Category;
import com.etour.entity.Itinerary;
import com.etour.entity.Role;
import com.etour.entity.Tour;
import com.etour.entity.TourAddon;
import com.etour.entity.TourSchedule;
import com.etour.entity.User;
import com.etour.enums.PriceType;
import com.etour.repository.CategoryRepository;
import com.etour.repository.ItineraryRepository;
import com.etour.repository.RoleRepository;
import com.etour.repository.TourAddonRepository;
import com.etour.repository.TourRepository;
import com.etour.repository.TourScheduleRepository;
import com.etour.repository.UserRepository;

/**
 * Runs the actual seeding inside one transaction/Hibernate session (invoked
 * via an injected bean reference from DataSeeder's CommandLineRunner, not
 * self-invocation, so the @Transactional proxy actually applies) - lazy
 * collections like Tour.categories need an open session to read.
 */
@Service
public class DemoDataSeederService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final TourRepository tourRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final ItineraryRepository itineraryRepository;
    private final TourAddonRepository tourAddonRepository;

    public DemoDataSeederService(RoleRepository roleRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder, CategoryRepository categoryRepository, TourRepository tourRepository,
            TourScheduleRepository tourScheduleRepository, ItineraryRepository itineraryRepository,
            TourAddonRepository tourAddonRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.tourRepository = tourRepository;
        this.tourScheduleRepository = tourScheduleRepository;
        this.itineraryRepository = itineraryRepository;
        this.tourAddonRepository = tourAddonRepository;
    }

    @Transactional
    public void seedAll() {
        seedRolesAndAdmin();
        Tour multiPathTour = seedEuropeCategoryAndTagTour();
        seedSchedulesForBookableTours();
        seedItinerary(multiPathTour);
        seedAddons();
    }

    private void seedRolesAndAdmin() {
        Role adminRole;

        if (roleRepository.count() == 0) {
            adminRole = new Role();
            adminRole.setRoleName("ADMIN");
            adminRole.setDescription("Back-office administrator");
            adminRole = roleRepository.save(adminRole);

            Role customerRole = new Role();
            customerRole.setRoleName("CUSTOMER");
            customerRole.setDescription("Registered traveller");
            roleRepository.save(customerRole);
        } else {
            adminRole = roleRepository.findByRoleName("ADMIN").orElse(null);
        }

        // A known-credential admin login for manual verification - existing
        // admin accounts in this DB have passwords nobody currently has.
        String seedAdminEmail = "admin.seed@etour.com";
        if (adminRole != null && !userRepository.existsByEmail(seedAdminEmail)) {
            User seedAdmin = new User();
            seedAdmin.setFirstName("Demo");
            seedAdmin.setLastName("Admin");
            seedAdmin.setEmail(seedAdminEmail);
            seedAdmin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            seedAdmin.setPreferredLanguage("en");
            seedAdmin.setStatus(true);
            seedAdmin.setRole(adminRole);
            userRepository.save(seedAdmin);
        }
    }

    /**
     * Adds an "Europe" sub-category under the existing "International"
     * top-level category (if not already present) and tags one existing
     * International+Events tour with it too, so that tour becomes reachable
     * via International -> Europe, via the existing Events tag, and via
     * direct search - the same tour, three paths.
     */
    private Tour seedEuropeCategoryAndTagTour() {
        Optional<Category> existingEurope = categoryRepository.findByCategoryName("Europe");
        Category europe;
        if (existingEurope.isPresent()) {
            europe = existingEurope.get();
        } else {
            Category international = categoryRepository.findByCategoryName("International").orElse(null);
            if (international == null) {
                return null;
            }
            europe = new Category();
            europe.setCategoryName("Europe");
            europe.setDescription("European destinations");
            europe.setParentCategory(international);
            europe.setCategoryCode(international.getCategoryCode());
            europe.setIsFeatured("N");
            europe.setStatus(true);
            europe = categoryRepository.save(europe);
        }

        Tour londonTour = tourRepository.findByTitleIgnoreCase("London, the Seasoned Classic").orElse(null);
        Long europeId = europe.getCategoryId();
        if (londonTour != null) {
            boolean alreadyTagged = londonTour.getCategories().stream()
                    .anyMatch(c -> c.getCategoryId().equals(europeId));
            if (!alreadyTagged) {
                Set<Category> categories = new HashSet<>(londonTour.getCategories());
                categories.add(europe);
                londonTour.setCategories(categories);
                tourRepository.save(londonTour);
            }
        }
        return londonTour;
    }

    /** Every tour needs at least one schedule to be bookable at all. */
    private void seedSchedulesForBookableTours() {
        for (Tour tour : tourRepository.findAll()) {
            if (!tourScheduleRepository.findByTourTourId(tour.getTourId()).isEmpty()) {
                continue;
            }
            TourSchedule schedule = new TourSchedule();
            schedule.setTour(tour);
            schedule.setDepartureDate(LocalDate.now().plusMonths(2));
            int duration = tour.getDurationDays() == null ? 7 : tour.getDurationDays();
            schedule.setReturnDate(schedule.getDepartureDate().plusDays(duration));
            schedule.setAvailableSeats(20);
            schedule.setPrice(tour.getBasePrice() == null ? new BigDecimal("25000.00") : tour.getBasePrice());
            tourScheduleRepository.save(schedule);
        }
    }

    private void seedItinerary(Tour multiPathTour) {
        for (Tour tour : tourRepository.findAll()) {
            if (!itineraryRepository.findByTour_TourIdOrderByDayNumberAsc(tour.getTourId()).isEmpty()) {
                continue;
            }

            if (multiPathTour != null && tour.getTourId().equals(multiPathTour.getTourId())) {
                addDay(tour, 1, "Arrival in London", "Airport pickup and check-in, evening Thames river walk.");
                addDay(tour, 2, "City Highlights", "Buckingham Palace, Big Ben, London Eye and Westminster Abbey.");
                addDay(tour, 3, "Museums & Markets", "British Museum in the morning, Camden Market in the afternoon.");
                continue;
            }

            int days = Math.min(tour.getDurationDays() == null ? 3 : tour.getDurationDays(), 5);
            for (int day = 1; day <= days; day++) {
                if (day == 1) {
                    addDay(tour, day, "Arrival",
                            "Arrive and check in; orientation walk around " + tour.getTitle() + ".");
                } else if (day == days) {
                    addDay(tour, day, "Departure",
                            "Free time for last-minute shopping, then transfer for departure.");
                } else {
                    addDay(tour, day, "Sightseeing & Local Experiences",
                            "Guided sightseeing and local experiences, day " + day + " of the tour.");
                }
            }
        }
    }

    private void addDay(Tour tour, int dayNumber, String title, String description) {
        Itinerary day = new Itinerary();
        day.setTour(tour);
        day.setDayNumber(dayNumber);
        day.setTitle(title);
        day.setDescription(description);
        itineraryRepository.save(day);
    }

    private void seedAddons() {
        for (Tour tour : tourRepository.findAll()) {
            if (!tourAddonRepository.findByTour_TourIdAndStatusTrue(tour.getTourId()).isEmpty()) {
                continue;
            }
            TourAddon addon = new TourAddon();
            addon.setTour(tour);
            addon.setAddonName("Travel Insurance");
            addon.setDescription("Comprehensive travel insurance for the duration of the trip.");
            addon.setPrice(new BigDecimal("1500.00"));
            addon.setPriceType(PriceType.PER_PERSON);
            addon.setIsOptional(true);
            addon.setDisplayOrder(1);
            addon.setStatus(true);
            tourAddonRepository.save(addon);
        }
    }
}
