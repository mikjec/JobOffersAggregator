package pl.pollub.agregator.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OfferDTO {

    private Long id;
    private String title;
    private String location;
}
