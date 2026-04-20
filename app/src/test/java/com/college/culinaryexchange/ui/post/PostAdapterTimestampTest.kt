package com.college.culinaryexchange.ui.post

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class PostAdapterTimestampTest {

    private fun relativeTime(diffMs: Long): String {
        val now = System.currentTimeMillis()
        val timestamp = now - diffMs
        return PostAdapter.getRelativeTime(timestamp, now)
    }

    @Test fun `zero diff returns just now`() = assertEquals("just now", relativeTime(0))

    @Test fun `30 seconds returns just now`() =
        assertEquals("just now", relativeTime(TimeUnit.SECONDS.toMillis(30)))

    @Test fun `59 seconds returns just now`() =
        assertEquals("just now", relativeTime(TimeUnit.SECONDS.toMillis(59)))

    @Test fun `1 minute returns min ago`() =
        assertEquals("1 min ago", relativeTime(TimeUnit.MINUTES.toMillis(1)))

    @Test fun `45 minutes returns min ago`() =
        assertEquals("45 min ago", relativeTime(TimeUnit.MINUTES.toMillis(45)))

    @Test fun `1 hour returns hours ago`() =
        assertEquals("1 hours ago", relativeTime(TimeUnit.HOURS.toMillis(1)))

    @Test fun `23 hours returns hours ago`() =
        assertEquals("23 hours ago", relativeTime(TimeUnit.HOURS.toMillis(23)))

    @Test fun `1 day returns days ago`() =
        assertEquals("1 days ago", relativeTime(TimeUnit.DAYS.toMillis(1)))

    @Test fun `6 days returns days ago`() =
        assertEquals("6 days ago", relativeTime(TimeUnit.DAYS.toMillis(6)))

    @Test fun `7 days returns weeks ago`() =
        assertEquals("1 weeks ago", relativeTime(TimeUnit.DAYS.toMillis(7)))

    @Test fun `14 days returns 2 weeks ago`() =
        assertEquals("2 weeks ago", relativeTime(TimeUnit.DAYS.toMillis(14)))
}
