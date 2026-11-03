package pl.pollub.agregator.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.pollub.agregator.dto.CompanyDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    private Long id;
    private String name;
    private String industry;
    private Boolean isActive;

    public Company(Long id, String name) {
        this.id = id;
        this.name = name;
        this.isActive = true;
    }

    public CompanyDTO toSnapshot() {
        return CompanyDTO.builder()
                .id(this.id)
                .name(this.name)
                .industry(this.industry)
                .build();
    }

    public CompanyDTO toDTO() {
        return toSnapshot();
    }

    public static Company fromDTO(CompanyDTO dto) {
        if (dto == null) {
            return null;
        }
        return Company.builder()
                .id(dto.getId())
                .name(dto.getName())
                .industry(dto.getIndustry())
                .isActive(true)
                .build();
    }

    public void restoreFromSnapshot(CompanyDTO snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Snapshot dla encji Company (id=" + this.id + ") nie moze byc null.");
        }
        this.id = snapshot.getId();
        this.name = snapshot.getName();
        this.industry = snapshot.getIndustry();
    }

    @Override
    public String toString() {
        return "Company{id=" + id + ", name='" + name + "', industry='" + industry + "', isActive=" + isActive + "}";
    }
}
