package pl.pollub.agregator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.pollub.agregator.dto.OfferDTO;
import pl.pollub.agregator.service.OfferService;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    public List<OfferDTO> getAll(
            @RequestParam(name = "includeInactive", defaultValue = "false") boolean includeInactive,
            @RequestParam(name = "companyId", required = false) Long companyId) {
        return offerService.getAll(includeInactive, companyId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferDTO> getById(@PathVariable Long id) {
        return offerService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OfferDTO> save(@RequestBody OfferDTO dto) {
        OfferDTO created = offerService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfferDTO> update(@PathVariable Long id, @RequestBody OfferDTO dto) {
        return offerService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/soft-delete")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        boolean deleted = offerService.softDelete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}/soft")
    public ResponseEntity<Void> softDeleteByDeleteMethod(@PathVariable Long id) {
        return softDelete(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) {
        boolean deleted = offerService.hardDelete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteExplicit(@PathVariable Long id) {
        return hardDelete(id);
    }
}
