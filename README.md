# Deciderator App

For when you can't decide.

### Server

See [server/README.md](https://github.com/rikbrown/deciderator/blob/3.0/server/README.md)


### Local testing

```
ng serve
```

### Publish

*Note: Github Actions setup to automatically publish, so shouldn't be necessary*

```
docker build -f Dockerfile-prod-nossl -t rikbrown/deciderator-app .
docker push rikbrown/deciderator-app:3.0
```

`Dockerfile-prod-nossl` runs a basic HTTP web server, typically expected to run behind a proxy e.g. letsencrypt.
