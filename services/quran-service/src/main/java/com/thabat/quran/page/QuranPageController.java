package com.thabat.quran.page;

import com.thabat.quran.page.dto.QuranPageProgressResponse;
import com.thabat.quran.page.dto.QuranProgressSummaryResponse;
import com.thabat.quran.page.dto.RecordQuranReviewRequest;
import com.thabat.quran.page.dto.UpdateQuranPageRequest;
import com.thabat.quran.security.JwtUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quran")
@RequiredArgsConstructor
@Validated
public class QuranPageController {

    private final QuranPageService quranPageService;

    @GetMapping("/pages")
    public List<QuranPageProgressResponse> listPages(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam(name = "status", required = false) QuranPageStatus status
    ) {
        return quranPageService.listPages(principal.getUserId(), status);
    }

    @GetMapping("/pages/{pageNumber}")
    public QuranPageProgressResponse getPage(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable("pageNumber")
            @Min(value = QuranPageConstants.MIN_PAGE, message = "pageNumber must be between 1 and 604")
            @Max(value = QuranPageConstants.MAX_PAGE, message = "pageNumber must be between 1 and 604")
            int pageNumber
    ) {
        return quranPageService.getPage(principal.getUserId(), pageNumber);
    }

    @PutMapping("/pages/{pageNumber}")
    public QuranPageProgressResponse updatePage(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable("pageNumber")
            @Min(value = QuranPageConstants.MIN_PAGE, message = "pageNumber must be between 1 and 604")
            @Max(value = QuranPageConstants.MAX_PAGE, message = "pageNumber must be between 1 and 604")
            int pageNumber,
            @Valid @RequestBody UpdateQuranPageRequest request
    ) {
        return quranPageService.updatePage(principal.getUserId(), pageNumber, request);
    }

    @PostMapping("/pages/{pageNumber}/reviews")
    @ResponseStatus(HttpStatus.OK)
    public QuranPageProgressResponse recordReview(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable("pageNumber")
            @Min(value = QuranPageConstants.MIN_PAGE, message = "pageNumber must be between 1 and 604")
            @Max(value = QuranPageConstants.MAX_PAGE, message = "pageNumber must be between 1 and 604")
            int pageNumber,
            @Valid @RequestBody RecordQuranReviewRequest request
    ) {
        return quranPageService.recordReview(principal.getUserId(), pageNumber, request);
    }

    @GetMapping("/progress")
    public QuranProgressSummaryResponse getProgress(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return quranPageService.getProgress(principal.getUserId());
    }
}
