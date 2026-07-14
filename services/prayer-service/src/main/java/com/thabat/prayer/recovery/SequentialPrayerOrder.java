package com.thabat.prayer.recovery;

/**
 * Derives the next obligatory prayer from a 1-based sequence number.
 * Order: FAJR → DHUHR → ASR → MAGHRIB → ISHA → FAJR …
 * Mapping uses sequenceNumber % 5 (1→FAJR … 0→ISHA).
 */
public final class SequentialPrayerOrder {

    private SequentialPrayerOrder() {
    }

    public static PrayerType prayerForSequence(long sequenceNumber) {
        if (sequenceNumber < 1) {
            throw new IllegalArgumentException("sequenceNumber must be >= 1");
        }

        int remainder = (int) (sequenceNumber % 5L);
        return switch (remainder) {
            case 1 -> PrayerType.FAJR;
            case 2 -> PrayerType.DHUHR;
            case 3 -> PrayerType.ASR;
            case 4 -> PrayerType.MAGHRIB;
            case 0 -> PrayerType.ISHA;
            default -> throw new IllegalStateException("Unexpected remainder: " + remainder);
        };
    }

    public static long nextSequenceNumber(long completedCount) {
        return completedCount + 1L;
    }
}
