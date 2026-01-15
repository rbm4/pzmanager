package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.UserService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/auth")
public class SteamAuthController {
    
    private static final Logger logger = Logger.getLogger(SteamAuthController.class.getName());
    private static final String STEAM_OPENID_URL = "https://steamcommunity.com/openid/login";
    private static final String STEAM_API_URL = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/";
    
    @Value("${steam.api.key}")
    private String steamApiKey;
    
    @Value("${server.base-url:http://localhost:8080}")
    private String baseUrl;
    
    private final UserService userService;
    private final RestTemplate restTemplate;

    public SteamAuthController(UserService userService) {
        this.userService = userService;
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/steam")
    public RedirectView steamLogin(HttpServletRequest request) throws UnsupportedEncodingException {
        String returnUrl = baseUrl + "/auth/steam/callback";
        String realm = baseUrl;
        
        String steamLoginUrl = STEAM_OPENID_URL +
            "?openid.ns=" + URLEncoder.encode("http://specs.openid.net/auth/2.0", StandardCharsets.UTF_8) +
            "&openid.mode=checkid_setup" +
            "&openid.return_to=" + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8) +
            "&openid.realm=" + URLEncoder.encode(realm, StandardCharsets.UTF_8) +
            "&openid.identity=" + URLEncoder.encode("http://specs.openid.net/auth/2.0/identifier_select", StandardCharsets.UTF_8) +
            "&openid.claimed_id=" + URLEncoder.encode("http://specs.openid.net/auth/2.0/identifier_select", StandardCharsets.UTF_8);
        
        logger.info("Redirecting to Steam login: " + steamLoginUrl);
        return new RedirectView(steamLoginUrl);
    }

    @GetMapping("/steam/callback")
    public String steamCallback(
            @RequestParam Map<String, String> params,
            HttpSession session,
            HttpServletRequest request) {
        
        try {
            // Verify the OpenID response
            if (!"id_res".equals(params.get("openid.mode"))) {
                logger.warning("Invalid OpenID mode: " + params.get("openid.mode"));
                return "redirect:/login?error=invalid_response";
            }
            
            // Verify with Steam
            if (!verifyWithSteam(params, request)) {
                logger.warning("Steam verification failed");
                return "redirect:/login?error=verification_failed";
            }
            
            // Extract Steam ID
            String claimedId = params.get("openid.claimed_id");
            String steamId = extractSteamId(claimedId);
            
            if (steamId == null) {
                logger.warning("Failed to extract Steam ID from: " + claimedId);
                return "redirect:/login?error=invalid_steamid";
            }
            
            logger.info("Steam login successful - Steam ID: " + steamId);
            
            // Fetch user details from Steam API
            Map<String, Object> playerData = fetchPlayerData(steamId);
            
            if (playerData == null) {
                logger.warning("Failed to fetch player data for Steam ID: " + steamId);
                return "redirect:/login?error=api_error";
            }
            
            String username = (String) playerData.get("personaname");
            String avatarUrl = (String) playerData.get("avatarfull");
            String profileUrl = (String) playerData.get("profileurl");
            
            // Create or update user
            User user = userService.processOAuthUser(steamId, username, avatarUrl, profileUrl);
            
            // Create Spring Security authentication
            org.springframework.security.core.authority.SimpleGrantedAuthority authority = 
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole());
            
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    java.util.Collections.singletonList(authority)
                );
            
            authentication.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                .buildDetails(request));
            
            // Set authentication in SecurityContext
            org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(authentication);
            
            // Set session attributes
            session.setAttribute("user", user);
            session.setAttribute("steamId", steamId);
            session.setAttribute("username", username);
            session.setAttribute("role", user.getRole());
            
            logger.info("User logged in: " + username + " (Steam ID: " + steamId + ")");
            
            return "redirect:/player";
            
        } catch (Exception e) {
            logger.severe("Steam login error: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login?error=login_failed";
        }
    }

    private boolean verifyWithSteam(Map<String, String> params, HttpServletRequest request) {
        try {
            // Create a copy of all parameters
            Map<String, String> verifyParams = new java.util.HashMap<>(params);
            
            // Change mode to check_authentication
            verifyParams.put("openid.mode", "check_authentication");
            
            // Build form data
            org.springframework.util.MultiValueMap<String, String> formData = 
                new org.springframework.util.LinkedMultiValueMap<>();
            
            for (Map.Entry<String, String> entry : verifyParams.entrySet()) {
                if (entry.getKey().startsWith("openid.")) {
                    formData.add(entry.getKey(), entry.getValue());
                }
            }
            
            logger.info("Verifying with Steam, parameters: " + formData.keySet());
            
            // Use POST request with form data
            var headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
            
            org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> requestEntity = 
                new org.springframework.http.HttpEntity<>(formData, headers);
            
            String response = restTemplate.postForObject(STEAM_OPENID_URL, requestEntity, String.class);
            logger.info("Steam verification response: " + response);
            
            return response != null && response.contains("is_valid:true");
            
        } catch (Exception e) {
            logger.severe("Steam verification error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String extractSteamId(String claimedId) {
        if (claimedId == null) {
            return null;
        }
        
        Pattern pattern = Pattern.compile("https://steamcommunity.com/openid/id/(\\d+)");
        Matcher matcher = pattern.matcher(claimedId);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchPlayerData(String steamId) {
        try {
            String url = STEAM_API_URL + "?key=" + steamApiKey + "&steamids=" + steamId;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("response")) {
                Map<String, Object> responseData = (Map<String, Object>) response.get("response");
                if (responseData.containsKey("players")) {
                    Object playersObj = responseData.get("players");
                    if (playersObj instanceof java.util.List) {
                        java.util.List<?> players = (java.util.List<?>) playersObj;
                        if (!players.isEmpty() && players.get(0) instanceof Map) {
                            return (Map<String, Object>) players.get(0);
                        }
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            logger.severe("Error fetching player data: " + e.getMessage());
            return null;
        }
    }
}
