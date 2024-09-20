package com.genomics.gateway.filter;

import com.genomics.gateway.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;


import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private static final PathPatternParser pathPatternParser = new PathPatternParser();
    private static final List<PathPattern> excludedPatterns = Arrays.asList(
            pathPatternParser.parse("/actuator/**"),
            pathPatternParser.parse("/auth"),
            pathPatternParser.parse("/auth/"),
            pathPatternParser.parse("/auth/signup"),
            pathPatternParser.parse("/auth/signin")
    );
    
	private static final Map<PathPattern, List<String>> roleBasedUrls = Map.of(
			pathPatternParser.parse("/auth/getUserbyUserId/**"), Arrays.asList("ADMIN"),
			pathPatternParser.parse("/auth/getUserbyUsername/**"), Arrays.asList("ADMIN"),
			pathPatternParser.parse("/auth/updateUser"), Arrays.asList("ADMIN"), 
			pathPatternParser.parse("/ms1"), Arrays.asList("ADMIN", "SCIENTIST", "BIOINFORMATICIAN", "TEAM_LEAD", "CORPORATE_EXECUTIVE"),
			pathPatternParser.parse("/ms2"), Arrays.asList("ADMIN")
	);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Check if request path matches any of the excluded patterns
        String requestPath = exchange.getRequest().getPath().value();
        if (matchesAnyExcludedPattern(requestPath, exchange)) {
            return chain.filter(exchange);
        }

        // Validate if the path has "Authorization" Header and if its value starts with "Bearer"
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            return unauthorized(exchange, "The API call did not contain a JWT!");
        }

        // Validate if the JWT token is proper
        String token = authHeader.replace("Bearer", "").trim();
        if (!jwtTokenUtil.validateToken(token)) {
            return unauthorized(exchange, "The JWT in the API call is either invalid or expired!");
        }
        
        // Validate if the requested path is to be checked for authorization
        Optional<Map.Entry<PathPattern, List<String>>> matchedPattern = roleBasedUrls.entrySet().stream()
                .filter(entry -> entry.getKey().matches(exchange.getRequest().getPath()))
                .findFirst();
        
        if (matchedPattern.isEmpty()) {
        	// if the requested URL is not in the "roleBasedUrls" list, then its accessible to all
            return chain.filter(exchange);
		} else {
			// Fetch the roles needed to access the URL
			List<String> requiredRoles = matchedPattern.get().getValue();
			
			// Extract user roles from JWT
			Set<String> userRoles = jwtTokenUtil.getRolesFromToken(token);

			// Check if user has the required roles
			if (userRoles.stream().noneMatch(requiredRoles::contains)) {
				return unauthorized(exchange, "You do not have the necessary permissions to access this resource!");
			}
		}

        return chain.filter(exchange);
    }

    private boolean matchesAnyExcludedPattern(String requestPath, ServerWebExchange exchange) {
        return excludedPatterns.stream().anyMatch(pattern -> pattern.matches(exchange.getRequest().getPath()));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap(message.getBytes())));
    }
}
