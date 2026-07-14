package com.thabat.quran.aggregate;

import com.thabat.quran.aggregate.dto.QuranHizbProgressResponse;
import com.thabat.quran.aggregate.dto.QuranJuzProgressResponse;
import com.thabat.quran.aggregate.dto.QuranSectionDetailResponse;
import com.thabat.quran.aggregate.dto.QuranSurahProgressResponse;
import com.thabat.quran.security.JwtUserPrincipal;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quran")
@RequiredArgsConstructor
@Validated
public class QuranAggregateController {

    private final QuranAggregateService quranAggregateService;

    @GetMapping("/surahs")
    public List<QuranSurahProgressResponse> listSurahs(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return quranAggregateService.listSurahs(principal.getUserId());
    }

    @GetMapping("/surahs/{surahNumber}")
    public QuranSectionDetailResponse getSurah(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable("surahNumber")
            @Min(value = 1, message = "surahNumber must be between 1 and 114")
            @Max(value = 114, message = "surahNumber must be between 1 and 114")
            int surahNumber
    ) {
        return quranAggregateService.getSurah(principal.getUserId(), surahNumber);
    }

    @GetMapping("/juz")
    public List<QuranJuzProgressResponse> listJuz(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return quranAggregateService.listJuz(principal.getUserId());
    }

    @GetMapping("/juz/{juzNumber}")
    public QuranSectionDetailResponse getJuz(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable("juzNumber")
            @Min(value = 1, message = "juzNumber must be between 1 and 30")
            @Max(value = 30, message = "juzNumber must be between 1 and 30")
            int juzNumber
    ) {
        return quranAggregateService.getJuz(principal.getUserId(), juzNumber);
    }

    @GetMapping("/hizbs")
    public List<QuranHizbProgressResponse> listHizbs(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return quranAggregateService.listHizbs(principal.getUserId());
    }

    @GetMapping("/hizbs/{hizbNumber}")
    public QuranSectionDetailResponse getHizb(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable("hizbNumber")
            @Min(value = 1, message = "hizbNumber must be between 1 and 60")
            @Max(value = 60, message = "hizbNumber must be between 1 and 60")
            int hizbNumber
    ) {
        return quranAggregateService.getHizb(principal.getUserId(), hizbNumber);
    }
}
