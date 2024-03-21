package com.data.proman.controller;

import com.data.proman.enitity.AuthTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.*;

@AllArgsConstructor
@RestController
public class AuthController {

//    @Value("${spring.security.oauth2.client.provider.auth0.issuer-uri}")
//    private String issuerUri;

    private final ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/loginuser")
    public RedirectView userLogin() throws URISyntaxException {
        String issuer = (String) getClientRegistration().getProviderDetails().getConfigurationMetadata().get("issuer");
        String clientId = getClientRegistration().getClientId();
        String redirectUri = "http://localhost:8080/callback";
        List<String> scopes = Arrays.asList("openid", "email", "profile");
//        String scope = "openid email profile";
//        Map<String, String> requestBody = new HashMap<>();
//
//        requestBody.put("response_type", "code");
//        requestBody.put("redirect_uri", redirectUri);
//        requestBody.put("client_id", clientId);
//        requestBody.put("scope", scope);

        URIBuilder uriBuilder = new URIBuilder(issuer+"authorize");
        uriBuilder.addParameter("client_id", clientId);
        uriBuilder.addParameter("redirect_uri", redirectUri);
        uriBuilder.addParameter("response_type", "code");
        uriBuilder.addParameter("scope", "openid profile email");
        URI url = uriBuilder.build();

        return new RedirectView(url.toString());
    }

    @GetMapping("/callback")
    public ResponseEntity<AuthTokenResponse> exchangeCodeForToken(HttpServletRequest request) {
        String authorizationCode = request.getParameter("code");

        String issuer = (String) getClientRegistration().getProviderDetails().getConfigurationMetadata().get("issuer");
        String clientId = getClientRegistration().getClientId();
        String clientSecret = getClientRegistration().getClientSecret();
        String redirectUri = "http://localhost:8080/callback";
        String tokenUrl = issuer + "oauth/token";
        List<String> scopes = Arrays.asList("openid", "email", "profile");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", "authorization_code");
        requestBody.put("client_id", clientId);
        requestBody.put("client_secret", clientSecret);
        requestBody.put("code", authorizationCode);
        requestBody.put("redirect_uri", redirectUri);
        requestBody.put("scope", "openid profile email");

        RestTemplate restTemplate = new RestTemplate();
        AuthTokenResponse tokenResponse = restTemplate.postForObject(tokenUrl, requestBody, AuthTokenResponse.class);

        assert tokenResponse != null;
        String accessToken = tokenResponse.getAccessToken();
        Map<String, Object> userInfo = getUserInfo(accessToken, issuer+"userinfo");
        return new ResponseEntity<>(tokenResponse,HttpStatus.OK);
    }

    @GetMapping("/getUserDetails")
    public ResponseEntity<Map<String,Object>> userDetails() {
//        String accessToken = ((OAuth2User) principal).getAttribute("access_token");

        String issuer = (String) getClientRegistration().getProviderDetails().getConfigurationMetadata().get("issuer");
        String clientId = getClientRegistration().getClientId();
        String clientSecret = getClientRegistration().getClientSecret();
        String audience = "https://dev-0gs0vrfhn5kx2q5g.us.auth0.com/api/v2/";
        String tokenUrl = issuer + "oauth/token";
        Map<String, String> requestBody = new HashMap<>();

        requestBody.put("grant_type", "client_credentials");
        requestBody.put("audience", audience);
        requestBody.put("client_id", clientId);
        requestBody.put("client_secret", clientSecret);
        AuthTokenResponse tokenResponse = restTemplate.postForObject(tokenUrl, requestBody, AuthTokenResponse.class);

        assert tokenResponse != null;
        String accessToken = tokenResponse.getAccessToken();
//        return new ResponseEntity<>(tokenResponse, HttpStatus.OK);

        String userInfoUrl = issuer + "userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        assert accessToken != null;
        headers.setBearerAuth(accessToken);
        headers.set("authorization", "Bearer "+accessToken);
        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> userinfo = response.getBody();
            return new ResponseEntity<>(userinfo, HttpStatus.OK);
        }
        else {
            // Handle errors
            System.out.println("Error retrieving user info: " + response.getStatusCode());
        }

        return null;
    }

    private ClientRegistration getClientRegistration() {
        return this.clientRegistrationRepository.findByRegistrationId("auth0");
    }

    private Map<String, Object> getUserInfo(String accessToken, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>("", headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> userInfo = response.getBody();
            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");

            // Use the retrieved user information
            System.out.println("User: " + response.getBody());
            return userInfo;
        } else {
            // Handle error (e.g., log the error)
            System.out.println("Error retrieving user information: " + response.getStatusCodeValue());
        }
        return null;
    }

}
