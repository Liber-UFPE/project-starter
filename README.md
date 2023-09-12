# Project Starter

![CI Workflow](https://github.com/Liber-UFPE/project-starter/actions/workflows/build.yml/badge.svg?branch=main)
![Main Workflow](https://github.com/Liber-UFPE/project-starter/actions/workflows/main.yml/badge.svg?branch=main)
![Deploy Workflow](https://github.com/Liber-UFPE/project-starter/actions/workflows/deploy.yml/badge.svg?branch=main)

This is a project starter template. There are a few things you need to do after creating your repository using this template:

- [ ] Replace `project-starter` with your project's name
- [ ] Edit `src/main/resources/public/stylesheets/main.css` as needed (different colors, fonts, etc.)
- [ ] Edit `src/main/jte/layout.kte` as necessary to support your project's navigation

## Adding a new page

To add a new page, you need to edit a few files:

### 1. View

Add a new template such as `src/main/jte/my-new-page.kte` that uses the project layout:

```html
@template.layout(
    title = "Page title",
    content = @`
        @template.sections.top(
        title = "Main top section title",
        subtext = @`
        Main top section content. It may be just regular <abbr title="HyperText Markup Language">HTML</abbr>.
        `
        )
        @template.sections.main(
            content = @`
            <div class="row mb-2">
                <p>Secondary section HTML</p>
            </div>
            `
        )
    `
)
```

### 2. Controller / Route

Such as `src/main/kotlin/br/ufpe/liber/controllers/IndexController.kt`, or another one if necessary:

```kotlin
@Get("/my-new-page")
fun index() = ok(templates.myNewPage()) // `myNewPage` is generated automatically
```

### 3. Layout changes

If this adds to your project's navigation, you can new links to the `navbar` in the `src/main/jte/layout.kte` file:

```diff
        <li class="nav-item">
            <a class="nav-link btn btn-outline-success" href="/" role="button">Index</a>
        </li>
+       <li class="nav-item">
+           <a class="nav-link btn btn-outline-success" href="/my-new-page" role="button">My New Page</a>
+       </li>
    </ul>
    <form class="d-flex" role="search">
        <input class="form-control me-2" type="search" placeholder="Busca" aria-label="Search">

```

### 4. Tests

Add some tests for your new page / route.

## Run locally

To run the project locally, open a terminal and execute:

```shell
./gradlew run
```

If you want to reload the application for every code change, run [Gradle in _continuous_ mode](https://docs.micronaut.io/latest/guide/#gradleReload):

```shell
./gradlew run -t
```

This won't reload the application when there is a change in the templates, though. To do so, you need to open two terminal windows and run these two commands on each one of them:

```shell
# Terminal 1:
# Will compile the templates for every change
./gradlew generateJte -t
```

```shell
# Terminal 2:
# Will restart the application for every change, including the templates
./gradlew run -t
```

## Requirements

1. Java 17+ (easier to install with [SDKMAN](https://sdkman.io/))
2. [Docker Desktop](https://www.docker.com/products/docker-desktop/) (if you want to test docker images)
3. [Ktlint CLI](https://pinterest.github.io/ktlint/1.0.0/install/cli/) (if you want to run code inspections locally)
4. [Gradle](https://gradle.org/install/#with-a-package-manager) (if you don't want to use the `./gradlew` script)

## Technical aspects

The project is developed using Micronaut Framework, [Gradle](https://gradle.org/), and [Kotlin](https://kotlinlang.org/).

### Micronaut Documentation

- [User Guide](https://docs.micronaut.io/4.1.3/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.1.3/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.1.3/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)

### Template Engine

It uses JTE/KTE as the template engine.

- [JTE Website](https://jte.gg/)
- [JTE Documentation](https://github.com/casid/jte/blob/main/DOCUMENTATION.md)
- [JTE Tutorial](https://javalin.io/tutorials/jte)

### CI & CD

The project uses [GitHub Actions](https://docs.github.com/en/actions) to run tests, package a new version, and deploy it to [Fly.io](https://fly.io/) (experimental). A Docker image is build for every merge/push made to `main` branch.

### Tests & Code Coverage

We use [Kotest](https://kotest.io/) as the test framework, and [Kover](https://github.com/Kotlin/kotlinx-kover) as the Code Coverage tool. See also [Micronaut Kotest integration docs].(https://micronaut-projects.github.io/micronaut-test/latest/guide/#kotest5)

### Code Inspections

For every merge/push, and also for pull requests, there are GitHub Actions to run [ktlint](https://github.com/pinterest/ktlint) and [detekt](https://github.com/detekt/detekt). There is also an (experimental) integration with [DeepSource](https://deepsource.com/). 

Ktlint is configured to use `intellij_idea` code style so that it won't conflict with code formatting action in IDEA.

### Project Directory Layout

Project follow the default [Maven Standard Directory Layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html) for Kotlin projects. The main folders are:

| Directory                   | Description                                          |
|:----------------------------|:-----------------------------------------------------|
| `src/main`                  | Root folder for application code                     |
| `src/main/jte`              | JTE template folder                                  |
| `src/main/kotlin`           | Application Kotlin code                              |
| `src/main/resources`        | Configurations and other resources                   |
| `src/main/resources/public` | Web assets such as images, javascript, and css files |
| `src/test`                  | Root folder for test code                            |
| `.github`                   | Root folder for GitHub configurations                |
| `.github/workflows`         | GitHub Actions configuration                         |
