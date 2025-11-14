package org.example;

import com.symphony.bdk.core.SymphonyBdk;
import com.symphony.bdk.core.auth.exception.AuthInitializationException;
import com.symphony.bdk.core.auth.exception.AuthUnauthorizedException;
import com.symphony.bdk.core.config.model.*;
import com.symphony.bdk.http.api.ApiException;

public class StartDatafeed {

    public static void main(String[] args) throws AuthInitializationException, AuthUnauthorizedException, ApiException {
        String botUserName = "<new-bot-username>";

        final SymphonyBdk bdk = Bdk.getBdk(botUserName);

        bdk.datafeed().start();
    }
}