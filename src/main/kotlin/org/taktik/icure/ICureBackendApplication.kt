/*
 * Copyright (C) 2018 Taktik SA
 *
 * This file is part of iCureBackend.
 *
 * iCureBackend is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * iCureBackend is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with iCureBackend.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.taktik.icure

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.JndiDataSourceAutoConfiguration
import org.springframework.boot.web.servlet.ServletContextInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.TaskScheduler
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.taktik.icure.dao.GenericDAO
import org.taktik.icure.dao.migration.DbMigration
import org.taktik.icure.entities.embed.AddressType
import org.taktik.icure.entities.embed.Confidentiality
import org.taktik.icure.entities.embed.DocumentStatus
import org.taktik.icure.entities.embed.DocumentType
import org.taktik.icure.entities.embed.Gender
import org.taktik.icure.entities.embed.InsuranceStatus
import org.taktik.icure.entities.embed.PartnershipStatus
import org.taktik.icure.entities.embed.PartnershipType
import org.taktik.icure.entities.embed.PaymentType
import org.taktik.icure.entities.embed.PersonalStatus
import org.taktik.icure.entities.embed.TelecomType
import org.taktik.icure.entities.embed.Visibility
import org.taktik.icure.logic.CodeLogic
import org.taktik.icure.logic.ICureLogic
import org.taktik.icure.logic.PropertyLogic
import org.taktik.icure.logic.ReplicationLogic
import org.taktik.icure.services.external.http.WebSocketServlet

@SpringBootApplication(scanBasePackages = [
    "org.springframework.boot.autoconfigure.aop",
    "org.springframework.boot.autoconfigure.cache",
    "org.springframework.boot.autoconfigure.context",
    "org.springframework.boot.autoconfigure.dao",
    "org.springframework.boot.autoconfigure.jackson",
    "org.springframework.boot.autoconfigure.jdbc",
    "org.springframework.boot.autoconfigure.jersey",
    "org.springframework.boot.autoconfigure.transaction",
    "org.springframework.boot.autoconfigure.validation",
    "org.springframework.boot.autoconfigure.websocket",
    "org.taktik.icure.config",
    "org.taktik.icure.dao",
    "org.taktik.icure.logic",
    "org.taktik.icure.be.ehealth.logic",
    "org.taktik.icure.be.drugs.dao",
    "org.taktik.icure.be.drugs.logic",
    "org.taktik.icure.be.format.logic",
    "org.taktik.icure.be.samv2.logic",
    "org.taktik.icure.properties",
    "org.taktik.icure.services"
], exclude = [
    FreeMarkerAutoConfiguration::class,
    CacheAutoConfiguration::class,
    DataSourceAutoConfiguration::class,
    JndiDataSourceAutoConfiguration::class
])
@EnableWebSecurity
@PropertySource("classpath:icure-default.properties")
class ICureBackendApplication {
    private val log = LoggerFactory.getLogger(this.javaClass)


    @Bean
    fun initializer(webSocketServlet: WebSocketServlet) = ServletContextInitializer {
        val webSocketServletReg = it.addServlet("webSocketServlet", webSocketServlet)
        webSocketServletReg.setLoadOnStartup(1);
        webSocketServletReg.addMapping("/ws/*")
    }

    @Bean
    fun performStartupTasks(@Qualifier("threadPoolTaskExecutor") taskExecutor: TaskExecutor, taskScheduler: TaskScheduler, iCureLogic: ICureLogic, codeLogic: CodeLogic, propertyLogic: PropertyLogic, replicationLogic:ReplicationLogic, allDaos: List<GenericDAO<*>>, migrations: List<DbMigration>) = ApplicationRunner {
        //Check that core types have corresponding codes
        log.info("icure (" + iCureLogic.version + ") is initialised")

        taskExecutor.execute {
            listOf(AddressType::class.java, DocumentType::class.java, DocumentStatus::class.java,
                   Gender::class.java, InsuranceStatus::class.java, PartnershipStatus::class.java, PartnershipType::class.java, PaymentType::class.java,
                   PersonalStatus::class.java, TelecomType::class.java, Confidentiality::class.java, Visibility::class.java).forEach({ codeLogic.importCodesFromEnum(it) })
        }

        taskExecutor.execute {
            val resolver = PathMatchingResourcePatternResolver(javaClass.classLoader);
            resolver.getResources("classpath*:/org/taktik/icure/db/codes/**.xml").forEach {
                val md5 = it.filename.replace(Regex(".+\\.([0-9a-f]{20}[0-9a-f]+)\\.xml"), "$1")
                codeLogic.importCodesFromXml(md5, it.filename.replace(Regex("(.+)\\.[0-9a-f]{20}[0-9a-f]+\\.xml"), "$1"), it.inputStream)
            }
        }


        //Execute migrations sequentially
        taskExecutor.execute {
            migrations.forEach { dbMigration ->
                try {
                    if (!dbMigration.hasBeenApplied()) {
                        dbMigration.apply()
                    }
                } catch (e: Exception) {
                    log.error("Could not perform dbMigration " + dbMigration.javaClass.getName(), e)
                }
            }
        }

        //Schedule background tasks (plugins) + replication + index refresh
        taskScheduler.scheduleAtFixedRate({ replicationLogic.startReplications() }, 60_000L)
        taskScheduler.scheduleAtFixedRate({ allDaos.forEach { it.refreshIndex() } }, 240_000L)

        log.info("icure (" + iCureLogic.version + ") is started")
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(ICureBackendApplication::class.java, *args)
}
