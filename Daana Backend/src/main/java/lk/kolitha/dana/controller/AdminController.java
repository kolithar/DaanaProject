package lk.kolitha.dana.controller;

import lk.kolitha.dana.entity.Charity;
import lk.kolitha.dana.service.CharityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin/charity")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Log4j2
public class AdminController {

    private final CharityService charityService;

    // Admin approves
    @PutMapping("/{id}/approve")
    public ResponseEntity<Charity> approve(@PathVariable Long id) {
        return ResponseEntity.ok(charityService.approveCharity(id));
    }

    // Admin rejects
    @PutMapping("/{id}/reject")
    public ResponseEntity<Charity> reject(@PathVariable Long id) {
        return ResponseEntity.ok(charityService.rejectCharity(id));
    }
}
