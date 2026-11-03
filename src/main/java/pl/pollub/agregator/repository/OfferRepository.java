package pl.pollub.agregator.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pl.pollub.agregator.dto.OfferDTO;
import pl.pollub.agregator.entity.Offer;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OfferRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Offer> offerRowMapper = (rs, rowNum) -> Offer.builder()
            .id(rs.getLong("id"))
            .title(rs.getString("title"))
            .location(rs.getString("location"))
            .companyId(rs.getObject("company_id") != null ? rs.getLong("company_id") : null)
            .isActive(rs.getBoolean("is_active"))
            .build();

    public List<OfferDTO> getAll() {
        return getAll(false);
    }

    public List<OfferDTO> getAll(boolean includeInactive) {
        String sql = includeInactive
                ? "SELECT * FROM offer"
                : "SELECT * FROM offer WHERE is_active = true";

        return jdbcTemplate.query(sql, offerRowMapper)
                .stream()
                .map(Offer::toDTO)
                .toList();
    }

    public Optional<OfferDTO> getById(Long id) {
        String sql = "SELECT * FROM offer WHERE id = ?";
        try {
            Offer offer = jdbcTemplate.queryForObject(sql, offerRowMapper, id);
            return Optional.ofNullable(offer != null ? offer.toDTO() : null);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public OfferDTO save(OfferDTO dto) {
        Offer offer = Offer.fromDTO(dto);

        if (offer.getId() != null) {
            String sql = "INSERT INTO offer (id, title, location, company_id, is_active) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(
                    sql,
                    offer.getId(),
                    offer.getTitle(),
                    offer.getLocation(),
                    offer.getCompanyId(),
                    offer.isActive()
            );
            return offer.toDTO();
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO offer (title, location, company_id, is_active) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, offer.getTitle());
            ps.setString(2, offer.getLocation());
            if (offer.getCompanyId() != null) {
                ps.setLong(3, offer.getCompanyId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            ps.setBoolean(4, offer.isActive());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            offer.setId(key.longValue());
        }

        return offer.toDTO();
    }

    public Optional<OfferDTO> update(Long id, OfferDTO dto) {
        Optional<Offer> existing = findEntityById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }

        Offer offer = existing.get();
        offer.restoreFromSnapshot(dto);
        offer.setId(id);

        String sql = "UPDATE offer SET title = ?, location = ?, company_id = ?, is_active = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(
                sql,
                offer.getTitle(),
                offer.getLocation(),
                offer.getCompanyId(),
                offer.isActive(),
                id
        );

        if (rowsUpdated > 0) {
            return Optional.of(offer.toDTO());
        }
        return Optional.empty();
    }

    public boolean softDelete(Long id) {
        String sql = "UPDATE offer SET is_active = false WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);
        return rows > 0;
    }

    public boolean hardDelete(Long id) {
        String sql = "DELETE FROM offer WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);
        return rows > 0;
    }

    public List<OfferDTO> getByCompanyId(Long companyId) {
        String sql = "SELECT * FROM offer WHERE company_id = ? AND is_active = true";
        return jdbcTemplate.query(sql, offerRowMapper, companyId)
                .stream()
                .map(Offer::toDTO)
                .toList();
    }

    private Optional<Offer> findEntityById(Long id) {
        String sql = "SELECT * FROM offer WHERE id = ?";
        try {
            Offer offer = jdbcTemplate.queryForObject(sql, offerRowMapper, id);
            return Optional.ofNullable(offer);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
