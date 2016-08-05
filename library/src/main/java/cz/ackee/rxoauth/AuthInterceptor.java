package cz.ackee.rxoauth;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor for oauth2 authorization
 * Created by David Bilik[david.bilik@ackee.cz] on {03/03/16}
 **/
public class AuthInterceptor implements Interceptor {
    public static final String TAG = AuthInterceptor.class.getName();


    OAuthStore oAuthStore;

    public AuthInterceptor(OAuthStore oauthStore) {
        this.oAuthStore = oauthStore;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder builder = originalRequest.newBuilder();

        if (!TextUtils.isEmpty(oAuthStore.getAccessToken())) {
            String accToken = oAuthStore.getAccessToken();
            builder.addHeader("Authorization", "Bearer " + accToken);
        }
        return chain.proceed(builder.build());
    }
}
