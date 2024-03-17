package de.dwins.calendar.application.port.input

import de.dwins.calendar.infrastructure.adapter.output.GoogleAuthenticationConnector
import de.dwins.calendar.infrastructure.adapter.output.GoogleCalendarConntector
import de.dwins.calendar.infrastructure.adapter.output.User
import de.dwins.calendar.application.port.output.GoogleCalendarEventOutputPort
import de.dwins.calendar.application.port.output.UserOutputPort
import de.dwins.calendar.domain.entity.GoogleCalendarEvent
import de.dwins.calendar.domain.usecase.GoogleCalendarUsecase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant

private const val CANCELLED_EVENT_STATUS = "cancelled"

class GoogleCalendarService(
        private val googleCalendarConntector: GoogleCalendarConntector,
        private val googleAuthenticationConnector: GoogleAuthenticationConnector,
        private val userOutputPort: UserOutputPort,
        private val calendarEventOutputPort: GoogleCalendarEventOutputPort
) : GoogleCalendarUsecase {

    private val logger: Logger = LoggerFactory.getLogger(GoogleCalendarService::class.java)
    override fun getAuthorizationUrlForGoogleCalendarAccess(): String {
        return googleAuthenticationConnector.getAuthorizationUrl()
    }

    override fun authorizeCalendarSyncForUser(authorizationCode: String) {
        val user = googleAuthenticationConnector.getAuthenticatedUser(authorizationCode)
        userOutputPort.update(user)

        registerCalendarUpdateWebhook(user.googleId)
    }

    override fun registerCalendarUpdateWebhook(googleId: String) {
        val user = userOutputPort.findBy(googleId)
                ?: error("User with googleId '$googleId' has not authorized access to the Google Calendar yet.")

        registerCalendarUpdateWebhook(user = user)
    }

    private fun registerCalendarUpdateWebhook(user: User) {
        googleCalendarConntector.registerWebhook(user)
        userOutputPort.update(
                user.copy(webhookRegistered = Instant.now()),
        )
    }


    override fun synchronizeGoogleCalendarEvents(googleId: String) {
        val user = userOutputPort.findBy(googleId)
                ?: error("User with googleId '$googleId' has not authorized access to the Google Calendar yet.")

        val existingEvents = calendarEventOutputPort.getGoogleCalendarEvents(googleId)
        logger.info("Loading {} existing events for user with googleId '{}'.", existingEvents.size, googleId)

        val syncResult = googleCalendarConntector.synchronizeEvents(
                user = user,
                syncToken = user.syncToken,
                pageToken = null,
        )

        val updatedEvents = syncResult.events.toMutableList()

        var pageToken = syncResult.nextPageToken
        var nextSyncToken = syncResult.nextSyncToken
        while (nextSyncToken == null) {
            googleCalendarConntector.synchronizeEvents(user, pageToken, null).also {
                pageToken = it.nextPageToken
                updatedEvents.addAll(it.events)
                nextSyncToken = it.nextSyncToken
            }
        }
        logger.info("Retrieving {} updated events for consultant with googleId '{}'.", updatedEvents.size, googleId)

        val mergedEvents = existingEvents.mapNotNull { googleCalendarEvent ->
            val updatedEvent = updatedEvents.find { it.id == googleCalendarEvent.id }
            if (updatedEvent != null) {
                when (updatedEvent.status) {
                    // In case of a host change the status of the event is also "cancelled" for the previous owner
                    CANCELLED_EVENT_STATUS -> null
                    else -> updatedEvent
                }
            } else {
                googleCalendarEvent
            }
        } + getNewEvents(updatedEvents, existingEvents)

        userOutputPort.update(
                user = user.copy(
                        syncToken = nextSyncToken
                ),
        )
        logger.info("Saving {} events for consultant with googleId '{}'.", mergedEvents.size, googleId)
        calendarEventOutputPort.saveGoogleCalendarEvents(
                googleId = googleId,
                calendarEvents = mergedEvents.toList(),
        )
    }

    private fun getNewEvents(
            updatedEvents: MutableList<GoogleCalendarEvent>,
            existingEvents: List<GoogleCalendarEvent>,
    ): List<GoogleCalendarEvent> = updatedEvents.filter { updatedEvent ->
        updatedEvent.status != CANCELLED_EVENT_STATUS && existingEvents.find { it.id == updatedEvent.id } == null
    }

}