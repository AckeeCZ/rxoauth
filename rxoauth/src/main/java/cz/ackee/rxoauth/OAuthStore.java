package cz.ackee.rxoauth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Store of oauth2 credentials
 */
public class OAuthStore {
    public static final String TAG = OAuthStore.class.getName();
    private static final String SP_NAME = "oauth2";
    private static final String ACCESS_TOKEN_KEY = "oath2_access_token";
    private static final String REFRESH_TOKEN_KEY = "oath2_refresh_token";
    private final SharedPreferences sp;


    public OAuthStore(Context ctx) {
        sp = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public OAuthStore(SharedPreferences sp) {
        this.sp = sp;
    }

    public void saveOauthCredentials(String accessToken, String refreshToken) {
        sp.edit().putString(ACCESS_TOKEN_KEY, accessToken)
                .putString(REFRESH_TOKEN_KEY, refreshToken)
                .apply();
    }

    public String getAccessToken() {
        return sp.getString(ACCESS_TOKEN_KEY, null);
    }

    public String getRefreshToken() {
        return sp.getString(REFRESH_TOKEN_KEY, null);
    }

    public void onLogout() {
        sp.edit().clear().commit();
    }

    public void saveOauthCredentials(ICredentialsModel credentials) {
        saveOauthCredentials(credentials.getAccessToken(), credentials.getRefreshToken());
    }

}
