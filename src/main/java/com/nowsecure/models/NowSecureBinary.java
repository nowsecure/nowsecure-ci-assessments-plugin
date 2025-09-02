package com.nowsecure.models;

import hudson.FilePath;
import hudson.model.TaskListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class NowSecureBinary {
    List<String> arguments;
    FilePath toolPath;

    public NowSecureBinary(String arch, String osName, FilePath workspace) throws IOException, InterruptedException {
        var toolName = getToolName(arch, osName);
        this.toolPath = workspace.child(toolName);
        this.arguments = new LinkedList<>();
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(toolName)) {
            this.toolPath.copyFrom(inputStream);
        }
        this.toolPath.chmod(0755);
        this.arguments.add(this.toolPath.getRemote());
    }

    private String getToolName(String arch, String osName) throws IllegalArgumentException {
        String platform, architecture;
        var extension = "";

        if (osName.toLowerCase().contains("windows")) {
            platform = "windows";
            extension = ".exe";
        } else if (osName.toLowerCase().contains("linux")) {
            platform = "linux";
        } else if (osName.toLowerCase().contains("mac")) {
            platform = "darwin";
        } else {
            throw new IllegalArgumentException(
                    "Unsupported operating sytem detected. Currently we support linux, mac, and windows");
        }

        if (arch.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
            architecture = "amd64";
        } else if ("aarch64".equals(arch)) {
            architecture = "arm64";
        } else {
            throw new IllegalArgumentException(
                    "Unsupported architecture detected. Currently we support amd64 and arm64.");
        }

        // Ex: ns_linux-amd64
        return String.format("ns_%s-%s%s", platform, architecture, extension);
    }

    public NowSecureBinary addArgument(String flag) {
        this.arguments.add(flag);
        return this;
    }

    public NowSecureBinary addArgument(String flag, String value) {
        this.arguments.addAll(List.of(flag, value));
        return this;
    }

    public Process startWithListener(TaskListener listener) throws InterruptedException, IOException {
        var process = new ProcessBuilder()
                .redirectErrorStream(true)
                .command(this.arguments)
                .start();

        var outputReader = new Thread(() -> {
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    listener.getLogger().println(line);
                }
            } catch (java.io.IOException e) {
                e.printStackTrace(listener.error("Error reading process output."));
            }
        });

        outputReader.start();

        return process;
    }
}
