package net.kaleidos.redmine.migrator

import net.kaleidos.redmine.MigratorToTuesmonSpecBase
import net.kaleidos.redmine.RedmineClient
import net.kaleidos.redmine.RedmineTuesmonRef
import net.kaleidos.redmine.testdata.ProjectDataProvider
import net.kaleidos.redmine.testdata.VersionDataProvider

class VersionMigratorSpec extends MigratorToTuesmonSpecBase {

    @Delegate
    ProjectDataProvider projectDataProvider = new ProjectDataProvider()
    @Delegate
    VersionDataProvider versionDataProvider = new VersionDataProvider()

    void setup() {
        deleteTuesmonProjects()
    }

    void 'migrate versions of one project'() {
        given: 'a mocked redmine client'
            RedmineClient mockedClient = Stub(RedmineClient) {
                findAllVersionByProjectId(_) >> buildRedmineVersionList()
            }

        and: 'a tuesmon client'
            def tuesmonClient = createTuesmonClient()

        and: 'a redmine project to migrate its versions'
            ProjectMigrator projectMigrator = new ProjectMigrator(mockedClient, tuesmonClient)
            RedmineTuesmonRef migratedProjectInfo = projectMigrator.migrateProject(buildRedmineProject())

        and: 'building a version migrator instance'
            VersionMigrator versionMigrator = new VersionMigrator(mockedClient, tuesmonClient)

        when: 'migration the versions of the project'
            def tuesmonMilestones = versionMigrator.migrateVersionsFromProject(migratedProjectInfo)

        then: 'the milestones are created in tuesmon'
            tuesmonMilestones.size() > 0
            tuesmonMilestones.every(basicData)
    }

    Closure<Boolean> basicData = {
        it.project &&
        it.name &&
        it.isClosed != null &&
        it.startDate &&
        it.endDate
    }
}