# Android (Kotlin) quick guide

This guide shows example Retrofit interfaces and a small token store for Android (Kotlin) to call the backend endpoints documented in `openapi.yaml`.

1) Add dependencies (Gradle Kotlin DSL):

```kotlin
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
```

2) Retrofit service example

```kotlin
interface AuthApi {
  @POST("/api/auth/login")
  suspend fun login(@Body req: LoginRequest): Response<AuthResponse>

  @POST("/api/auth/register")
  suspend fun register(@Body req: RegisterRequest): Response<AuthResponse>

  @POST("/api/auth/refresh")
  suspend fun refresh(@Body body: Map<String,String>): Response<Map<String,String>>

  @POST("/api/auth/oauth/google")
  suspend fun oauthGoogle(@Body body: Map<String,String>): Response<AuthResponse>
}

interface TallerApi {
  @POST("/api/talleres")
  suspend fun crearTaller(@Body body: Map<String,String>): Response<Map<String,Any>>

  @POST("/api/talleres/{tallerId}/almacenes")
  suspend fun crearAlmacen(@Path("tallerId") tallerId: String, @Body body: Map<String,String>): Response<Map<String,Any>>

  @POST("/api/talleres/invitaciones/accept")
  suspend fun acceptInvitation(@Body body: Map<String,String>): Response<Map<String,Any>>
}
```

3) OkHttp Interceptor for JWT

```kotlin
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val req = chain.request()
    val token = tokenProvider()
    return if (token != null) {
      val newReq = req.newBuilder().addHeader("Authorization", "Bearer $token").build()
      chain.proceed(newReq)
    } else chain.proceed(req)
  }
}
```

4) Simple in-memory token store (for demo only)

```kotlin
object TokenStore {
  var accessToken: String? = null
  var refreshToken: String? = null
}

suspend fun ensureAccessToken(api: AuthApi) {
  if (TokenStore.accessToken == null && TokenStore.refreshToken != null) {
    val resp = api.refresh(mapOf("refreshToken" to TokenStore.refreshToken!!))
    if (resp.isSuccessful) {
      TokenStore.accessToken = resp.body()?.get("accessToken")
    }
  }
}
```

Notes:
- Use secure storage for tokens on device (EncryptedSharedPreferences, Keystore-backed store).
- Handle 401 responses by attempting refresh then retrying the original request.
