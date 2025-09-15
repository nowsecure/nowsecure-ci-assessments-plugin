package com.nowsecure.plugin;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.util.Secret;
import java.io.IOException;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class NowSecurePluginTest {

    final String binaryFilePath = "./";
    final String group = "abc";

    public void setupCredentials(JenkinsRule jenkins, String secretId, String secretText) throws IOException {
        var secret = Secret.fromString(secretText);
        StringCredentialsImpl credential =
                new StringCredentialsImpl(CredentialsScope.GLOBAL, secretId, "Test plain text credential", secret);
        CredentialsStore store =
                CredentialsProvider.lookupStores(jenkins.jenkins).iterator().next();
        store.addCredentials(Domain.global(), credential);
    }

    @Test
    void invalidCredentialIdShouldFail(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        var builder = new NowSecurePlugin(binaryFilePath, group, "bad token credential id");
        project.getBuildersList().add(builder);
        var build = jenkins.buildAndAssertStatus(Result.FAILURE, project);
        jenkins.assertLogContains("Could not find a TextCredential matching the specified credentialId", build);
    }

    @Test
    void validCredentialIdShouldSucceed(JenkinsRule jenkins) throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        var id = "some-id";
        setupCredentials(jenkins, id, "some-text");
        var builder = new NowSecurePlugin(binaryFilePath, group, id);
        // Create the Secret object
        project.getBuildersList().add(builder);
        var build = jenkins.buildAndAssertStatus(Result.SUCCESS, project);
        jenkins.assertLogContains("Finished: SUCCESS", build);
    }
}
