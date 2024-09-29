<!-- markdownlint-disable MD013 -->

# Project Starter

![CI Workflow](https://github.com/Liber-UFPE/project-starter/actions/workflows/build.yml/badge.svg?branch=main)

Esse é um template para iniciar projetos. Há algumas coisas que você precisa fazer após criar seu repositório usando este template:

- [ ] Substituir `project-starter`, `PROJECT_STARTER` (e outras menções) pelo nome do seu projeto
- [ ] Editar `src/main/resources/public/stylesheets/main.css` conforme necessário (cores diferentes, fontes, etc.)
- [ ] Editar `src/main/resources/public/javascript/main.js` conforme necessário
- [ ] Editar `src/main/jte/layout.kte` conforme necessário para suportar a navegação do seu projeto

## Adicionando uma nova página

Para adicionar uma nova página, você precisa editar alguns arquivos:

### 1. View

Adicione um novo template como `src/main/jte/my-new-page.kte` que usa o layout do projeto:

```html
@template.layout(title = "Página", content = @`
    <section class="main-section-bg">
        <div class="px-4 pt-8 mx-auto max-w-screen-xl z-10 relative md:pt-16 text-center">
            <h1 class="mb-4 leading-none text-6xl text-gray-800 tracking-tight text-white md:text-8xl lg:text-8xl">Headline para a página</h1>
        </div>
    </section>
    @template.partials.content-section(content = @`
        <article class="format lg:format-lg">
            <p class="mb-3 text-3xl text-gray-800">Texto do primeiro paragrafo</p>
            <p class="text-gray-800 leading-relaxed mb-4">Texto do segundo paragrafo</p>
        </article>
    `)
`, metadata = @`
    <meta name="description" content="Meta descrição da página">
`)
```

### 2. Controller / Route

Como `src/main/kotlin/br/ufpe/liber/controllers/IndexController.kt`, ou outro se necessário:

```kotlin
@Get("/my-new-page")
fun index() = ok(templates.myNewPage()) // `myNewPage` é gerado automaticamente
```

### 3. Mudanças no layout

Se isso adiciona à navegação do seu projeto, você pode adicionar novos links à `navbar` no arquivo `src/main/jte/layout.kte`:

```diff
 <ul class="flex flex-col p-4 mt-4 font-medium md:space-x-8 md:flex-row md:mt-0">
     <li>
         @template.partials.navlink(path = "/", text = "Inicio", title = "Página inicial")
     </li>
+    <li>
+        @template.partials.navlink(path = "#", text = "Link #1", title = "Link #1")
+    </li>
 </ul>
```

### 4. Testes

Adicione alguns testes para a sua nova página / rota.

> [!TIP]
> Se sua rota:
> 
> - Não recebe parâmetros
> - Retorna HTML
> 
> Então ela será testada automaticamente pelos testes de acessibilidade.


## Executar localmente

Para executar o projeto localmente, abra um terminal e execute:

```shell
./gradlew run
```

A aplicação ficará acessível em <http://localhost:8080/>.

Se você quiser recarregar a aplicação a cada alteração de código, execute [o Gradle em modo contínuo](https://docs.micronaut.io/latest/guide/index.html#gradleReload):

```shell
./gradlew run -t
```

## Simular ambiente de produção

O aplicativo é executado usando o [nginx](https://nginx.org/) como proxy, em uma máquina com o [Rocky Linux](https://rockylinux.org/). Para simular este ambiente, você pode usar o [Vagrant][vagrant], que irá configurar todos os detalhes usando um único comando:

```shell
./gradlew clean vagrantUp
```

O servidor nginx ficará acessível em <http://localhost:9080/>, e a aplicação em <http://localhost:9080/project-starter>.

Para acessar a VM via SSH, execute:

```shell
vagrant ssh
```

### Destruir / Reiniciar a VM Vagrant

Se a VM estiver executando, execute o seguinte comando para destruí-lo:

```shell
vagrant destroy --graceful --force
```

## Requisitos

1. Java 21 (mais fácil de instalar com [SDKMAN](https://sdkman.io/))
2. [Node.js 20](https://nodejs.org/en)
3. [Docker Desktop](https://www.docker.com/products/docker-desktop/) (se você quiser testar as imagens Docker)
4. [Ktlint CLI][ktlint-cli] (se você quiser executar inspeções de código localmente)
5. [Gradle](https://gradle.org/install/#with-a-package-manager) (se você não quiser usar o script `./gradlew`)
6. [Vagrant][vagrant] (se você quiser rodar o projeto usando uma VM)


## Aspectos técnicos

O projeto é desenvolvido usando:

- [Micronaut Framework][micronaut]
- [Gradle][gradle]
- [Kotlin][kotlin]
- [Tailwind CSS][tailwind]

### Documentação de Micronaut

- [Guia do usuário](https://docs.micronaut.io/latest/guide/index.html)
- [API Referência](https://docs.micronaut.io/latest/api/index.html)
- [Referência de Configuração](https://docs.micronaut.io/latest/guide/configurationreference.html)
- [Guias sobre o Micronaut](https://guides.micronaut.io/index.html)

### Template Engine

O projeto usa JTE / KTE como template engine.

- [Documentação do JTE](https://jte.gg)
- [Tutorial JTE](https://javalin.io/tutorials/jte)

### CI & CD

O projeto usa [GitHub Actions](https://docs.github.com/en/actions) para executar testes e outras validações descritas abaixo.

#### Inspeções de código

Para cada merge/push, e também para pull requests, existem ações do GitHub para executar [ktlint][ktlint], [detekt](https://github.com/detekt), e [DiKTat](https://github.com/saveourtool/diktat) (experimental).

O ktlint está configurado para usar o estilo de código `intellij_idea` para que ele não entre em conflito com a ação de formatação de código da IntelliJ IDEA.

Há também uma integração com o Sonar Cloud: <https://sonarcloud.io/project/overview?id=Liber-UFPE_project-starter>.

### Testes e Cobertura de Código

Usamos [Kotest](https://kotest.io/) como framework de teste, e [Kover](https://github.com/Kotlin/kotlinx-kover) como a ferramenta de cobertura de código. Ver também [Micronaut Kotest integrações docs](https://micronaut-projects.github.io/micronaut-test/latest/guide/index.html#kotest5).

> [!TIP]
> Veja a cobertura de código mais recente na [página do projeto no SonarCloud](https://sonarcloud.io/component_measures?metric=coverage&view=list&id=Liber-UFPE_project-starter).

### Assets Pipeline

Para garantir que as páginas carreguem rapidamente, há um processamento dos assets estáticos (JavaScripts, CSS, imagens).
O [esbuild](https://esbuild.github.io/) é usado em conjunto com alguns pacotes npm:

- [sharp](https://github.com/lovell/sharp) para gerar versões `webp` das images
- [gzipper](https://github.com/gios/gzipper) para gerar versões comprimidas (`gzip`, `brotli`, `deflate`)
- [postcss](https://postcss.org/) para otimizar o uso do [Tailwind CSS][tailwind] e manter apenas os estilos efetivamente usados.

Esse processamento é então integrado ao `build` principal da aplicação usando o [Gradle Plugin for Node](https://github.com/node-gradle/gradle-node-plugin).

> [!TIP]
> Dá para testar o processamento dos assets de maneira isolada executando diretamente `node assets-pipeline.mjs`.

### Layout de Diretório de Projetos

O projeto segue o padrão [Maven Standard Directory Layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html) para projetos Kotlin. As pastas principais são:

| Diretório                   | Descrição                                          |
|:----------------------------|:---------------------------------------------------|
| `src/main`                  | Pasta raiz para código de aplicação                |
| `src/main/jte`              | Pasta de templates JTE                             |
| `src/main/kotlin`           | Código Kotlin da aplicação                         |
| `src/main/resources`        | Configurações e outros recursos                    |
| `src/main/resources/public` | Web assets como imagens, javascript e arquivos css |
| `src/test`                  | Pasta raiz para código de teste                    |
| `scripts`                   | Pasta com scripts para deploy usando o Vagrant     |
| `github`                    | Pasta raiz para configurações do GitHub            |
| `.github/workflows`         | GitHub Ações configuração                          |

[gradle]: https://gradle.org/
[kotlin]: https://kotlinlang.org/
[micronaut]: https://micronaut.io/
[tailwind]: https://tailwindcss.com/
[vagrant]: https://www.vagrantup.com/
[ktlint]: https://github.com/pinterest/ktlint
[ktlint-cli]: https://pinterest.github.io/ktlint/latest/install/cli/
