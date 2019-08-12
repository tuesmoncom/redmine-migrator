package net.kaleidos.redmine

import groovy.util.logging.Log4j
import net.kaleidos.domain.Project
import net.kaleidos.tuesmon.TuesmonClient
import spock.lang.Specification

@Log4j
class MigratorToTuesmonSpecBase extends Specification {

    TuesmonClient createTuesmonClient() {
        return createTuesmonClientBase()
    }

    TuesmonClient createTuesmonAdminClient() {
        return createTuesmonClientBase("admin")
    }

    TuesmonClient createTuesmonClientBase(String specialUser = "") {
        def config =
            new ConfigSlurper()
                .parse(new File("src/test/resources/tuesmon${specialUser ? '_' + specialUser : ''}.groovy").text)
        def client = new TuesmonClient(config.host)

        return client.authenticate(config.user, config.passwd)
    }

    void deleteTuesmonProjects() {
        TuesmonClient tuesmonClient = createTuesmonAdminClient()

        tuesmonClient.with {
            projects.each { p ->
                log.debug "Deleting project '${p.name}' with id ${p.id}"
                deleteProject(new Project(id: p.id))
            }
        }
    }
}