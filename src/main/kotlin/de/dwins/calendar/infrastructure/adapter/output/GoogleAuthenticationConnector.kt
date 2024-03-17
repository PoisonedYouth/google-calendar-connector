package de.dwins.calendar.infrastructure.adapter.output

import com.google.api.client.auth.oauth2.Credential

/**
 * Interface for interacting with the Google API authentication.
 *
 */
interface GoogleAuthenticationConnector {

    /**
     * Returns the authorization URL for connecting with the Google Calendar API.
     *
     * @return The authorization URL as a [String].
     */
    fun getAuthorizationUrl(): String

    /**
     * Retrieves the authenticated user based on the provided authorization code.
     * The authorization code is used for retrieving the access token.
     *
     * @param code The authorization code obtained after the user grants access to the Google Calendar API.
     * @return The [User] object representing the authenticated user.
     */
    fun getAuthenticatedUser(code: String): User

    /**
     * Initializes the access credentials for the provided user.
     *
     * @param user The [User] object representing the user.
     * @return The initialized [Credential] object.
     */
    fun initialzeAccessCredentials(user: User): Credential

}