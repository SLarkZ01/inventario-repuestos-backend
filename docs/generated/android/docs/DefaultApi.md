# DefaultApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**apiAuthLoginPost**](DefaultApi.md#apiAuthLoginPost) | **POST** api/auth/login | Login con username o email + password
[**apiAuthLogoutPost**](DefaultApi.md#apiAuthLogoutPost) | **POST** api/auth/logout | Revocar refresh token
[**apiAuthOauthFacebookPost**](DefaultApi.md#apiAuthOauthFacebookPost) | **POST** api/auth/oauth/facebook | Login/registro con Facebook access token
[**apiAuthOauthGooglePost**](DefaultApi.md#apiAuthOauthGooglePost) | **POST** api/auth/oauth/google | Login/registro con Google ID Token
[**apiAuthRefreshPost**](DefaultApi.md#apiAuthRefreshPost) | **POST** api/auth/refresh | Obtener nuevo access token usando refresh token
[**apiAuthRegisterPost**](DefaultApi.md#apiAuthRegisterPost) | **POST** api/auth/register | Registro de usuario
[**apiTalleresGet**](DefaultApi.md#apiTalleresGet) | **GET** api/talleres | Listar talleres del usuario autenticado
[**apiTalleresInvitacionesAcceptPost**](DefaultApi.md#apiTalleresInvitacionesAcceptPost) | **POST** api/talleres/invitaciones/accept | Aceptar invitación por código
[**apiTalleresPost**](DefaultApi.md#apiTalleresPost) | **POST** api/talleres | Crear taller (usuario autenticado es propietario)
[**apiTalleresTallerIdAlmacenesPost**](DefaultApi.md#apiTalleresTallerIdAlmacenesPost) | **POST** api/talleres/{tallerId}/almacenes | Crear almacen en taller
[**apiTalleresTallerIdInvitacionesCodigoPost**](DefaultApi.md#apiTalleresTallerIdInvitacionesCodigoPost) | **POST** api/talleres/{tallerId}/invitaciones/codigo | Crear invitación por código para un taller



Login con username o email + password

### Example
```kotlin
// Import classes:
//import org.openapitools.client.*
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(DefaultApi::class.java)
val loginRequest : LoginRequest =  // LoginRequest | 

val result : AuthResponse = webService.apiAuthLoginPost(loginRequest)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **loginRequest** | [**LoginRequest**](LoginRequest.md)|  |

### Return type

[**AuthResponse**](AuthResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


Revocar refresh token

### Example
```kotlin
// Import classes:
//import org.openapitools.client.*
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(DefaultApi::class.java)
val apiAuthRefreshPostRequest : ApiAuthRefreshPostRequest =  // ApiAuthRefreshPostRequest | 

webService.apiAuthLogoutPost(apiAuthRefreshPostRequest)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **apiAuthRefreshPostRequest** | [**ApiAuthRefreshPostRequest**](ApiAuthRefreshPostRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined


Login/registro con Facebook access token

### Example
```kotlin
// Import classes:
//import org.openapitools.client.*
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(DefaultApi::class.java)
val apiAuthOauthFacebookPostRequest : ApiAuthOauthFacebookPostRequest =  // ApiAuthOauthFacebookPostRequest | 

webService.apiAuthOauthFacebookPost(apiAuthOauthFacebookPostRequest)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **apiAuthOauthFacebookPostRequest** | [**ApiAuthOauthFacebookPostRequest**](ApiAuthOauthFacebookPostRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined


Login/registro con Google ID Token

### Example
```kotlin
// Import classes:
//import org.openapitools.client.*
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(DefaultApi::class.java)
val apiAuthOauthGooglePostRequest : ApiAuthOauthGooglePostRequest =  // ApiAuthOauthGooglePostRequest | 

webService.apiAuthOauthGooglePost(apiAuthOauthGooglePostRequest)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **apiAuthOauthGooglePostRequest** | [**ApiAuthOauthGooglePostRequest**](ApiAuthOauthGooglePostRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined


Obtener nuevo access token usando refresh token

### Example
```kotlin
// Import classes:
//import org.openapitools.client.*
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(DefaultApi::class.java)
val apiAuthRefreshPostRequest : ApiAuthRefreshPostRequest =  // ApiAuthRefreshPostRequest | 

webService.apiAuthRefreshPost(apiAuthRefreshPostRequest)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **apiAuthRefreshPostRequest** | [**ApiAuthRefreshPostRequest**](ApiAuthRefreshPostRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined


Registro de usuario

### Example
```kotlin
// Import classes:
//import org.openapitools.client.*
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(DefaultApi::class.java)
val registerRequest : RegisterRequest =  // RegisterRequest | 

val result : AuthResponse = webService.apiAuthRegisterPost(registerRequest)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **registerRequest** | [**RegisterRequest**](RegisterRequest.md)|  |

### Return type

[**AuthResponse**](AuthResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


Listar talleres del usuario autenticado

### Example
```kotlin
// Import classes:
//import org.openapitools.client.*
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiClient = ApiClient()
apiClient.setBearerToken("TOKEN")
val webService = apiClient.createWebservice(DefaultApi::class.java)

webService.apiTalleresGet()
```

### Parameters
This endpoint does not need any parameter.

### Return type

null (empty response body)

### Authorization


Configure bearerAuth:
    ApiClient().setBearerToken("TOKEN")

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined


Aceptar invitación por código

### Example
```kotlin
// Import classes:
//import org.openapitools.client.*
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiClient = ApiClient()
apiClient.setBearerToken("TOKEN")
val webService = apiClient.createWebservice(DefaultApi::class.java)
val apiTalleresInvitacionesAcceptPostRequest : ApiTalleresInvitacionesAcceptPostRequest =  // ApiTalleresInvitacionesAcceptPostRequest | 

webService.apiTalleresInvitacionesAcceptPost(apiTalleresInvitacionesAcceptPostRequest)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **apiTalleresInvitacionesAcceptPostRequest** | [**ApiTalleresInvitacionesAcceptPostRequest**](ApiTalleresInvitacionesAcceptPostRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization


Configure bearerAuth:
    ApiClient().setBearerToken("TOKEN")

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined


Crear taller (usuario autenticado es propietario)

### Example
```kotlin
// Import classes:
//import org.openapitools.client.*
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiClient = ApiClient()
apiClient.setBearerToken("TOKEN")
val webService = apiClient.createWebservice(DefaultApi::class.java)
val apiTalleresPostRequest : ApiTalleresPostRequest =  // ApiTalleresPostRequest | 

webService.apiTalleresPost(apiTalleresPostRequest)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **apiTalleresPostRequest** | [**ApiTalleresPostRequest**](ApiTalleresPostRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization


Configure bearerAuth:
    ApiClient().setBearerToken("TOKEN")

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined


Crear almacen en taller

### Example
```kotlin
// Import classes:
//import org.openapitools.client.*
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiClient = ApiClient()
apiClient.setBearerToken("TOKEN")
val webService = apiClient.createWebservice(DefaultApi::class.java)
val tallerId : kotlin.String = tallerId_example // kotlin.String | 
val apiTalleresTallerIdAlmacenesPostRequest : ApiTalleresTallerIdAlmacenesPostRequest =  // ApiTalleresTallerIdAlmacenesPostRequest | 

webService.apiTalleresTallerIdAlmacenesPost(tallerId, apiTalleresTallerIdAlmacenesPostRequest)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **tallerId** | **kotlin.String**|  |
 **apiTalleresTallerIdAlmacenesPostRequest** | [**ApiTalleresTallerIdAlmacenesPostRequest**](ApiTalleresTallerIdAlmacenesPostRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization


Configure bearerAuth:
    ApiClient().setBearerToken("TOKEN")

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined


Crear invitación por código para un taller

### Example
```kotlin
// Import classes:
//import org.openapitools.client.*
//import org.openapitools.client.infrastructure.*
//import org.openapitools.client.models.*

val apiClient = ApiClient()
apiClient.setBearerToken("TOKEN")
val webService = apiClient.createWebservice(DefaultApi::class.java)
val tallerId : kotlin.String = tallerId_example // kotlin.String | 
val apiTalleresTallerIdInvitacionesCodigoPostRequest : ApiTalleresTallerIdInvitacionesCodigoPostRequest =  // ApiTalleresTallerIdInvitacionesCodigoPostRequest | 

webService.apiTalleresTallerIdInvitacionesCodigoPost(tallerId, apiTalleresTallerIdInvitacionesCodigoPostRequest)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **tallerId** | **kotlin.String**|  |
 **apiTalleresTallerIdInvitacionesCodigoPostRequest** | [**ApiTalleresTallerIdInvitacionesCodigoPostRequest**](ApiTalleresTallerIdInvitacionesCodigoPostRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization


Configure bearerAuth:
    ApiClient().setBearerToken("TOKEN")

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

