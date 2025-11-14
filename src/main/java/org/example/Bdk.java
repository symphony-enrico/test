package org.example;

import com.symphony.bdk.core.SymphonyBdk;
import com.symphony.bdk.core.SymphonyBdkBuilder;
import com.symphony.bdk.core.auth.exception.AuthInitializationException;
import com.symphony.bdk.core.auth.exception.AuthUnauthorizedException;
import com.symphony.bdk.core.config.model.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;

public class Bdk {

    public static SymphonyBdk getBdk(String botUserName) throws AuthInitializationException, AuthUnauthorizedException {
        return new SymphonyBdkBuilder()
                .config(getConfig(botUserName, privateKey(), sbeUrl, sbeUrl))
                .build();
    }

    public static String sbeUrl = "https://<pod-url>";
    public static String userProvisioningSA = "<user-provisioning-SA-username>";

    public static String privateKey() {
        return """
-----BEGIN PRIVATE KEY-----
-----END PRIVATE KEY-----""".trim();
    }

    public static String publicKey() {
        return """
-----BEGIN PUBLIC KEY-----
-----END PUBLIC KEY-----""".trim();
    }

    private static BdkConfig getConfig(String botUserName, String privateKeyContent, String sbeUrl, String agentUrl) {
        final URI sbeUri = URI.create(sbeUrl);

        final BdkConfig config = new BdkConfig();
        config.setHost(sbeUri.getHost());
        config.setContext(sbeUri.getPath());

        final URI agentUri = URI.create(agentUrl);
        final BdkAgentConfig agentConfig = new BdkAgentConfig();
        agentConfig.setHost(agentUri.getHost());
        agentConfig.setContext(agentUri.getPath());
        config.setAgent(agentConfig);

        final BdkBotConfig bot = new BdkBotConfig();
        bot.setUsername(botUserName);
        final BdkRsaKeyConfig pk = new BdkRsaKeyConfig();
        pk.setContent(privateKeyContent.getBytes(StandardCharsets.UTF_8));
        bot.setPrivateKey(pk);
        config.setBot(bot);

        addAppConfig(config);
        return config;
    }

    /**
     * Add app config to an existing BdkConfig, by using same key and id as bot
     */
    public static void addAppConfig(BdkConfig config) {
        BdkBotConfig bot = config.getBot();

        final BdkExtAppConfig app = new BdkExtAppConfig();
        app.setAppId(bot.getUsername());
        app.setPrivateKey(bot.getPrivateKey());

        config.setApp(app);
    }
}
