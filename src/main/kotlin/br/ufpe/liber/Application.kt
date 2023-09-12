@file:Suppress("FILE_NAME_MATCH_CLASS")

package br.ufpe.liber

import io.micronaut.context.annotation.Factory
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import io.micronaut.core.io.ResourceResolver
import io.micronaut.runtime.Micronaut.run
import jakarta.inject.Singleton
import java.util.*

object Application {
    @JvmStatic
    @Suppress("detekt:SpreadOperator")
    fun main(args: Array<String>) {
        run(*args)
    }
}

@Factory
internal class TemplatesFactory {
    @Singleton
    fun createTemplate(): Templates = StaticTemplates()
}

@Singleton
class GitPropertiesLoader(private val resourceResolver: ResourceResolver) : ApplicationEventListener<StartupEvent> {
    override fun onApplicationEvent(event: StartupEvent) {
        val properties = Properties()
        resourceResolver.getResourceAsStream("classpath:git.properties").ifPresent {
            properties.load(it)
        }

        GitProperties.value = GitProperties(
            branch = properties.getProperty("git.branch"),
            commitId = properties.getProperty("git.commit.id"),
            commitTime = properties.getProperty("git.commit.time"),
            totalCommitCount = properties.getProperty("git.total.commit.count").toInt(),
        )
    }
}

data class GitProperties(
    val branch: String,
    val commitId: String,
    val commitTime: String,
    val totalCommitCount: Int,
) {
    companion object {
        // DeepSouce flags this as bad practice:
        // https://app.deepsource.com/directory/analyzers/kotlin/issues/KT-W1047
        // But this use is intentional since we know Micronaut will initialize it
        // before usage (considering we have the ApplicationEventListener above).
        // Therefore, we add a skip to this occurrence.
        lateinit var value: GitProperties // skipcq: KT-W1047
    }
}
