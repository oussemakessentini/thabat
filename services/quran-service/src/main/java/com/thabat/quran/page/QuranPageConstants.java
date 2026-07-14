package com.thabat.quran.page;

public final class QuranPageConstants {

    public static final int MIN_PAGE = 1;
    public static final int MAX_PAGE = 604;
    public static final int TOTAL_PAGES = 604;

    private QuranPageConstants() {
    }

    public static boolean isValidPageNumber(int pageNumber) {
        return pageNumber >= MIN_PAGE && pageNumber <= MAX_PAGE;
    }
}
