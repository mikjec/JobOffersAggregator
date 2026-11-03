package pl.pollub.agregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferDTO {

    private Long id;
    private String title;
    private String location;
    private Long companyId;
}
