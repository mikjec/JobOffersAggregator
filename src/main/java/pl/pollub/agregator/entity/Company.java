package pl.pollub.agregator.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.pollub.agregator.dto.CompanyDTO;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String industry;

    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Offer> offers = new ArrayList<>();

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
            throw new IllegalArgumentException("Snapshot dla encji Company nie moze byc null.");
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
