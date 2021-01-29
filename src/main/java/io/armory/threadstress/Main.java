package io.armory.threadstress;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.exec.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class Main {

    private final boolean useKubectl;
    private final int rateMs;
    private final int sleepSec;

    public static void main(String[] args) throws InterruptedException {
        boolean useKubectl = Arrays.asList(args).contains("-k");
        int rateMs = Integer.parseInt(Optional.ofNullable(System.getenv("RATE_MS")).orElse("50"));
        int sleepSec = Integer.parseInt(Optional.ofNullable(System.getenv("SLEEP_SEC")).orElse("1"));

        System.out.println("Usage: <program> [-k] [rateMs] [sleepSec]");
        if (useKubectl) {
            System.out.println("Using kubectl");
            System.out.println("Rate: " + rateMs + "ms");
        } else {
            System.out.println("Rate: " + rateMs + "ms");
            System.out.println("Sleep: " + sleepSec + "s");
        }
        new Main(useKubectl, rateMs, sleepSec).run();
    }

    public Main(boolean useKubectl, int rateMs, int sleepSec) {
        this.useKubectl = useKubectl;
        this.rateMs = rateMs;
        this.sleepSec = sleepSec;
    }

    private void run() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setNameFormat("AgentScheduler-%d").build());
        ForkJoinPool fjp = new ForkJoinPool(100);

        while (true) {
            executorService.submit(this::execKubectlLike);
            fjp.submit(this::execCfLike);
            Thread.sleep(rateMs);
        }
    }

    private void execKubectlLike() {
        ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
        ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
        Executor executor =
                buildExecutor(new PumpStreamHandler(stdOut, stdErr, null));
        try {
            executor.execute(getCommandLine(), getEnvironment());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CommandLine getCommandLine() {
        CommandLine commandLine;
        if (useKubectl) {
            try {
                Thread.sleep(sleepSec * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            commandLine = new CommandLine("kubectl");
            commandLine.addArguments(new String[]{"get", "pods"}, false);
        } else {
            commandLine = new CommandLine("sleep");
            commandLine.addArguments(new String[]{String.valueOf(sleepSec)}, false);
        }
        return commandLine;
    }

    private Map<String, String> getEnvironment() {
        return System.getenv();
    }

    private Executor buildExecutor(ExecuteStreamHandler streamHandler) {
        Executor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);
        executor.setWatchdog(new ExecuteWatchdog(60 * 1000));
        executor.setExitValues(null);
        return executor;
    }

    private void execCfLike() {
        InputStream is = this.getClass().getResourceAsStream("/test.txt");
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
            String s = textBuilder.toString();
            Thread.sleep(sleepSec * 1000L);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
