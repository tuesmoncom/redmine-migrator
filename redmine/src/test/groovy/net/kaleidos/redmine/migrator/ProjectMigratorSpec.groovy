package net.kaleidos.redmine.migrator

import spock.lang.Specification

import net.kaleidos.redmine.RedmineTuesmonRef
import net.kaleidos.redmine.MigratorToTuesmonSpecBase
import net.kaleidos.redmine.testdata.ProjectDataProvider

import net.kaleidos.tuesmon.TuesmonClient
import net.kaleidos.redmine.RedmineClient

class ProjectMigratorSpec extends MigratorToTuesmonSpecBase {

    @Delegate
    ProjectDataProvider projectDataProvider = new ProjectDataProvider()

    void setup() {
        deleteTuesmonProjects()
    }

    void 'migrating one project'() {
        given: 'a mocked redmine client'
            RedmineClient mockedClient = Stub(RedmineClient) {
                findAllMembershipByProjectIdentifier(_) >> buildRedmineMembershipList()
                findUserFullById(_) >> { Integer id -> buildRedmineUser("${randomTime}") }
                findAllTracker() >> buildRedmineTrackerList()
                findAllIssueStatus() >> buildRedmineStatusList()
                findAllIssuePriority() >> buildRedmineIssuePriorityList()
            }
        and: 'building a simple migrator instance'
            ProjectMigrator migrator =
                new ProjectMigrator(mockedClient, createTuesmonClient())
        when: 'trying to migrate basic estructure of a redmine project'
            RedmineTuesmonRef migratedProjectInfo = migrator.migrateProject(buildRedmineProject())
        then: 'checking the object'
            migratedProjectInfo.redmineId
            migratedProjectInfo.redmineIdentifier
            with(migratedProjectInfo.project) {
                name
                description
                roles.size() == 3
                memberships.size() == 4 // 3 + admin
                issueTypes.unique().size() == 5
                issueStatuses.unique().size() == 5
                issuePriorities.unique().size() == 5
                issueSeverities.unique().size() == 1
            }
        and: 'checking all membership user data (admin included)'
            with(migratedProjectInfo.project) {
                memberships.every { m -> m.email }
                memberships.every { m -> m.role }
            }
    }

}

