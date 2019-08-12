package net.kaleidos.redmine

import net.kaleidos.domain.Project as TuesmonProject

class RedmineTuesmonRef {

    final Integer redmineId
    final String redmineIdentifier
    final TuesmonProject project

    public RedmineTuesmonRef(
        final Integer redmineId,
        final String redmineIdentifier,
        final TuesmonProject project) {

        this.redmineId = redmineId
        this.redmineIdentifier = redmineIdentifier
        this.project = project

    }

}
