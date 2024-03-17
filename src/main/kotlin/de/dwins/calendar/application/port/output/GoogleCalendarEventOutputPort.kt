package de.dwins.calendar.application.port.output

import de.dwins.calendar.domain.entity.GoogleCalendarEvent

/**
 * Interface for interacting with the output of Google Calendar events.
 */
interface GoogleCalendarEventOutputPort {
    /**
     * Retrieves Google Calendar events for a given Google ID.
     *
     * @param googleId The Google ID associated with the user.
     * @return A list of [GoogleCalendarEvent] objects representing the Google Calendar events.
     */
    fun getGoogleCalendarEvents(googleId: String): List<GoogleCalendarEvent>

    /**
     * Saves Google Calendar events for a given Google ID.
     *
     * @param googleId The Google ID associated with the user.
     * @param calendarEvents A list of [GoogleCalendarEvent] objects representing the Google Calendar events to be saved.
     */
    fun saveGoogleCalendarEvents(googleId: String, calendarEvents: List<GoogleCalendarEvent>)
}
