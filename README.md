# document-intelligence-api

Proof of concept for a document intelligence system

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:8997/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

