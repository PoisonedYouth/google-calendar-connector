package de.dwins.calendar.infrastructure.adapter.output

import de.dwins.calendar.domain.entity.GoogleCalenderSync
import java.time.Instant

/**
 * Interface for interacting with the Google Calendar API.
 */
interface GoogleCalendarConntector {

    /**
     * Registers a webhook for a user.
     *
     * @param user The [User] object representing the user for whom the webhook is being registered.
     */
    fun registerWebhook(user: User)

    /**
     * Synchronizes events from a Google Calendar account.
     *
     * @param user The [User] object representing the user for whom the events should be synchronized.
     * @param syncToken The synchronization token used for incremental synchronization. Can be null for full synchronization.
     * @param pageToken The page token for paginated retrieval of events. Can be null for the first page.
     * @return A [GoogleCalenderSync] object containing the synchronized events and synchronization metadata.
     */
    fun synchronizeEvents(user: User, syncToken: String?, pageToken: String?): GoogleCalenderSync
}

data class User(
    val id: String? = null,
    val googleId: String,
    val primaryMail: String,
    val refreshToken: String,
    val syncToken: String? = null,
    val webhookRegistered: Instant?,
)

