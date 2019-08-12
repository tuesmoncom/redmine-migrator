package net.kaleidos.redmine.testdata

import net.kaleidos.domain.Project as TuesmonProject
import net.kaleidos.domain.Membership as TuesmonMembership
import net.kaleidos.domain.IssueStatus as TuesmonIssueStatus

import com.taskadapter.redmineapi.bean.User as RedmineUser
import com.taskadapter.redmineapi.bean.Issue as RedmineIssue
import com.taskadapter.redmineapi.bean.Tracker as RedmineTracker
import com.taskadapter.redmineapi.bean.Journal as RedmineHistory
import com.taskadapter.redmineapi.bean.Project as RedmineProject
import com.taskadapter.redmineapi.bean.Attachment as RedmineAttachment
import com.taskadapter.redmineapi.bean.IssueStatus as RedmineIssueStatus
import com.taskadapter.redmineapi.bean.IssuePriority as RedmineIssuePriority

import com.taskadapter.redmineapi.internal.Transport.Pagination

class IssueDataProvider {

    Iterator<RedmineIssue> buildRedmineIssueList() {
        return (1..10)
            .collect(this.&buildRedmineIssueWithIndex)
            .collect(this.&buildPagination)
            .iterator()
    }

    Pagination<RedmineIssue> buildPagination(RedmineIssue issue) {
       return new Pagination(total:10, limit:1, offset:1, list: [issue])
    }

    RedmineIssue buildRedmineIssueWithIndex(Integer index) {
        return new RedmineIssue(
            id: 17002 + index,
            project: [id: 11, name: 'MyRedmineProject'] as RedmineProject,
            tracker: [id: 22, name: 'tracker-1'] as RedmineTracker,
            statusName: 'status-1',
            priorityText: 'priority-1',
            author: [id: 245, name: 'Ronnie'] as RedmineUser,
            assignee: [id: 245, name: 'Ronnie'] as RedmineUser,
            createdOn: new Date() - 20,
            dueDate: new Date() + 1,
            subject: "Integration [${index}]"
        )
    }

    List<RedmineAttachment> buildAttachmentList() {
        return (1..10).collect(this.&buildAttachmentWithIndex)
    }

    RedmineAttachment buildAttachmentWithIndex(Integer index) {
        return new RedmineAttachment(
            contentURL: buildLocalResourceUrl(),
            fileName: "debian.jpg",
            description: "simple image",
            createdOn: new Date() - 20,
            author: [id: 245, name: 'Ronnie'] as RedmineUser
        )
    }

    List<RedmineHistory> buildHistoryList() {
        return (1..10).collect(this.&buildHistoryWithIndex)
    }

    RedmineHistory buildHistoryWithIndex(Integer index) {
        def even = index % 2 == 0
       return new RedmineHistory(
            user:[id: 245, name: 'Ronnie'] as RedmineUser,
            createdOn: new Date() - 10,
            notes: even ? "some comment on history ${index}" : "",
            details: even ? null : [[
                property: "attr",
                name: "done_ratio",
                old_value: "0",
                new_value: "100"
            ]]
       )
    }

    String buildLocalResourceUrl() {
        return new File("src/test/resources/debian.jpg")
            .toURL()
            .toString()
    }

}
