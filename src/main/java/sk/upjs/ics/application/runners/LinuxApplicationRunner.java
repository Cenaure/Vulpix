package sk.upjs.ics.application.runners;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

public class LinuxApplicationRunner implements IApplicationRunner {

    @Override
    public void run(Path applicationPath, Consumer<String> logConsumer) throws RunnerException {

        if (!Files.exists(applicationPath))
            throw new RunnerException(RunnerExceptionCodes.APPLICATION_PATH_INVALID, applicationPath);

        try {
            if (isWindowsExecutable(applicationPath)) {
                System.out.println("Windows executable");

                int exitCode = this.umuRun(applicationPath, Path.of("./umu-run.log"), logConsumer);
                System.out.println("Exit code: " + exitCode);

            } else {
                System.out.println("Not a Windows executable");
                // TODO: native run
            }
        } catch (IOException e) {
            throw new RunnerException(RunnerExceptionCodes.APPLICATION_PATH_INVALID, e, applicationPath);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RunnerException(RunnerExceptionCodes.APPLICATION_APPLICATION_INTERRUPTED, e);
        }
    }

    /**
     * Executes application using the "umu-run" command, writes output to a log file, and invokes consumer
     */
    private int umuRun(Path applicationPath, Path logPath, Consumer<String> logConsumer) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder()
                .command("umu-run", applicationPath.toString());

        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
            BufferedWriter logFile = Files.newBufferedWriter(logPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                logFile.write(line);
                logFile.newLine();
                logConsumer.accept(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // returns exit code
        return process.waitFor();
    }

    private boolean isWindowsExecutable(Path filePath) {
        byte[] bytes;

        try (InputStream inputStream = Files.newInputStream(filePath);) {
            bytes = inputStream.readNBytes(2);
        } catch (IOException e) {
            throw new RunnerException(RunnerExceptionCodes.APPLICATION_PATH_INVALID, e, filePath);
        }

        // Windows executable starts with 'MZ'
        // https://en.wikipedia.org/wiki/DOS_MZ_executable
        return bytes.length == 2 && bytes[0] == 'M' && bytes[1] == 'Z';
    }
}
