package net.kaleidos.redmine.migrator

import spock.lang.Specification

import net.kaleidos.redmine.testdata.IssueDataProvider
import net.kaleidos.redmine.testdata.ProjectDataProvider

import net.kaleidos.redmine.RedmineTuesmonRef
import net.kaleidos.redmine.MigratorToTuesmonSpecBase
import net.kaleidos.redmine.RedmineFileDownloader
import net.kaleidos.redmine.testdata.ProjectDataProvider

import net.kaleidos.tuesmon.TuesmonClient
import net.kaleidos.redmine.RedmineClient

import net.kaleidos.domain.Issue as TuesmonIssue
import net.kaleidos.domain.History as TuesmonHistory
import net.kaleidos.domain.Attachment as TuesmonAttachment
import com.taskadapter.redmineapi.bean.Attachment as RedmineAttachment

class IssueMigratorSpec extends MigratorToTuesmonSpecBase {

    @Delegate ProjectDataProvider projectDataProvider = new ProjectDataProvider()
    @Delegate IssueDataProvider issueDataProvider = new IssueDataProvider()

    void setup() {
        deleteTuesmonProjects()
    }

    void 'migrating project issues'() {
        given: 'a mocked redmine client'
            RedmineClient redmineClient = buildRedmineClientToCreateProject()
            TuesmonClient tuesmonClient = createTuesmonClient()
        and: 'building a migrator instances'
            ProjectMigrator projectMigrator = new ProjectMigrator(redmineClient, tuesmonClient)
            IssueMigrator issueMigrator = new IssueMigrator(redmineClient, tuesmonClient)
        when: 'migrating a given project'
            RedmineTuesmonRef migratedProjectInfo = projectMigrator.migrateProject(buildRedmineProject())
        and: 'trying to migrate basic the estructure of related issues'
            List<TuesmonIssue> migratedIssues = issueMigrator.migrateIssuesByProject(migratedProjectInfo)
            TuesmonIssue firstTuesmonIssue = migratedIssues.first()
            TuesmonAttachment firstIssueAttachment = firstTuesmonIssue.attachments.first()
            TuesmonHistory firstIssueHistory = firstTuesmonIssue.history.first()
        then: 'checking basic request data'
            migratedIssues.size() > 0
            migratedIssues.every(basicData)
        and: 'checking first issue data in detail'
            firstTuesmonIssue.type == 'tracker-1'
            firstTuesmonIssue.status == 'status-1'
            firstTuesmonIssue.priority == 'priority-1'
            firstTuesmonIssue.severity == 'Normal'
            firstTuesmonIssue.subject
            firstTuesmonIssue.subject.contains('Integration')
            firstTuesmonIssue.description
            firstTuesmonIssue.owner
            firstTuesmonIssue.assignedTo
            firstTuesmonIssue.createdDate
        and: 'checking attachments'
            firstIssueAttachment.data
            firstIssueAttachment.name == "debian.jpg"
            firstIssueAttachment.owner
            firstIssueAttachment.description == "simple image"
            firstIssueAttachment.createdDate
        and: 'checking history'
            firstIssueHistory.user
            firstIssueHistory.createdAt
            firstIssueHistory.comment
            firstIssueHistory.comment.contains("some comment")
    }

    RedmineClient buildRedmineClientToCreateProject() {
        return Stub(RedmineClient) {
            findAllMembershipByProjectIdentifier(_) >> buildRedmineMembershipList()
            findUserFullById(_) >> { Integer id -> buildRedmineUser("${randomTime}") }
            findAllTracker() >> buildRedmineTrackerList()
            findAllIssueStatus() >> buildRedmineStatusList()
            findAllIssuePriority() >> buildRedmineIssuePriorityList()
            findAllIssueByProjectIdentifier(_) >> buildRedmineIssueList()
            findIssueById(_) >> { Integer index ->
                def redmineIssue = buildRedmineIssueWithIndex(index)
                redmineIssue.attachments = buildAttachmentList()
                redmineIssue.journals = buildHistoryList()
                redmineIssue
            }
            downloadAttachment(_) >> { RedmineAttachment att ->
                return new RedmineFileDownloader("http://lala","apiKey002ifj03ifj")
                    .download(att.contentURL)
            }
        }
    }

    void 'migrating project issues with no author or assignee'() {
        given: 'a mocked redmine client'
            RedmineClient redmineClient = buildRedmineClientWithIssuesWithoutOwners()
            TuesmonClient tuesmonClient = createTuesmonClient()
        and: 'building a migrator instances'
            ProjectMigrator projectMigrator = new ProjectMigrator(redmineClient, tuesmonClient)
            IssueMigrator issueMigrator = new IssueMigrator(redmineClient, tuesmonClient)
        when: 'migrating a given project'
            RedmineTuesmonRef migratedProjectInfo = projectMigrator.migrateProject(buildRedmineProject())
        and: 'trying to migrate basic the estructure of related issues'
            List<TuesmonIssue> migratedIssues = issueMigrator.migrateIssuesByProject(migratedProjectInfo)
            TuesmonIssue firstTuesmonIssue = migratedIssues.first()
            TuesmonAttachment firstIssueAttachment = firstTuesmonIssue.attachments.first()
            TuesmonHistory firstIssueHistory = firstTuesmonIssue.history.first()
        then: 'checking basic request data'
            migratedIssues.size() > 0
            migratedIssues.every(basicData)
    }

    RedmineClient buildRedmineClientWithIssuesWithoutOwners() {
        // Reusing general stub
        def stub = buildRedmineClientToCreateProject()

        // Modifying only how we're getting issues
        stub.findAllIssueByProjectIdentifier(_) >> {
            buildRedmineIssueList().collect {
                it.author= null
                it.assignee = null
                it
            }
        }

        return stub
    }

    void 'migrating project issue history only with comments'() {
        given: 'a mocked redmine client'
            RedmineClient redmineClient = buildRedmineClientToCreateProject()
            TuesmonClient tuesmonClient = createTuesmonClient()
        and: 'building a migrator instances'
            ProjectMigrator projectMigrator = new ProjectMigrator(redmineClient, tuesmonClient)
            IssueMigrator issueMigrator = new IssueMigrator(redmineClient, tuesmonClient)
        when: 'migrating a given project'
            RedmineTuesmonRef migratedProjectInfo = projectMigrator.migrateProject(buildRedmineProject())
        and: 'trying to migrate basic the estructure of related issues'
            List<TuesmonIssue> migratedIssues = issueMigrator.migrateIssuesByProject(migratedProjectInfo)
            TuesmonIssue firstTuesmonIssue = migratedIssues.first()
            TuesmonAttachment firstIssueAttachment = firstTuesmonIssue.attachments.first()
            TuesmonHistory firstIssueHistory = firstTuesmonIssue.history.first()
        then: 'checking basic request data'
            migratedIssues.size() > 0
            migratedIssues.every(basicData)
    }

    Closure<Boolean> basicData = {
        it.ref &&
        it.type &&
        it.status  &&
        it.priority  &&
        it.severity &&
        it.subject &&
        it.description &&
        it.project  &&
        it.owner  &&
        it.createdDate &&
        it.finishedDate &&
        it.attachments &&
        it.history
    }



}

