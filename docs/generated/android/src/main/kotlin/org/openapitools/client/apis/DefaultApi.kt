package org.openapitools.client.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.squareup.moshi.Json

import org.openapitools.client.models.AcceptInvitationRequest
import org.openapitools.client.models.ApiAuthLogoutPostRequest
import org.openapitools.client.models.AuthResponse
import org.openapitools.client.models.CrearAlmacenRequest
import org.openapitools.client.models.CrearInvitacionRequest
import org.openapitools.client.models.CrearTallerRequest
import org.openapitools.client.models.LoginRequest
import org.openapitools.client.models.OAuthFacebookRequest
import org.openapitools.client.models.OAuthGoogleRequest
import org.openapitools.client.models.RefreshRequest
import org.openapitools.client.models.RegisterRequest
import org.openapitools.client.models.UserProfile

interface DefaultApi {
    /**
     * Login con username o email + password
     * 
     * Responses:
     *  - 200: Tokens y usuario
     *  - 401: Credenciales inválidas
     *
     * @param loginRequest 
     * @return [Call]<[AuthResponse]>
     */
    @POST("api/auth/login")
    fun apiAuthLoginPost(@Body loginRequest: LoginRequest): Call<AuthResponse>

    /**
     * Revocar refresh token
     * 
     * Responses:
     *  - 200: OK
     *
     * @param apiAuthLogoutPostRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/auth/logout")
    fun apiAuthLogoutPost(@Body apiAuthLogoutPostRequest: ApiAuthLogoutPostRequest? = null): Call<Unit>

    /**
     * Obtener perfil del usuario autenticado
     * 
     * Responses:
     *  - 200: Perfil del usuario
     *  - 401: Unauthorized
     *
     * @return [Call]<[UserProfile]>
     */
    @GET("api/auth/me")
    fun apiAuthMeGet(): Call<UserProfile>

    /**
     * Login/registro con Facebook access token
     * 
     * Responses:
     *  - 200: Tokens y usuario
     *
     * @param oauthFacebookRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/auth/oauth/facebook")
    fun apiAuthOauthFacebookPost(@Body oauthFacebookRequest: OAuthFacebookRequest? = null): Call<Unit>

    /**
     * Login/registro con Google ID Token
     * 
     * Responses:
     *  - 200: Tokens y usuario
     *
     * @param oauthGoogleRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/auth/oauth/google")
    fun apiAuthOauthGooglePost(@Body oauthGoogleRequest: OAuthGoogleRequest? = null): Call<Unit>

    /**
     * Obtener nuevo access token usando refresh token
     * 
     * Responses:
     *  - 200: Nuevo access token
     *
     * @param refreshRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/auth/refresh")
    fun apiAuthRefreshPost(@Body refreshRequest: RefreshRequest? = null): Call<Unit>

    /**
     * Registro de usuario
     * 
     * Responses:
     *  - 201: Usuario creado
     *  - 409: Email o username ya registrado
     *
     * @param registerRequest 
     * @return [Call]<[AuthResponse]>
     */
    @POST("api/auth/register")
    fun apiAuthRegisterPost(@Body registerRequest: RegisterRequest): Call<AuthResponse>

    /**
     * Revocar todos los refresh tokens del usuario autenticado
     * 
     * Responses:
     *  - 200: OK
     *
     * @return [Call]<[Unit]>
     */
    @POST("api/auth/revoke-all")
    fun apiAuthRevokeAllPost(): Call<Unit>

    /**
     * Listar talleres del usuario autenticado
     * 
     * Responses:
     *  - 200: Lista de talleres
     *
     * @return [Call]<[Unit]>
     */
    @GET("api/talleres")
    fun apiTalleresGet(): Call<Unit>

    /**
     * Aceptar invitación por código
     * 
     * Responses:
     *  - 200: Usuario unido al taller
     *
     * @param acceptInvitationRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/talleres/invitaciones/accept")
    fun apiTalleresInvitacionesAcceptPost(@Body acceptInvitationRequest: AcceptInvitationRequest? = null): Call<Unit>

    /**
     * Crear taller (usuario autenticado es propietario)
     * 
     * Responses:
     *  - 201: Taller creado
     *
     * @param crearTallerRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/talleres")
    fun apiTalleresPost(@Body crearTallerRequest: CrearTallerRequest? = null): Call<Unit>

    /**
     * Crear almacen en taller
     * 
     * Responses:
     *  - 201: Almacen creado
     *
     * @param tallerId 
     * @param crearAlmacenRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/talleres/{tallerId}/almacenes")
    fun apiTalleresTallerIdAlmacenesPost(@Path("tallerId") tallerId: kotlin.String, @Body crearAlmacenRequest: CrearAlmacenRequest? = null): Call<Unit>

    /**
     * Crear invitación por código para un taller
     * 
     * Responses:
     *  - 201: Código de invitación (raw code sólo se devuelve al creador)
     *
     * @param tallerId 
     * @param crearInvitacionRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/talleres/{tallerId}/invitaciones/codigo")
    fun apiTalleresTallerIdInvitacionesCodigoPost(@Path("tallerId") tallerId: kotlin.String, @Body crearInvitacionRequest: CrearInvitacionRequest? = null): Call<Unit>

}
