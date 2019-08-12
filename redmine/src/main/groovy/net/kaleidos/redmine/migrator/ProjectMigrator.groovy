package net.kaleidos.redmine.migrator

import com.taskadapter.redmineapi.bean.IssueStatus as RedmineIssueStatus
import com.taskadapter.redmineapi.bean.Membership as RedmineMembership
import com.taskadapter.redmineapi.bean.Project as RedmineProject
import com.taskadapter.redmineapi.bean.User as RedmineUser
import groovy.transform.InheritConstructors
import net.kaleidos.domain.IssueStatus as TuesmonIssueStatus
import net.kaleidos.domain.Membership as TuesmonMembership
import net.kaleidos.domain.Project as TuesmonProject
import net.kaleidos.redmine.RedmineTuesmonRef

@InheritConstructors
class ProjectMigrator extends AbstractMigrator<TuesmonProject> {

    static final String SEVERITY_NORMAL = 'Normal'

    String migrationUserEmail

    List<RedmineTuesmonRef> migrateAllProjects() {
        return redmineClient.findAllProject().collect(this.&migrateProject)
    }

    RedmineTuesmonRef migrateProject(final RedmineProject redmineProject) {
        return new RedmineTuesmonRef(
            redmineProject.id,
            redmineProject.identifier,
            save(buildProjectFromRedmineProject(redmineProject))
        )
    }

    TuesmonProject buildProjectFromRedmineProject(final RedmineProject redmineProject) {
        List<TuesmonMembership> memberships =
            getMembershipsByProjectIdentifier(redmineProject.identifier)

        return new TuesmonProject(
            name: redmineProject.name,
            description: redmineProject.with { description ?: name },
            roles: memberships?.role?.unique(),
            memberships: memberships,
            issueTypes: issueTypes,
            issueStatuses: issueStatuses,
            issuePriorities: issuePriorities,
            issueSeverities: issueSeverities
        )
    }

    List<TuesmonMembership> getMembershipsByProjectIdentifier(String identifier) {
        return redmineClient
            .findAllMembershipByProjectIdentifier(identifier)
            .collect(this.&transformToTuesmonMembership)
            .findAll()
            .findAll { it.email != migrationUserEmail }
    }

    TuesmonMembership transformToTuesmonMembership(final RedmineMembership redmineMembership) {

        if (!redmineMembership?.user?.id) {
            return
        }

        RedmineUser user =
            redmineClient.findUserFullById(redmineMembership.user.id)

        return new TuesmonMembership(
            email: user.mail,
            role: redmineMembership.roles.name.first()
        )
    }

    List<String> getIssueTypes() {
        return redmineClient.findAllTracker().collect(extractName)
    }

    List<TuesmonIssueStatus> getIssueStatuses() {
        return redmineClient.findAllIssueStatus().collect(this.&tuesmonIssueStatusFromRedmineIssueStatus)
    }

    TuesmonIssueStatus tuesmonIssueStatusFromRedmineIssueStatus(final RedmineIssueStatus status) {
        return new TuesmonIssueStatus(name: status.name, isClosed: status.isClosed())
    }

    List<String> getIssuePriorities() {
        return redmineClient.findAllIssuePriority().collect(extractName)
    }

    List<String> getIssueSeverities() {
        return [SEVERITY_NORMAL]
    }

    Closure<String> extractName = { it.name }

    @Override
    TuesmonProject save(final TuesmonProject tuesmonProject) {
        return tuesmonClient.createProject(tuesmonProject)
    }

}
