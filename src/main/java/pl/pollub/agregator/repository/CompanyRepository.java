package pl.pollub.agregator.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pl.pollub.agregator.dto.CompanyDTO;
import pl.pollub.agregator.dto.OfferDTO;
import pl.pollub.agregator.entity.Company;
import pl.pollub.agregator.entity.Offer;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CompanyRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Company> companyRowMapper = (rs, rowNum) -> Company.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .industry(rs.getString("industry"))
            .isActive(rs.getBoolean("is_active"))
            .build();

    private final RowMapper<Offer> offerRowMapper = (rs, rowNum) -> Offer.builder()
            .id(rs.getLong("id"))
            .title(rs.getString("title"))
            .location(rs.getString("location"))
            .companyId(rs.getObject("company_id") != null ? rs.getLong("company_id") : null)
            .isActive(rs.getBoolean("is_active"))
            .build();

    public List<CompanyDTO> getAll() {
        return getAll(false);
    }

    public List<CompanyDTO> getAll(boolean includeInactive) {
        String sql = includeInactive
                ? "SELECT * FROM company"
                : "SELECT * FROM company WHERE is_active = true";

        return jdbcTemplate.query(sql, companyRowMapper)
                .stream()
                .map(Company::toDTO)
                .toList();
    }

    public Optional<CompanyDTO> getById(Long id) {
        String sql = "SELECT * FROM company WHERE id = ?";
        try {
            Company company = jdbcTemplate.queryForObject(sql, companyRowMapper, id);
            return Optional.ofNullable(company != null ? company.toDTO() : null);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public CompanyDTO save(CompanyDTO dto) {
        Company company = Company.fromDTO(dto);

        if (company.getId() != null) {
            String sql = "INSERT INTO company (id, name, industry, is_active) VALUES (?, ?, ?, true)";
            jdbcTemplate.update(sql, company.getId(), company.getName(), company.getIndustry());
            return company.toDTO();
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO company (name, industry, is_active) VALUES (?, ?, true)";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, company.getName());
            ps.setString(2, company.getIndustry());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            company.setId(key.longValue());
        }

        return company.toDTO();
    }

    public Optional<CompanyDTO> update(Long id, CompanyDTO dto) {
        Optional<CompanyDTO> existing = getById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        Company company = Company.fromDTO(existing.get());
        company.restoreFromSnapshot(dto);
        company.setId(id);

        String sql = "UPDATE company SET name = ?, industry = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(
                sql,
                company.getName(),
                company.getIndustry(),
                id
        );

        if (rowsUpdated > 0) {
            return Optional.of(company.toDTO());
        }
        return Optional.empty();
    }

    public boolean softDelete(Long id) {
        String sql = "UPDATE company SET is_active = false WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);
        return rows > 0;
    }

    public boolean hardDelete(Long id) {
        String sql = "DELETE FROM company WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);
        return rows > 0;
    }

    public List<OfferDTO> getAllOffers(Long companyId) {
        return getAllOffers(companyId, false);
    }

    public List<OfferDTO> getAllOffers(Long companyId, boolean includeInactive) {
        String sql = includeInactive
                ? "SELECT * FROM offer WHERE company_id = ?"
                : "SELECT * FROM offer WHERE company_id = ? AND is_active = true";

        return jdbcTemplate.query(sql, offerRowMapper, companyId)
                .stream()
                .map(Offer::toDTO)
                .toList();
    }
}
