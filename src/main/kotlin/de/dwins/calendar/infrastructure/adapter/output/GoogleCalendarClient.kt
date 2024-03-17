package de.dwins.calendar.infrastructure.adapter.output

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Channel
import de.dwins.calendar.application.port.output.UserOutputPort
import de.dwins.calendar.domain.entity.GoogleCalenderSync
import de.dwins.calendar.domain.entity.toGoogleCalendarEvent
import de.dwins.calendar.infrastructure.configuration.CalendarConnectionProperties
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

private const val CALENDAR_ID = "primary"
class GoogleCalendarClient(
        private val googleAuthenticationConnector: GoogleAuthenticationConnector,
        private val userOutputPort: UserOutputPort,
        private val calendarConnectionProperties: CalendarConnectionProperties,
) : GoogleCalendarConntector {

    private val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()

    override fun registerWebhook(user: User) {
        val service: Calendar = createCalendarService(user)
        val channel = Channel()
            .setId(UUID.randomUUID().toString())
            .setExpiration(
                Instant.now().plus(30, ChronoUnit.DAYS).toEpochMilli(),
            ) // Maximum expiration period is 1 month
            .setType(
                "web_hook",
            ).setAddress(
                calendarConnectionProperties.redirectBaseUri +
                        "/calendar/${user.googleId}/events/update",
            )
        service.events().watch(CALENDAR_ID, channel).execute()
    }

    override fun synchronizeEvents(user: User, syncToken: String?, pageToken: String?): GoogleCalenderSync {
        val existingSyncToken = syncToken ?: user.syncToken
        val service: Calendar = createCalendarService(user)
        val result = if (existingSyncToken == null || pageToken != null) {
            service.events().list(CALENDAR_ID)
                .setMaxResults(100)
                .setTimeZone("UTC")
                .setTimeMin(DateTime(Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli()))
                .setTimeMax(DateTime(Instant.now().plus(30, ChronoUnit.DAYS).toEpochMilli()))
                .setPageToken(pageToken)
                .setSingleEvents(true)
                .execute()
        } else {
            service.events().list(CALENDAR_ID)
                .setMaxResults(100)
                .setTimeZone("UTC")
                .setSyncToken(existingSyncToken)
                .setSingleEvents(true)
                .execute()
        }
        return GoogleCalenderSync(
                nextSyncToken = result.nextSyncToken,
                nextPageToken = result.nextPageToken,
                events = result.items.map { item ->
                    item.toGoogleCalendarEvent()
                }
        ).also {
            userOutputPort.update(
                user.copy(
                    syncToken = it.nextSyncToken
                )
            )
        }
    }

    private fun createCalendarService(user: User): Calendar {
        val credential = googleAuthenticationConnector.initialzeAccessCredentials(user)
        val service: Calendar = Calendar.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("google-calendar-connector")
            .build()
        return service
    }
}