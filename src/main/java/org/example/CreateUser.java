package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.symphony.bdk.core.SymphonyBdk;
import com.symphony.bdk.core.SymphonyBdkBuilder;
import com.symphony.bdk.core.auth.exception.AuthInitializationException;
import com.symphony.bdk.core.auth.exception.AuthUnauthorizedException;
import com.symphony.bdk.core.config.model.*;
import com.symphony.bdk.core.service.application.ApplicationService;
import com.symphony.bdk.gen.api.model.*;
import com.symphony.bdk.http.api.ApiException;
import com.symphony.bdk.http.api.HttpClient;
import com.symphony.bdk.http.api.util.TypeReference;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CreateUser {

    public static void main(String[] args) throws Exception {
        String agentToCreate = "<agent-to-create-username>";

        final SymphonyBdk bdk = Bdk.getBdk(Bdk.userProvisioningSA);

        // AGENT CREATE
        final V2UserCreate newUser = new V2UserCreate();
        final V2UserAttributes attrs = new V2UserAttributes();
        attrs.setUserName(agentToCreate);
        attrs.setEmailAddress(agentToCreate + "@email.com"); // TODO should differ per tenant
        attrs.setDisplayName(agentToCreate);
        attrs.setAccountType(V2UserAttributes.AccountTypeEnum.SYSTEM);
        attrs.setCurrentKey(createSAKey());
        newUser.setUserAttributes(attrs);
        // FIXME temporarily not add the AI_AGENT role so that we can move forward with the deployment
        newUser.setRoles(List.of("INDIVIDUAL", "APP_SERVICE_ACCOUNT"));

        V2UserDetail userDetail = bdk.users().create(newUser);
        System.out.println("Successfully created the Agent Bot " + userDetail.getUserSystemInfo().getId());

        final String appId = userDetail.getUserAttributes().getUserName();

        final ApplicationInfo appInfo = new ApplicationInfo();
        appInfo.setAppId(appId);
        appInfo.setName(agentToCreate);
        appInfo.setPublisher("Symphony");
        appInfo.setDomain("symphony.com");

        final ApplicationDetail appDetail = new ApplicationDetail();
        appDetail.setApplicationInfo(appInfo);
        appDetail.setDescription("Test app");
        appDetail.setPermissions(List.of("GET_BASIC_USER_INFO", "GET_MESSAGES","ACT_AS_USER", "GET_EXTENDED_USER_INFO"));
        appDetail.setAuthenticationKeys(createAppKey());

        List<AppProperty> properties = new ArrayList<>();
        properties.add(new AppProperty().key("testKey").value("testValue"));
        appDetail.setProperties(properties);

        // FIXME: the public API and therefore the Java BDK haven't been updated yet for setting the appSA when creating an app
        ObjectNode createAppJson = new ObjectMapper().valueToTree(appDetail);
        createAppJson.put("appServiceAccount", userDetail.getUserSystemInfo().getId());

        HttpClient podClient = bdk.http().basePath(bdk.config().getBasePath() + "/pod").build();
        ApplicationDetail applicationDetail = podClient.path("/v1/admin/app/create")
                .header("sessionToken", bdk.botSession().getSessionToken())
                .body(createAppJson)
                .post(new TypeReference<>() {});

        String agentId = applicationDetail.getApplicationInfo().getAppId();
        System.out.println("Successfully created the Agent Application " + agentId);

        UserAppEntitlement agentAppEntitlement =
                updateAppEntitlement(bdk.applications(), bdk.botInfo().getId(), agentId, true, true);
        System.out.println("Successfully updated app entitlement");
    }

    private static V2UserKeyRequest createSAKey() {
        final V2UserKeyRequest key = new V2UserKeyRequest();
        key.setAction("SAVE");
        key.setKey(Bdk.publicKey());
        return key;
    }

    private static AppAuthenticationKeys createAppKey() {
        AppAuthenticationKeys keys = new AppAuthenticationKeys();
        AppAuthenticationKey key = new AppAuthenticationKey();
        key.setAction("SAVE");
        key.setKey(Bdk.publicKey());
        keys.setCurrent(key);
        return keys;
    }

    private static UserAppEntitlement updateAppEntitlement(ApplicationService apps, Long callerUserId, String appId,
                                                           boolean install, boolean listed) throws Exception {
        final UserAppEntitlementPatch appPatch = new UserAppEntitlementPatch();
        appPatch.setAppId(appId);
        appPatch.setInstall(install ? UserAppEntitlementPatch.InstallEnum.TRUE : UserAppEntitlementPatch.InstallEnum.FALSE);
        appPatch.setListed(listed ? UserAppEntitlementPatch.ListedEnum.TRUE : UserAppEntitlementPatch.ListedEnum.FALSE);
        return apps.patchUserApplications(callerUserId, List.of(appPatch))
                .stream()
                .filter(app -> app.getAppId().equals(appId))
                .findFirst()
                .orElseThrow(() -> new Exception());
    }
}