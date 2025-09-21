package org.openapitools.client.apis

import org.openapitools.client.infrastructure.CollectionFormats.*
import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.squareup.moshi.Json

import org.openapitools.client.models.ApiAuthOauthFacebookPostRequest
import org.openapitools.client.models.ApiAuthOauthGooglePostRequest
import org.openapitools.client.models.ApiAuthRefreshPostRequest
import org.openapitools.client.models.ApiTalleresInvitacionesAcceptPostRequest
import org.openapitools.client.models.ApiTalleresPostRequest
import org.openapitools.client.models.ApiTalleresTallerIdAlmacenesPostRequest
import org.openapitools.client.models.ApiTalleresTallerIdInvitacionesCodigoPostRequest
import org.openapitools.client.models.AuthResponse
import org.openapitools.client.models.LoginRequest
import org.openapitools.client.models.RegisterRequest

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
     * @param apiAuthRefreshPostRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/auth/logout")
    fun apiAuthLogoutPost(@Body apiAuthRefreshPostRequest: ApiAuthRefreshPostRequest? = null): Call<Unit>

    /**
     * Login/registro con Facebook access token
     * 
     * Responses:
     *  - 200: Tokens y usuario
     *
     * @param apiAuthOauthFacebookPostRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/auth/oauth/facebook")
    fun apiAuthOauthFacebookPost(@Body apiAuthOauthFacebookPostRequest: ApiAuthOauthFacebookPostRequest? = null): Call<Unit>

    /**
     * Login/registro con Google ID Token
     * 
     * Responses:
     *  - 200: Tokens y usuario
     *
     * @param apiAuthOauthGooglePostRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/auth/oauth/google")
    fun apiAuthOauthGooglePost(@Body apiAuthOauthGooglePostRequest: ApiAuthOauthGooglePostRequest? = null): Call<Unit>

    /**
     * Obtener nuevo access token usando refresh token
     * 
     * Responses:
     *  - 200: Nuevo access token
     *
     * @param apiAuthRefreshPostRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/auth/refresh")
    fun apiAuthRefreshPost(@Body apiAuthRefreshPostRequest: ApiAuthRefreshPostRequest? = null): Call<Unit>

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
     * @param apiTalleresInvitacionesAcceptPostRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/talleres/invitaciones/accept")
    fun apiTalleresInvitacionesAcceptPost(@Body apiTalleresInvitacionesAcceptPostRequest: ApiTalleresInvitacionesAcceptPostRequest? = null): Call<Unit>

    /**
     * Crear taller (usuario autenticado es propietario)
     * 
     * Responses:
     *  - 201: Taller creado
     *
     * @param apiTalleresPostRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/talleres")
    fun apiTalleresPost(@Body apiTalleresPostRequest: ApiTalleresPostRequest? = null): Call<Unit>

    /**
     * Crear almacen en taller
     * 
     * Responses:
     *  - 201: Almacen creado
     *
     * @param tallerId 
     * @param apiTalleresTallerIdAlmacenesPostRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/talleres/{tallerId}/almacenes")
    fun apiTalleresTallerIdAlmacenesPost(@Path("tallerId") tallerId: kotlin.String, @Body apiTalleresTallerIdAlmacenesPostRequest: ApiTalleresTallerIdAlmacenesPostRequest? = null): Call<Unit>

    /**
     * Crear invitación por código para un taller
     * 
     * Responses:
     *  - 201: Código de invitación (raw code sólo se devuelve al creador)
     *
     * @param tallerId 
     * @param apiTalleresTallerIdInvitacionesCodigoPostRequest  (optional)
     * @return [Call]<[Unit]>
     */
    @POST("api/talleres/{tallerId}/invitaciones/codigo")
    fun apiTalleresTallerIdInvitacionesCodigoPost(@Path("tallerId") tallerId: kotlin.String, @Body apiTalleresTallerIdInvitacionesCodigoPostRequest: ApiTalleresTallerIdInvitacionesCodigoPostRequest? = null): Call<Unit>

}
