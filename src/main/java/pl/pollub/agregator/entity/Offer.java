package pl.pollub.agregator.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.pollub.agregator.dto.OfferDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "offer")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Builder.Default
    private boolean isActive = true;

    public Offer(Long id, String title) {
        this.id = id;
        this.title = title;
        this.isActive = true;
    }

    public OfferDTO toSnapshot() {
        return OfferDTO.builder()
                .id(this.id)
                .title(this.title)
                .location(this.location)
                .companyId(this.company != null ? this.company.getId() : null)
                .build();
    }

    public OfferDTO toDTO() {
        return toSnapshot();
    }

    public static Offer fromDTO(OfferDTO dto) {
        if (dto == null) {
            return null;
        }
        return Offer.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .location(dto.getLocation())
                .isActive(true)
                .build();
    }

    public void restoreFromSnapshot(OfferDTO snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Snapshot dla encji Offer nie moze byc null.");
        }
        this.id = snapshot.getId();
        this.title = snapshot.getTitle();
        this.location = snapshot.getLocation();
    }

    @Override
    public String toString() {
        return "Offer{id=" + id + ", title='" + title + "', location='" + location + "', companyId=" + (company != null ? company.getId() : null) + ", isActive=" + isActive + "}";
    }
}
