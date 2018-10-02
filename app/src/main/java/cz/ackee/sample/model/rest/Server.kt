package cz.ackee.sample.model.rest

import android.content.Context
import io.appflate.restmock.MockAnswer
import io.appflate.restmock.RESTMockServer
import io.appflate.restmock.RESTMockServerStarter
import io.appflate.restmock.android.AndroidAssetsFileParser
import io.appflate.restmock.android.AndroidLogger
import io.appflate.restmock.utils.RequestMatchers
import okhttp3.mockwebserver.MockResponse
import java.util.Random
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Class simulating server. It returns some data if user provides correct token.
 */
class Server(val context: Context) {

    var accessToken: String? = null
    var refreshToken: String? = null
    var expiresIn = 15L

    var itemsList = """[
        {
         "data" : %d
        },
        {
         "data" : %d
        },
        {
         "data" : %d
        }
    ]
    """.trimIndent()

    var loginResponse = """
        {
            "name" : "John Doe",
            "accessToken" : "%s",
            "refreshToken" : "%s",
            "expiresIn" : "%d"
        }
    """.trimIndent()

    init {
        RESTMockServerStarter.startSync(AndroidAssetsFileParser(context), AndroidLogger())

        RESTMockServer.whenGET(RequestMatchers.pathContains("items")).delay(TimeUnit.MILLISECONDS, 500).thenAnswer(MockAnswer {
            val tokenHeader = it.getHeader("Authorization")

            if (tokenHeader == null || tokenHeader.substring(tokenHeader.indexOf("Bearer ") + "Bearer ".length) != accessToken) {
                //unauthorized
                MockResponse()
                        .setResponseCode(401)
            } else {
                //authorized
                val random = Random()
                MockResponse()
                        .setResponseCode(200)
                        .setBody(itemsList.format(random.nextInt(), random.nextInt(), random.nextInt()))
            }
        })

        RESTMockServer.whenPOST(RequestMatchers.pathContains("login")).delay(TimeUnit.MILLISECONDS, 500).thenAnswer(MockAnswer {
            // accepts any login/password
            accessToken = UUID.randomUUID().toString()
            refreshToken = UUID.randomUUID().toString()
            MockResponse()
                    .setResponseCode(200)
                    .setBody(loginResponse.format(accessToken, refreshToken, expiresIn))

        })

        RESTMockServer.whenPOST(RequestMatchers.pathContains("refresh_token")).delay(TimeUnit.MILLISECONDS, 500).thenAnswer(MockAnswer {
            // invalid refresh token
            if (it.requestUrl.queryParameter("refresh_token") == null || it.requestUrl.queryParameter("refresh_token") != refreshToken) {
                MockResponse()
                        .setResponseCode(401)
            } else {
                accessToken = UUID.randomUUID().toString()
                refreshToken = UUID.randomUUID().toString()
                MockResponse()
                        .setResponseCode(200)
                        .setBody(loginResponse.format(accessToken, refreshToken, expiresIn))
            }
        })

        RESTMockServer.whenPOST(RequestMatchers.pathContains("logout")).delay(TimeUnit.MILLISECONDS, 500).thenAnswer(MockAnswer {
            accessToken = null
            refreshToken = null
            MockResponse()
                    .setResponseCode(200)
        })
    }
}