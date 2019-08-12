package net.kaleidos.redmine.migrator

import com.taskadapter.redmineapi.bean.Version as RedmineVersion
import groovy.transform.InheritConstructors
import net.kaleidos.domain.Milestone as TuesmonMilestone
import net.kaleidos.domain.Project as TuesmonProject
import net.kaleidos.redmine.RedmineTuesmonRef

@InheritConstructors
class VersionMigrator extends AbstractMigrator<TuesmonMilestone> {


    List<TuesmonMilestone> migrateVersionsFromProject(final RedmineTuesmonRef ref) {
        return redmineClient
            .findAllVersionByProjectId(ref.redmineId)
            .collect(this.&buildTuesmonMilestone.rcurry(ref.project))
            .collect(this.&save)
    }

    TuesmonMilestone buildTuesmonMilestone(final RedmineVersion version, final TuesmonProject tuesmonProject) {
        new TuesmonMilestone(
            project: tuesmonProject,
            name: version.name,
            isClosed: version.status == 'closed' ? true : false,
            startDate: version.createdOn,
            endDate: version.dueDate
        )
    }

    @Override
    TuesmonMilestone save(final TuesmonMilestone tuesmonMilestone) {
        return tuesmonClient.createMilestone(tuesmonMilestone)
    }
}