package pl.pollub.agregator.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import pl.pollub.agregator.dto.CompanyDTO;
import pl.pollub.agregator.dto.OfferDTO;
import pl.pollub.agregator.entity.Company;
import pl.pollub.agregator.entity.Offer;
import pl.pollub.agregator.entity.Role;
import pl.pollub.agregator.entity.User;
import pl.pollub.agregator.repository.CompanyRepository;
import pl.pollub.agregator.repository.OfferRepository;
import pl.pollub.agregator.repository.UserRepository;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("CompanyService Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompanyService companyService;

    private User employer;
    private User otherEmployer;
    private User admin;
    private Company activeCompany;

    @BeforeAll
    static void setUpAll() {
        assertTrue(Role.values().length >= 3);
    }

    @AfterAll
    static void tearDownAll() {
        assertNotNull(CompanyService.class);
    }

    @BeforeEach
    void setUp() {
        employer = User.builder().id(1L).username("employer1").role(Role.EMPLOYER).build();
        otherEmployer = User.builder().id(2L).username("employer2").role(Role.EMPLOYER).build();
        admin = User.builder().id(99L).username("admin").role(Role.ADMIN).build();
        activeCompany = Company.builder()
                .id(10L)
                .name("TechCorp")
                .industry("IT")
                .user(employer)
                .isActive(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        reset(companyRepository, offerRepository, userRepository);
    }

    static Stream<Long> invalidIdsProvider() {
        return Stream.of(null, 0L, -1L, Long.MIN_VALUE);
    }

    @Test
    @Order(1)
    @Timeout(1)
    @DisplayName("getAll() and getAll(boolean) should handle active filter and empty lists")
    void shouldGetAllCompaniesAndHandleEmptyList() {
        when(companyRepository.findAllByIsActiveTrue()).thenReturn(List.of(activeCompany));
        when(companyRepository.findAll()).thenReturn(Collections.emptyList());

        List<CompanyDTO> activeResult = assertTimeout(Duration.ofMillis(500), () -> companyService.getAll());
        List<CompanyDTO> allResult = companyService.getAll(true);

        assertAll(
                () -> assertEquals(1, activeResult.size()),
                () -> assertEquals("TechCorp", activeResult.get(0).getName()),
                () -> assertIterableEquals(Collections.emptyList(), allResult)
        );
    }

    @ParameterizedTest
    @Order(2)
    @MethodSource("invalidIdsProvider")
    @DisplayName("getById, existsById, and getByUserId should throw IllegalArgumentException for invalid IDs")
    void shouldThrowExceptionForInvalidIds(Long invalidId) {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> companyService.getById(invalidId)),
                () -> assertThrows(IllegalArgumentException.class, () -> companyService.existsById(invalidId)),
                () -> assertThrows(IllegalArgumentException.class, () -> companyService.getByUserId(invalidId))
        );
    }

    @Test
    @Order(3)
    @DisplayName("getById and existsById should handle boundary ID Long.MAX_VALUE when not found")
    void shouldHandleMaxLongIdWhenNotFound() {
        when(companyRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
        when(companyRepository.existsById(Long.MAX_VALUE)).thenReturn(false);

        Optional<CompanyDTO> dto = companyService.getById(Long.MAX_VALUE);

        assertFalse(dto.isPresent());
        assertFalse(companyService.existsById(Long.MAX_VALUE));
    }

    @ParameterizedTest
    @Order(4)
    @CsvSource({"true, 2", "false, 1"})
    @DisplayName("getAllOffers should return active or all offers for company")
    void shouldGetAllOffersByCompanyId(boolean includeInactive, int expectedCount) {
        Offer activeOffer = Offer.builder().id(1L).title("Java Dev").company(activeCompany).isActive(true).build();
        Offer inactiveOffer = Offer.builder().id(2L).title("Old Dev").company(activeCompany).isActive(false).build();

        if (includeInactive) {
            when(offerRepository.findAllByCompanyId(10L)).thenReturn(List.of(activeOffer, inactiveOffer));
        } else {
            when(offerRepository.findAllByCompanyIdAndIsActiveTrue(10L)).thenReturn(List.of(activeOffer));
        }

        List<OfferDTO> result = includeInactive
                ? companyService.getAllOffers(10L, true)
                : companyService.getAllOffers(10L);

        assertEquals(expectedCount, result.size());
    }

    @Test
    @Order(5)
    @DisplayName("getByUserId and countActive should return expected values")
    void shouldGetByUserIdAndCountActive() {
        when(companyRepository.findAllByUserId(1L)).thenReturn(List.of(activeCompany));
        when(companyRepository.countByIsActiveTrue()).thenReturn(0L);

        List<CompanyDTO> userCompanies = companyService.getByUserId(1L);
        long count = companyService.countActive();

        assertNotSame(Collections.emptyList(), userCompanies);
        assertEquals(1, userCompanies.size());
        assertEquals(0L, count);
    }

    @ParameterizedTest
    @Order(6)
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("save should reject null DTO or blank company name")
    void shouldRejectBlankCompanyNameOnSave(String invalidName) {
        CompanyDTO dto = CompanyDTO.builder().name(invalidName).industry("IT").build();

        assertThrows(IllegalArgumentException.class, () -> companyService.save(null, employer));
        assertThrows(IllegalArgumentException.class, () -> companyService.save(dto, employer));
        verify(companyRepository, never()).save(any());
    }

    @Test
    @Order(7)
    @DisplayName("save should throw AccessDeniedException when user is unauthenticated")
    void shouldRejectSaveWhenUserIsNull() {
        CompanyDTO dto = CompanyDTO.builder().name("New Company").industry("Finance").build();

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> companyService.save(dto, null));
        assertInstanceOf(AccessDeniedException.class, ex);
    }

    @ParameterizedTest
    @Order(8)
    @EnumSource(value = Role.class, names = {"EMPLOYER", "ADMIN"})
    @DisplayName("save should assign owner properly for EMPLOYER and ADMIN")
    void shouldSaveCompanyForAuthorizedRoles(Role role) {
        assumeTrue(role == Role.EMPLOYER || role == Role.ADMIN);
        User currentUser = User.builder().id(5L).username("user5").role(role).build();
        CompanyDTO dto = CompanyDTO.builder().name("ValidCorp").industry("IT").build();
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> {
            Company c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });

        CompanyDTO saved = assertDoesNotThrow(() -> companyService.save(dto, currentUser));

        assertNotNull(saved.getId());
        assertEquals(5L, saved.getUserId());
    }

    @Nested
    @DisplayName("Ownership and Modification Tests")
    class OwnershipTests {

        @Test
        @DisplayName("update should throw AccessDeniedException when another EMPLOYER tries to modify company")
        void shouldDenyUpdateForNonOwnerEmployer() {
            CompanyDTO dto = CompanyDTO.builder().name("Hacked").industry("IT").build();
            when(companyRepository.findById(10L)).thenReturn(Optional.of(activeCompany));

            assertThrows(AccessDeniedException.class, () -> companyService.update(10L, dto, otherEmployer));
            verify(companyRepository, never()).save(any());
        }

        @Test
        @DisplayName("update should succeed for ADMIN on any company")
        void shouldAllowUpdateForAdmin() {
            CompanyDTO dto = CompanyDTO.builder().name("UpdatedCorp").industry("Cloud").build();
            when(companyRepository.findById(10L)).thenReturn(Optional.of(activeCompany));
            when(companyRepository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));

            Optional<CompanyDTO> updated = companyService.update(10L, dto, admin);

            assertTrue(updated.isPresent());
            assertLinesMatch(List.of("UpdatedCorp"), List.of(updated.get().getName()));
        }

        @RepeatedTest(value = 2, name = "softDelete and restore cycle {currentRepetition}/{totalRepetitions}")
        @DisplayName("softDelete and restore should toggle active flag for owner and deny non-owner")
        void shouldSoftDeleteAndRestoreCompany() {
            when(companyRepository.findById(10L)).thenReturn(Optional.of(activeCompany));

            assertThrows(AccessDeniedException.class, () -> companyService.softDelete(10L, otherEmployer));
            assertTrue(companyService.softDelete(10L, employer));
            assertFalse(activeCompany.getIsActive());

            assertTrue(companyService.restore(10L, employer));
            assertTrue(activeCompany.getIsActive());
        }

        @Test
        @DisplayName("hardDelete should return false when not found and throw AccessDeniedException for non-owner")
        void shouldHandleHardDeleteEdgeCases() {
            when(companyRepository.findById(999L)).thenReturn(Optional.empty());
            when(companyRepository.findById(10L)).thenReturn(Optional.of(activeCompany));

            assertFalse(companyService.hardDelete(999L, employer));
            assertThrows(AccessDeniedException.class, () -> companyService.hardDelete(10L, otherEmployer));
            assertTrue(companyService.hardDelete(10L, admin));
            verify(companyRepository).deleteById(10L);
        }
    }
}
