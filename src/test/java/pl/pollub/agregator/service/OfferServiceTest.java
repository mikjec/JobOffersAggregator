package pl.pollub.agregator.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import pl.pollub.agregator.dto.OfferDTO;
import pl.pollub.agregator.entity.Company;
import pl.pollub.agregator.entity.Offer;
import pl.pollub.agregator.entity.Role;
import pl.pollub.agregator.entity.User;
import pl.pollub.agregator.repository.CompanyRepository;
import pl.pollub.agregator.repository.OfferRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("OfferService Unit Tests")
class OfferServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private OfferService offerService;

    private User employer;
    private User otherEmployer;
    private User admin;
    private Company company;
    private Offer offer;

    @BeforeEach
    void setUp() {
        employer = User.builder().id(1L).username("employer1").role(Role.EMPLOYER).build();
        otherEmployer = User.builder().id(2L).username("employer2").role(Role.EMPLOYER).build();
        admin = User.builder().id(99L).username("admin").role(Role.ADMIN).build();
        company = Company.builder().id(10L).name("TechCorp").user(employer).isActive(true).build();
        offer = Offer.builder().id(100L).title("Java Developer").location("London").company(company).isActive(true).build();
    }

    @AfterEach
    void tearDown() {
        reset(offerRepository, companyRepository);
    }

    @Test
    @Timeout(1)
    @DisplayName("getAll() and getAll(boolean, Long) should filter by active status or companyId")
    void shouldGetAllOffersAndFilterByCompanyId() {
        when(offerRepository.findAllByIsActiveTrue()).thenReturn(List.of(offer));
        when(offerRepository.findAllByCompanyIdAndIsActiveTrue(10L)).thenReturn(List.of(offer));

        List<OfferDTO> activeOffers = offerService.getAll();
        List<OfferDTO> companyOffers = offerService.getAll(false, 10L);

        assertArrayEquals(new Long[]{100L}, activeOffers.stream().map(OfferDTO::getId).toArray(Long[]::new));
        assertEquals(1, companyOffers.size());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -5L, Long.MIN_VALUE})
    @DisplayName("getById, existsById, getByCompanyId, and countActiveByCompanyId should reject non-positive IDs")
    void shouldRejectNonPositiveIds(long invalidId) {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> offerService.getById(invalidId)),
                () -> assertThrows(IllegalArgumentException.class, () -> offerService.existsById(invalidId)),
                () -> assertThrows(IllegalArgumentException.class, () -> offerService.getByCompanyId(invalidId)),
                () -> assertThrows(IllegalArgumentException.class, () -> offerService.countActiveByCompanyId(invalidId))
        );
    }

    @Test
    @DisplayName("getById, existsById, and countActiveByCompanyId should handle missing records")
    void shouldHandleMissingOfferAndZeroCount() {
        when(offerRepository.findById(500L)).thenReturn(Optional.empty());
        when(offerRepository.existsById(500L)).thenReturn(false);
        when(offerRepository.countByCompanyIdAndIsActiveTrue(10L)).thenReturn(0L);

        assertTrue(offerService.getById(500L).isEmpty());
        assertFalse(offerService.existsById(500L));
        assertEquals(0L, offerService.countActiveByCompanyId(10L));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\n"})
    @DisplayName("searchByLocation should throw IllegalArgumentException for null or blank location")
    void shouldRejectBlankLocationInSearch(String invalidLocation) {
        assertThrows(IllegalArgumentException.class, () -> offerService.searchByLocation(invalidLocation));
    }

    @Test
    @DisplayName("searchByLocation should trim query and return matching offers")
    void shouldTrimLocationAndReturnOffers() {
        when(offerRepository.findAllByLocationContainingIgnoreCaseAndIsActiveTrue("London"))
                .thenReturn(List.of(offer));

        List<OfferDTO> results = offerService.searchByLocation("  London  ");

        assertEquals(1, results.size());
        assertEquals("London", results.get(0).getLocation());
    }

    @Nested
    @DisplayName("Save, Update, and Delete Edge Cases")
    class WriteOperationsTests {

        @Test
        @DisplayName("save should throw 400 BAD_REQUEST when companyId is null and 404 NOT_FOUND when company does not exist")
        void shouldValidateCompanyOnSave() {
            OfferDTO noCompanyDto = OfferDTO.builder().title("Java Dev").location("Remote").companyId(null).build();
            OfferDTO missingCompanyDto = OfferDTO.builder().title("Java Dev").location("Remote").companyId(999L).build();
            when(companyRepository.findById(999L)).thenReturn(Optional.empty());

            ResponseStatusException badReq = assertThrows(ResponseStatusException.class,
                    () -> offerService.save(noCompanyDto, employer));
            ResponseStatusException notFound = assertThrows(ResponseStatusException.class,
                    () -> offerService.save(missingCompanyDto, employer));

            assertEquals(HttpStatus.BAD_REQUEST, badReq.getStatusCode());
            assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());
        }

        @Test
        @DisplayName("save should throw AccessDeniedException when EMPLOYER creates offer for another user's company")
        void shouldDenySaveForForeignCompany() {
            OfferDTO dto = OfferDTO.builder().title("Java Dev").location("Remote").companyId(10L).build();
            when(companyRepository.findById(10L)).thenReturn(Optional.of(company));

            assertThrows(AccessDeniedException.class, () -> offerService.save(dto, otherEmployer));
            assertThrows(AccessDeniedException.class, () -> offerService.save(dto, null));
            verify(offerRepository, never()).save(any());
        }

        @Test
        @DisplayName("save and update should succeed for company owner and ADMIN")
        void shouldSaveAndUpdateOfferForOwnerAndAdmin() {
            OfferDTO dto = OfferDTO.builder().title("Senior Java Dev").location("Berlin").companyId(10L).build();
            when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
            when(offerRepository.findById(100L)).thenReturn(Optional.of(offer));
            when(offerRepository.save(any(Offer.class))).thenAnswer(inv -> inv.getArgument(0));

            OfferDTO saved = offerService.save(dto, employer);
            Optional<OfferDTO> updated = offerService.update(100L, dto, admin);

            assertNotNull(saved);
            assertTrue(updated.isPresent());
            assertEquals("Senior Java Dev", updated.get().getTitle());
        }

        @Test
        @DisplayName("softDelete, restore, and hardDelete should enforce ownership and handle non-existent IDs")
        void shouldEnforceOwnershipOnDeleteAndRestore() {
            when(offerRepository.findById(100L)).thenReturn(Optional.of(offer));
            when(offerRepository.findById(404L)).thenReturn(Optional.empty());

            assertThrows(AccessDeniedException.class, () -> offerService.softDelete(100L, otherEmployer));
            assertTrue(offerService.softDelete(100L, employer));
            assertFalse(offer.isActive());

            assertTrue(offerService.restore(100L, admin));
            assertTrue(offer.isActive());

            assertFalse(offerService.hardDelete(404L, employer));
            assertTrue(offerService.hardDelete(100L, employer));
            verify(offerRepository).deleteById(100L);
        }
    }
}
