package com.cleevio.vexl.module.facebook.service;

import it.vexl.common.crypto.CryptoLibrary;
import com.cleevio.vexl.module.contact.exception.InvalidFacebookToken;
import com.cleevio.vexl.module.contact.service.ContactService;
import com.cleevio.vexl.module.facebook.dto.FacebookUser;
import com.cleevio.vexl.module.contact.exception.FacebookException;
import com.cleevio.vexl.module.facebook.dto.NewFacebookFriends;
import com.cleevio.vexl.module.user.entity.User;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.exception.FacebookOAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service for connection to Facebook service.
 * <p>
 * We return the current user, in the friends' field we return his friends who use the application
 * and in the friends.friends field we return mutual friends who use the application.
 * WARNING - the user himself will also be in the mutual friends.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FacebookService {

    private final ContactService contactService;

    public FacebookUser retrieveContacts(String facebookId, String accessToken)
            throws FacebookException, InvalidFacebookToken {

        log.info("Retrieving contacts for user {}",
                facebookId);

        try {
            final FacebookClient client = new DefaultFacebookClient(accessToken, Version.LATEST);
            final FacebookUser facebookUser = client.fetchObject(facebookId, FacebookUser.class,
                    Parameter.with("fields", "id,name,picture,friends{id,name,picture,friends}")
            );
            log.info("Successfully fetched {} friends.",
                    facebookUser.getFriends().size()
            );
            return facebookUser;

        } catch (FacebookOAuthException e) {
            log.error("Invalid Facebook token.", e);
            throw new InvalidFacebookToken();
        } catch (Exception e) {
            log.error("Error occurred during fetching data from Facebook", e);
            throw new FacebookException();
        }
    }

    @Transactional(readOnly = true)
    public NewFacebookFriends retrieveFacebookNotImportedConnection(User user, String facebookId, String accessToken)
            throws FacebookException, InvalidFacebookToken {
        log.info("Checking for new Facebook connections for user {}",
                user.getId());

        List<FacebookUser> newConnections = new ArrayList<>();

        final FacebookUser facebookUser = retrieveContacts(facebookId, accessToken);

        final Set<String> existingFriends = this.contactService.retrieveExistingContacts(
                user.getHash()
        );

        facebookUser.getFriends().forEach(f -> {
            if (!existingFriends.contains(CryptoLibrary.instance.sha256(f.getId()))) {
                newConnections.add(f);
            }
        });

        log.info("Found {} new Facebook contacts",
                newConnections.size());

        return new NewFacebookFriends(facebookUser, newConnections);
    }
}
