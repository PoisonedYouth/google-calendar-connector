package de.dwins.calendar.domain.entity

import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventAttendee
import com.google.api.services.calendar.model.EventDateTime
import java.time.Instant

data class GoogleCalendarEvent(
        val id: String,
        val description: String,
        val summary: String,
        val attendees: List<GoogleCalendarEventAttendee>,
        val creator: String,
        val organizer: String,
        val location: String,
        val start: Instant,
        val end: Instant,
        val recurringEventId: String? = null,
        val eventType: String,
        val status: String,
)

data class GoogleCalendarEventAttendee(
        val id: String? = null,
        val email: String,
        val optional: Boolean,
        val organizer: Boolean,
        val responseStatus: String,
)

data class GoogleCalenderSync(
        val nextSyncToken: String?,
        val nextPageToken: String?,
        val events: List<GoogleCalendarEvent>
)

fun Event.toGoogleCalendarEvent(): GoogleCalendarEvent = GoogleCalendarEvent(
        id = id,
        description = description ?: "-",
        summary = summary ?: "-",
        attendees = attendees?.map { it.toGoogleCalendarEventAttendee() } ?: emptyList(),
        creator = creator?.email ?: "-",
        organizer = organizer?.email ?: "-",
        location = location ?: "-",
        start = start?.toLocalDateTime() ?: Instant.MIN,
        end = end?.toLocalDateTime() ?: Instant.MIN,
        recurringEventId = recurringEventId,
        eventType = eventType ?: "-",
        status = status ?: "-"
)

fun EventAttendee.toGoogleCalendarEventAttendee() = GoogleCalendarEventAttendee(
        id = this.id,
        email = this.email,
        optional = this.optional ?: false,
        organizer = this.organizer ?: false,
        responseStatus = this.responseStatus,
)

fun EventDateTime.toLocalDateTime(): Instant {
    return if (this.dateTime != null) {
        Instant.ofEpochMilli(this.dateTime.value)
    } else {
        Instant.ofEpochMilli(this.date.value)
    }
}