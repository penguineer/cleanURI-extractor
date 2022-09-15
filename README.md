# cleanURI - Extractor

> Extract website information for the cleanURI service.


## Configuration

Configuration is done using environment variables:

* `PORT`: Port for the HTTP endpoint (default `8080`, only change when running locally!)
* `AMQP_HOST`: RabbitMQ host
* `AMQP_USER`: RabbitMQ user
* `AMQP_PASS`: RabbitMQ password
* `AMQP_VHOST`: RabbitMQ virtual host, defaults to '/'
* `EXTRACTION_TASK_QUEUE`: AMQP queue (inbound) for receiving extraction tasks from the [Canonizer](https://github.com/penguineer/cleanURI-canonizer)

This handler uses the [`reply-to` header](https://www.rabbitmq.com/direct-reply-to.html) for result message binding and therefore has no outbound routing key in its configuration.

## Deployment

### Run with Docker

With the configuration stored in a file `.env`, the service can be run as follows:

```bash
docker run --rm \
           --env-file .env \
           mrtux/cleanrui-extractor
```

The service does not store any state and therefore needs no mount points or other persistence.

Please make sure to pin the container to a specific version in a production environment.

### Development

This project uses the [Micronaut Framework](https://micronaut.io/).

Version numbers are determined with [jgitver](https://jgitver.github.io/).
Please check your [IDE settings](https://jgitver.github.io/#_ides_usage) to avoid problems, as there are still some unresolved issues.
If you encounter a project version `0` there is an issue with the jgitver generator.

For local execution the configuration can be provided in a `.env` file and made available using `dotenv`:
```bash
dotenv ./mvnw mn:run
```

Note that `.env` is part of the `.gitignore` and can be safely stored in the local working copy.

### Dependencies

The project depends on [cleanURI-common](https://github.com/penguineer/cleanURI-common) with a Maven artifact that is currently hosted as a GitHub package.
Please refer to the [README from cleanURI-common](https://github.com/penguineer/cleanURI-common/blob/main/README.md) on how to resolve the dependency locally.

The cleanURI-common dependency is resolved by [jitpack](https://jitpack.io/).
Since these dependencies are built on-demand it may take a moment to download.


## Build

The build is split into two stages:
1. Packaging with [Maven](https://maven.apache.org/)
2. Building the Docker container

This means that the [Dockerfile](Dockerfile) expects one (and only one) JAR file in the target directory.
Build as follows:

```bash
mvn --batch-mode --update-snapshots clean package
docker build .
```

Why not do everything with maven and [JIB](https://github.com/GoogleContainerTools/jib)?
So far I have not been able to integrate JIB with the mechanism that determined which tags should be build (e.g. only
build *latest* when on main branch). After 5h of trying I settled with this solution:
* [Maven](https://maven.apache.org/) is sufficiently reliable to create reproducible builds, and we can make use of the build cache.
* The [Dockerfile](Dockerfile) allows for the usual integration into image build and push.

The whole process is coded in the [docker-publish workflow](.github/workflows/docker-publish.yml) and only needs to be
executed manually for local builds.


## Maintainers

* Stefan Haun ([@penguineer](https://github.com/penguineer))


## Contributing

PRs are welcome!

If possible, please stick to the following guidelines:

* Keep PRs reasonably small and their scope limited to a feature or module within the code.
* If a large change is planned, it is best to open a feature request issue first, then link subsequent PRs to this issue, so that the PRs move the code towards the intended feature.


## License

[MIT](LICENSE.txt) © 2022 Stefan Haun and contributors
