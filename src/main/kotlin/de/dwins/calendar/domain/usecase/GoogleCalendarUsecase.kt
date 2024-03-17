package de.dwins.calendar.domain.usecase

typealias GoogleId = String

/**
 * Interface for interacting with Google Calendar operations.
 */
interface GoogleCalendarUsecase {

    /**
     * Generates the authorization URL for accessing Google Calendar API.
     *
     * @return The authorization URL as a String.
     */
    fun getAuthorizationUrlForGoogleCalendarAccess(): String

    /**
     * Authorizes the calendar sync for a user with the provided authorization code.
     *
     * @param authorizationCode The authorization code obtained from the callback URL after successful authorization.
     */
    fun authorizeCalendarSyncForUser(authorizationCode: String)

    /**
     * Registers a webhook for calendar update notifications on the specified Google Calendar.
     *
     * @param googleId The GoogleId of the calendar to register the webhook for.
     */
    fun registerCalendarUpdateWebhook(googleId: GoogleId)

    /**
     * Synchronizes Google Calendar events for the specified GoogleId.
     *
     * This method is responsible for synchronizing events from the Google Calendar identified by the given GoogleId.
     * It uses the Google Calendar API and ensures that the events in the local system are up-to-date with the ones in the Google Calendar.
     *
     * @param googleId The GoogleId of the calendar to synchronize.
     */
    fun synchronizeGoogleCalendarEvents(googleId: GoogleId)
}