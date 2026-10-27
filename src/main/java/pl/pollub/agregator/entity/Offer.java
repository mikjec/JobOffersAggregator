package pl.pollub.agregator.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.pollub.agregator.dto.OfferDTO;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

    @NonNull
    private Long id;

    @NonNull
    private String title;

    private String location;

    public OfferDTO toSnapshot() {
        return OfferDTO.builder()
                .id(this.id)
                .title(this.title)
                .location(this.location)
                .build();
    }

    public void restoreFromSnapshot(OfferDTO dto) {
        if (dto == null) {
            return;
        }
        this.id = dto.getId();
        this.title = dto.getTitle();
        this.location = dto.getLocation();
    }

    @Override
    public String toString() {
        return "Offer{id=" + id + ", title='" + title + "', location='" + location + "'}";
    }
}
