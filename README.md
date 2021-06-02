# thread-stress

Small java application to stress testing thread manipulation.

The application continuously runs the shell command `sleep <sec>` in background threads, as well as continuously reads a file loaded from the classpath.

If the argument `-k` is passed, it will run the command `kubectl get pods` instead of `sleep <sec>`.

### Environment variables

|Name|Description|Default|
|----|-----------|-------|
|RATE_MS|How often to keep submitting tasks to be run in background threads (milliseconds)|50|
|SLEEP_SEC|Minimum execution delay in seconds of background threads|1|

With default values in place, the program has a constant amount of ~150 live threads, and a rate of ~3000 threads created and finished per minute.


### Building

1. Create the jar file for the java application

    ```shell
   ./gradlew build
   ```

1. Create the Docker image

   ```shell
   docker build -t <tag> .
   ```
