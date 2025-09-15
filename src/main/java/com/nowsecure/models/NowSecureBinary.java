package com.nowsecure.models;

import hudson.FilePath;
import hudson.model.TaskListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class NowSecureBinary {
    List<String> arguments;
    FilePath toolPath;
    String toolName;

    protected static String getToolName(String arch, String osName) throws IllegalArgumentException {
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
            throw new IllegalArgumentException(String.format(
                    "Unsupported operating system: %s. Currently we support linux/amd64, windows/amd64, and darwin/arm64",
                    osName));
        }

        if (arch.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
            architecture = "amd64";
        } else if ("aarch64".equals(arch) || "arm64".equals(arch)) {
            architecture = "arm64";
        } else {
            throw new IllegalArgumentException(String.format(
                    "Unsupported architecture: %s. Currently we support linux/amd64, windows/amd64, and darwin/arm64.",
                    arch));
        }

        if ((platform.equals("windows") || platform.equals("linux")) && architecture.equals("amd64")) {
            return String.format("ns_%s-%s%s", platform, architecture, extension);
        } else if (platform.equals("darwin") && architecture.equals("arm64")) {
            return String.format("ns_%s-%s%s", platform, architecture, extension);
        }

        throw new IllegalArgumentException(String.format(
                "Unsupported platform / architecture: %s / %s. Currently we support linux/amd64, windows/amd64, and darwin/arm64.",
                platform, arch));
    }

    public NowSecureBinary(String arch, String osName, FilePath workspace) throws InterruptedException, IOException {
        this.toolName = NowSecureBinary.getToolName(arch, osName);
        this.toolPath = workspace.child(toolName);
        this.arguments = new LinkedList<>();
        try (var inputStream = NowSecureBinary.class.getClassLoader().getResourceAsStream(this.toolName)) {
            this.toolPath.copyFrom(inputStream);
        }
        this.toolPath.chmod(0755);
        this.arguments.add(this.toolPath.getRemote());
    }

    public FilePath getFilePath() {
        return new FilePath(new File(
                NowSecureBinary.class.getClassLoader().getResource("./").getPath()));
    }

    public NowSecureBinary addArgument(String flag) {
        this.arguments.add(flag);
        return this;
    }

    public NowSecureBinary addArgument(String flag, String value) {
        this.arguments.addAll(List.of(flag, value));
        return this;
    }

    public Process startWithListener(TaskListener listener) throws IOException {
        var process = this.start();

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

    protected Process start() throws IOException {
        return new ProcessBuilder()
                .redirectErrorStream(true)
                .command(this.arguments)
                .start();
    }
}
