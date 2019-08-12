package net.kaleidos.redmine.migrator

import net.kaleidos.redmine.RedmineClient
import net.kaleidos.tuesmon.TuesmonClient

import com.taskadapter.redmineapi.bean.User as RedmineUser
import com.taskadapter.redmineapi.bean.Journal as RedmineHistory
import com.taskadapter.redmineapi.bean.Attachment as RedmineAttachment

import net.kaleidos.domain.User as TuesmonUser
import net.kaleidos.domain.History as TuesmonHistory
import net.kaleidos.domain.Attachment as TuesmonAttachment

import groovy.util.logging.Log4j

@Log4j
abstract class AbstractMigrator<A> implements Migrator<A> {

    final TuesmonClient tuesmonClient
    final RedmineClient redmineClient

    AbstractMigrator(final RedmineClient redmineClient, final TuesmonClient tuesmonClient) {
        this.redmineClient = redmineClient
        this.tuesmonClient = tuesmonClient
    }

    def executeSafelyAndWarn = { action ->
        return { source ->
            try {
                action(source)
            } catch(Throwable e) {
                log.warn(e.message)
            }
        }
    }

    TuesmonAttachment convertToTuesmonAttachment(RedmineAttachment att) {
        RedmineUser user = redmineClient.findUserFullById(att.author.id)
        byte[] attachmentData = redmineClient.downloadAttachment(att)

        return new TuesmonAttachment(
            data: attachmentData.encodeBase64(),
            name: att.fileName,
            description: att.description,
            createdDate: att.createdOn,
            owner: user.mail
        )

    }

    TuesmonHistory convertToTuesmonHistory(RedmineHistory journal) {
        RedmineUser redmineUser = redmineClient.findUserFullById(journal.user.id)
        TuesmonUser tuesmonUser =
            new TuesmonUser(
                name: redmineUser.fullName,
                email: redmineUser.mail)

        return new TuesmonHistory(
            user: tuesmonUser,
            createdAt: journal.createdOn,
            comment: journal.notes
        )
    }


}
