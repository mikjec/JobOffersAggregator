package pl.pollub.agregator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pollub.agregator.dto.CompanyDTO;
import pl.pollub.agregator.dto.OfferDTO;
import pl.pollub.agregator.service.CompanyService;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public List<CompanyDTO> getAll(@RequestParam(name = "includeInactive", defaultValue = "false") boolean includeInactive) {
        return companyService.getAll(includeInactive);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDTO> getById(@PathVariable Long id) {
        return companyService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/offers")
    public ResponseEntity<List<OfferDTO>> getAllOffers(
            @PathVariable Long id,
            @RequestParam(name = "includeInactive", defaultValue = "false") boolean includeInactive) {
        if (!companyService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(companyService.getAllOffers(id, includeInactive));
    }

    @PostMapping
    public ResponseEntity<CompanyDTO> save(@RequestBody CompanyDTO dto) {
        CompanyDTO created = companyService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyDTO> update(@PathVariable Long id, @RequestBody CompanyDTO dto) {
        return companyService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/soft-delete")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        boolean deleted = companyService.softDelete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}/soft")
    public ResponseEntity<Void> softDeleteByDeleteMethod(@PathVariable Long id) {
        return softDelete(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) {
        boolean deleted = companyService.hardDelete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteExplicit(@PathVariable Long id) {
        return hardDelete(id);
    }
}
