# Axelix Console UI

The npm workspace at the repository root contains the shared React application
in `master/front-end` and its OSS entry point in `master-oss/front-end`.
With the Enterprise submodule checked out, it also includes the application
in `axelix-enterprise/front-end`.

Run these commands from the repository root.

```sh
npm ci
npm run dev -w axelix-oss
```

Use `-w axelix-enterprise` to start the Enterprise console. Both applications
use the shared Vite configuration and read the development proxy target from
`master/front-end/.env.local` (`VITE_LOCAL_API_URL`).

Run `npm run check -w @axelix/console` to check the shared code and
`npm run build -w axelix-oss` or `npm run build -w axelix-enterprise` to
type-check and bundle an edition. Each edition writes its own `dist/`.
Gradle packages that directory under `spa/` in the corresponding Master JAR.

The OSS entry point calls `mountConsole()`. Enterprise passes its license
details component and expiry alert to `mountConsole`. Both render inside
the common application's Redux, translation and Ant Design providers.
