// KakaoOAuthClient.java
package me.nam.dreamdriversserver.common.oauth;

import lombok.RequiredArgsConstructor;
import me.nam.dreamdriversserver.domain.user.dto.KakaoTokenResponse;
import me.nam.dreamdriversserver.domain.user.dto.KakaoUserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private final WebClient webClient;

    @Value("${kakao.oauth.client-id}")   private String clientId;
    @Value("${kakao.oauth.client-secret:}") private String clientSecret;
    @Value("${kakao.oauth.redirect-uri}")   private String redirectUri;
    @Value("${kakao.oauth.token-uri}")      private String tokenUri;
    @Value("${kakao.oauth.userinfo-uri}")   private String userInfoUri;

    public KakaoTokenResponse exchangeToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type",   "authorization_code");
        form.add("client_id",    clientId);
        if (!clientSecret.isBlank()) form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("code",         code);

        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .block();
    }

    public KakaoUserResponse getUser(String kakaoAccessToken) {
        var temp = webClient.get()
                .uri(userInfoUri)
                .headers(h -> h.setBearerAuth(kakaoAccessToken))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        System.out.println(temp
        );

        return webClient.get()
                .uri(userInfoUri)
                .headers(h -> h.setBearerAuth(kakaoAccessToken))
                .retrieve()
                .bodyToMono(KakaoUserResponse.class)
                .block();
    }
}