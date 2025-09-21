package lk.kolitha.dana.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String scope;
    private String userType;
    private String email;
    private String fullName;
    private String profileImageUrl;
    private String jti; // JWT ID for token tracking
    private Long userId;
    private String domain;
}
