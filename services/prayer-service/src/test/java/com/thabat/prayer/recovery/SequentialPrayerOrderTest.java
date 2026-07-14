package com.thabat.prayer.recovery;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SequentialPrayerOrderTest {

    @Test
    void mapsSequenceToPrayerOrder() {
        assertThat(SequentialPrayerOrder.prayerForSequence(1)).isEqualTo(PrayerType.FAJR);
        assertThat(SequentialPrayerOrder.prayerForSequence(2)).isEqualTo(PrayerType.DHUHR);
        assertThat(SequentialPrayerOrder.prayerForSequence(3)).isEqualTo(PrayerType.ASR);
        assertThat(SequentialPrayerOrder.prayerForSequence(4)).isEqualTo(PrayerType.MAGHRIB);
        assertThat(SequentialPrayerOrder.prayerForSequence(5)).isEqualTo(PrayerType.ISHA);
        assertThat(SequentialPrayerOrder.prayerForSequence(6)).isEqualTo(PrayerType.FAJR);
        assertThat(SequentialPrayerOrder.prayerForSequence(7)).isEqualTo(PrayerType.DHUHR);
    }
}
