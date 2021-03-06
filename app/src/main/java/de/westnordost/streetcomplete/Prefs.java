package de.westnordost.streetcomplete;

/** Constant class to have all the identifiers for shared preferences in one place */
public class Prefs
{
	public static final String

			OAUTH = "oauth",
			OAUTH_ACCESS_TOKEN = "oauth.accessToken",
			OAUTH_ACCESS_TOKEN_SECRET = "oauth.accessTokenSecret",
			MAP_TILECACHE = "map.tilecache",
			CURRENT_COUNTRY = "location.currentCountry",
			OSM_USER_ID = "osm.userid",
			SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS = "display.nonQuestionNotes",
			QUESTS_EXPIRATION_TIME_IN_MIN = "download.expirationTime",
			AUTOSYNC = "autosync";

	public enum Autosync
	{
		ON, WIFI, OFF
	}
}
