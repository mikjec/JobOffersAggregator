package pl.pollub.agregator.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.pollub.agregator.dto.CompanyDTO;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @NonNull
    private Long id;

    @NonNull
    private String name;

    private String industry;

    public CompanyDTO toSnapshot() {
        return CompanyDTO.builder()
                .id(this.id)
                .name(this.name)
                .industry(this.industry)
                .build();
    }

    public void restoreFromSnapshot(CompanyDTO dto) {
        if (dto == null) {
            return;
        }
        this.id = dto.getId();
        this.name = dto.getName();
        this.industry = dto.getIndustry();
    }

    @Override
    public String toString() {
        return "Company{id=" + id + ", name='" + name + "', industry='" + industry + "'}";
    }
}
