package dat.controllers.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dat.daos.security.SecurityDAO;
import dat.exceptions.ApiException;
import dat.exceptions.NotAuthorizedException;
import dat.exceptions.ValidationException;
import dat.daos.security.SecurityDAO;
import dat.entities.security.User;
import dat.utils.Utils;
import dk.bugelhartmann.ITokenSecurity;
import dk.bugelhartmann.TokenSecurity;
import dk.bugelhartmann.TokenVerificationException;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.*;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityController implements ISecurityController
{
    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    ObjectMapper objectMapper = new ObjectMapper();
    ITokenSecurity tokenSecurity = new TokenSecurity();
    private SecurityDAO securityDAO;

    public SecurityController(EntityManagerFactory emf)
    {
        securityDAO = SecurityDAO.getInstance(emf);
    }


    @Override
    public Handler register()
    {
        return (ctx) ->
        {
            UserDTO newUser = ctx.bodyAsClass(UserDTO.class);
            User createdUser = securityDAO.create(new User(newUser.getUsername(), newUser.getPassword()));
            ctx.json(new UserDTO(createdUser.getUsername(), createdUser.getRolesAsStrings()));
        };
    }
    @Override
    public Handler login()
    {
        return (ctx) ->
        {
            ObjectNode returnObject = objectMapper.createObjectNode(); // for sending json messages back to the client
            try
            {
                UserDTO user = ctx.bodyAsClass(UserDTO.class);
                UserDTO verifiedUser = securityDAO.getVerifiedUser(user.getUsername(), user.getPassword());
                String token = createToken(verifiedUser);

                ctx.status(200).json(returnObject
                        .put("token", token)
                        .put("username", verifiedUser.getUsername()));

            }
            catch (EntityNotFoundException | ValidationException e)
            {
                ctx.status(401);
                System.out.println(e.getMessage());
                ctx.json(returnObject.put("msg", e.getMessage()));
            }
        };
    }


    @Override
    public Handler authenticate()
    {
        ObjectNode returnObject = objectMapper.createObjectNode();

        return (ctx) ->
        {
            // This is a preflight request => no need for authentication
            if (ctx.method().toString().equals("OPTIONS"))
            {
                ctx.status(200);
                return;
            }
            // If the endpoint is not protected with roles or is open to ANYONE role, then skip
            Set<String> allowedRoles = ctx.routeRoles().stream().map(role -> role.toString().toUpperCase()).collect(Collectors.toSet());
            if (isOpenEndpoint(allowedRoles))
                return;

            // If there is no token we do not allow entry
            String header = ctx.header("Authorization");
            if (header == null)
            {
                throw new UnauthorizedResponse("Authorization header is missing"); // UnauthorizedResponse is javalin 6 specific but response is not json!
//                throw new dk.cphbusiness.exceptions.ApiException(401, "Authorization header is missing");
            }

            // If the Authorization Header was malformed, then no entry
            String token = header.split(" ")[1];
            if (token == null)
            {
                throw new UnauthorizedResponse("Authorization header is malformed"); // UnauthorizedResponse is javalin 6 specific but response is not json!
//                throw new dk.cphbusiness.exceptions.ApiException(401, "Authorization header is malformed");

            }
            UserDTO verifiedTokenUser = verifyToken(token);
            if (verifiedTokenUser == null)
            {
                throw new UnauthorizedResponse("Invalid user or token"); // UnauthorizedResponse is javalin 6 specific but response is not json!
//                throw new dk.cphbusiness.exceptions.ApiException(401, "Invalid user or token");
            }
            ctx.attribute("user", verifiedTokenUser); // -> ctx.attribute("user") in ApplicationConfig beforeMatched filter
        };
    }

    @Override
    public Handler authorize()
    {
        ObjectNode returnObject = objectMapper.createObjectNode();

        return (ctx) ->
        {
            Set<String> allowedRoles = ctx.routeRoles()
                    .stream()
                    .map(role -> role.toString().toUpperCase())
                    .collect(Collectors.toSet());

            // 1. Check if the endpoint is open to all (either by not having any roles or having the ANYONE role set
            if (isOpenEndpoint(allowedRoles))
                return;
            // 2. Get user and ensure it is not null
            UserDTO user = ctx.attribute("user");
            if (user == null)
            {
                throw new ForbiddenResponse("No user was added from the token");
//                throw new dk.cphbusiness.exceptions.ApiException(401, "No user was added from token");
            }

            // 3. See if any role matches
            if (!userHasAllowedRole(user, allowedRoles))
                throw new ForbiddenResponse("User was not authorized with roles: " + user.getRoles() + ". Needed roles are: " + allowedRoles);
//                throw new ApiException(403,"User was not authorized with roles: "+ user.getRoles()+". Needed roles are: "+ allowedRoles);

        };
    }


    private String createToken(UserDTO user) throws Exception
    {
        try
        {
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            String SECRET_KEY;

            if (System.getenv("DEPLOYED") != null)
            {
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                SECRET_KEY = System.getenv("SECRET_KEY");
            }
            else
            {
                ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
                TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
                SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
            }

            logger.info("Creating token for user: {}", user.getUsername());
            String token = tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
            logger.info("Token created successfully for user: {}", user.getUsername());
            return token;
        }
        catch (Exception e)
        {
            logger.error("Error creating token for user: {}", user.getUsername(), e);
            throw new ApiException(500, "Could not create token");
        }
    }

    private static boolean userHasAllowedRole(UserDTO user, Set<String> allowedRoles)
    {
        return user.getRoles().stream()
                .anyMatch(role -> allowedRoles.contains(role.toUpperCase()));
    }


    private boolean isOpenEndpoint(Set<String> allowedRoles)
    {
        // If the endpoint is not protected with any roles:
        if (allowedRoles.isEmpty())
            return true;

        // 1. Get permitted roles and Check if the endpoint is open to all with the ANYONE role
        if (allowedRoles.contains("ANYONE"))
        {
            return true;
        }
        return false;
    }


    private UserDTO verifyToken(String token) {
        boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
        String SECRET = IS_DEPLOYED ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "config.properties");

        try {
            if (tokenSecurity.tokenIsValid(token, SECRET) && tokenSecurity.tokenNotExpired(token)) {
                return tokenSecurity.getUserWithRolesFromToken(token);
            } else {
                throw new NotAuthorizedException(403, "Token is not valid");
            }
        } catch (ParseException | NotAuthorizedException | TokenVerificationException e) {
            e.printStackTrace();
            throw new ApiException(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized. Could not verify token");
        }
    }

    // Health check for the API. Used in deployment
    public void healthCheck(@NotNull Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }
}
