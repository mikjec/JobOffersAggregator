package pl.pollub.agregator.entity;

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
public class Offer {

    private Long id;
    private String title;
    private String location;
    private Long companyId;
    private boolean isActive;

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
                .companyId(this.companyId)
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
                .companyId(dto.getCompanyId())
                .isActive(true)
                .build();
    }

    public void restoreFromSnapshot(OfferDTO snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Snapshot dla encji Offer (id=" + this.id + ") nie moze byc null.");
        }
        this.id = snapshot.getId();
        this.title = snapshot.getTitle();
        this.location = snapshot.getLocation();
        this.companyId = snapshot.getCompanyId();
    }

    @Override
    public String toString() {
        return "Offer{id=" + id + ", title='" + title + "', location='" + location + "', companyId=" + companyId + ", isActive=" + isActive + "}";
    }
}
